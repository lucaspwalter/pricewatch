package com.pricewatch.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

@Component
public class ProductPageClient {

    private final ObjectMapper objectMapper;
    private final List<String> allowedDomains;
    private volatile String mercadoLivreAccessToken;
    private final String mercadoLivreClientId;
    private final String mercadoLivreClientSecret;
    private final String browserUrl;

    private static final Pattern MERCADO_LIVRE_ITEM_ID = Pattern.compile("(?i)(MLB)-?(\\d{6,})");
    private static final Pattern MERCADO_LIVRE_PRODUCT_ID = Pattern.compile("(?i)(MLBU\\d{6,})");
    private static final Pattern MERCADO_LIVRE_WID = Pattern.compile("(?i)(?:[?#&]wid=)(MLB\\d{6,})");

    public ProductPageClient(
            ObjectMapper objectMapper,
            @Value("${pricewatch.allowed-store-domains}") List<String> allowedDomains,
            @Value("${mercadolivre.access-token:}") String mercadoLivreAccessToken,
            @Value("${mercadolivre.client-id:}") String mercadoLivreClientId,
            @Value("${mercadolivre.client-secret:}") String mercadoLivreClientSecret
            , @Value("${pricewatch.browser-url:}") String browserUrl
    ) {
        this.objectMapper = objectMapper;
        this.allowedDomains = allowedDomains.stream()
                .map(domain -> domain.trim().toLowerCase(Locale.ROOT))
                .toList();
        this.mercadoLivreAccessToken = mercadoLivreAccessToken == null ? "" : mercadoLivreAccessToken.trim();
        this.mercadoLivreClientId = mercadoLivreClientId == null ? "" : mercadoLivreClientId.trim();
        this.mercadoLivreClientSecret = mercadoLivreClientSecret == null ? "" : mercadoLivreClientSecret.trim();
        this.browserUrl = browserUrl == null ? "" : browserUrl.trim();
    }

    public ProductData getProduct(String rawUrl) {
        String url = validateUrl(rawUrl);
        if (requiresBrowser(url)) {
            ProductData browserProduct = fetchWithBrowser(url);
            if (browserProduct != null) {
                return browserProduct;
            }
        }
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
                            + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .referrer("https://www.google.com/")
                    .header("Accept-Language", "pt-BR,pt;q=0.9,en;q=0.8")
                    .timeout(15_000)
                    .maxBodySize(3_000_000)
                    .get();
            ProductData product = extractProduct(document, url);
            if (product != null) {
                return product;
            }
            ProductData mercadoLivreProduct = fetchMercadoLivreApi(url);
            if (mercadoLivreProduct != null) {
                return mercadoLivreProduct;
            }
            if (isMercadoLivre(url) && isBlockedMercadoLivrePage(document)) {
                if (!hasMercadoLivreCredentials()) {
                    throw new IllegalStateException(
                            "Mercado Livre bloqueou a consulta. Configure MERCADOLIVRE_ACCESS_TOKEN"
                    );
                }
                throw new IllegalStateException(
                        "Mercado Livre bloqueou a pagina e o token configurado nao acessou a API"
                );
            }
            String title = extractTitle(document, url);
            BigDecimal price = extractPrice(document, url);
            if (title == null || price == null) {
                throw new IllegalStateException("Nome ou preco nao encontrado na pagina");
            }
            return new ProductData(url, title, price);
        } catch (IOException exception) {
            throw new IllegalStateException("Loja bloqueou ou nao respondeu ao PriceWatch", exception);
        }
    }

    private ProductData fetchWithBrowser(String url) {
        if (browserUrl.isBlank()) {
            return null;
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage",
                "--disable-blink-features=AutomationControlled", "--lang=pt-BR",
                "--window-size=1365,900");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        WebDriver driver = null;
        try {
            driver = new RemoteWebDriver(URI.create(browserUrl).toURL(), options);
            if (driver instanceof HasCdp cdp) {
                cdp.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", java.util.Map.of(
                        "source", "Object.defineProperty(navigator,'webdriver',{get:()=>undefined});"
                                + "Object.defineProperty(navigator,'languages',{get:()=>['pt-BR','pt']});"
                ));
            }
            driver.get(url);
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(current ->
                    browserProduct(current, url) != null || blockedBrowserPage(current));
            return browserProduct(driver, url);
        } catch (Exception exception) {
            return null;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private ProductData browserProduct(WebDriver driver, String url) {
        String title = firstText(driver, titleSelectors(url));
        BigDecimal price = firstPrice(driver, priceSelectors(url));
        if (title != null && price != null) {
            return new ProductData(url, title, price);
        }
        if (driver instanceof JavascriptExecutor javascript) {
            Object html = javascript.executeScript("return document.documentElement.outerHTML");
            if (html != null) {
                return extractProduct(Jsoup.parse(html.toString(), url), url);
            }
        }
        return null;
    }

    private String firstText(WebDriver driver, List<String> selectors) {
        for (String selector : selectors) {
            for (WebElement element : driver.findElements(By.cssSelector(selector))) {
                String text = blankToNull(element.getText());
                if (text != null) return text;
            }
        }
        return null;
    }

    private BigDecimal firstPrice(WebDriver driver, List<String> selectors) {
        for (String selector : selectors) {
            for (WebElement element : driver.findElements(By.cssSelector(selector))) {
                BigDecimal price = parsePrice(element.getText());
                if (price != null && price.signum() > 0) return price;
            }
        }
        return null;
    }

    private List<String> titleSelectors(String url) {
        if (isShopee(url)) return List.of("h1", "[data-testid=pdp-product-title]");
        if (isShein(url)) return List.of(".product-intro__head-name", "h1");
        return List.of("h1[data-pl=product-title]", "h1");
    }

    private List<String> priceSelectors(String url) {
        if (isShopee(url)) return List.of("[data-testid=pdp-price]", ".pqTWkA", "section h1 + div");
        if (isShein(url)) return List.of(".product-intro__head-price", ".from", "[class*=price]");
        return List.of("[data-pl=product-price]", ".product-price-value", "[class*=price--current]");
    }

    private boolean blockedBrowserPage(WebDriver driver) {
        String source = driver.getPageSource().toLowerCase(Locale.ROOT);
        return source.contains("verify you are human") || source.contains("verifique se você é humano")
                || source.contains("access denied") || source.contains("captcha");
    }

    private boolean requiresBrowser(String url) {
        return isShopee(url) || isShein(url) || isAliExpress(url);
    }

    ProductData extractProduct(Document document, String url) {
        if (isBlockedMercadoLivrePage(document)) {
            return null;
        }
        String title = extractTitle(document, url);
        BigDecimal price = extractPrice(document, url);
        return title == null || price == null ? null : new ProductData(url, title, price);
    }

    String validateUrl(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl == null ? "" : rawUrl.trim()).normalize();
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null || uri.getUserInfo() != null) {
                throw new IllegalArgumentException("Use uma URL HTTPS valida");
            }
            String normalizedHost = host.toLowerCase(Locale.ROOT);
            boolean allowed = allowedDomains.stream()
                    .anyMatch(domain -> normalizedHost.equals(domain) || normalizedHost.endsWith("." + domain));
            if (!allowed) {
                throw new IllegalArgumentException("Loja nao suportada: " + normalizedHost);
            }
            return uri.toString();
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Use uma URL HTTPS valida", exception);
        }
    }

    private String extractTitle(Document document, String url) {
        if (isAmazon(url)) {
            Element amazonTitle = document.selectFirst("#productTitle");
            if (amazonTitle != null && blankToNull(amazonTitle.text()) != null) {
                return amazonTitle.text().trim();
            }
        }
        if (isMercadoLivre(url)) {
            Element mercadoLivreTitle = document.selectFirst("h1.ui-pdp-title");
            if (mercadoLivreTitle != null && blankToNull(mercadoLivreTitle.text()) != null) {
                return mercadoLivreTitle.text().trim();
            }
        }
        String jsonLdTitle = findJsonLdText(document, "name");
        if (jsonLdTitle != null) {
            return jsonLdTitle;
        }
        Element meta = document.selectFirst("meta[property=og:title]");
        return meta == null ? null : blankToNull(meta.attr("content"));
    }

    private BigDecimal extractPrice(Document document, String url) {
        if (isAmazon(url)) {
            for (String selector : List.of(
                    "#corePriceDisplay_desktop_feature_div .a-price .a-offscreen",
                    "#corePrice_feature_div .a-price .a-offscreen",
                    "#apex_desktop .priceToPay .a-offscreen",
                    "#apex_desktop .a-price .a-offscreen",
                    "#price_inside_buybox",
                    "#priceblock_ourprice",
                    "#priceblock_dealprice"
            )) {
                Element element = document.selectFirst(selector);
                BigDecimal price = element == null ? null : parsePrice(element.text());
                if (price != null) {
                    return price;
                }
            }
        }
        if (isMercadoLivre(url)) {
            for (Element amount : document.select(
                    ".ui-pdp-price__second-line .andes-money-amount, "
                            + ".ui-pdp-price__main-container .andes-money-amount"
            )) {
                Element fraction = amount.selectFirst(".andes-money-amount__fraction");
                if (fraction == null) {
                    continue;
                }
                Element cents = amount.selectFirst(".andes-money-amount__cents");
                String value = fraction.text() + (cents == null ? "" : "," + cents.text());
                BigDecimal price = parsePrice(value);
                if (price != null) {
                    return price;
                }
            }
        }
        for (Element script : document.select("script[type=application/ld+json]")) {
            try {
                BigDecimal price = findPrice(objectMapper.readTree(script.data()));
                if (price != null) {
                    return price;
                }
            } catch (Exception ignored) {
                // Algumas lojas publicam blocos JSON-LD invalidos; tenta os demais metadados.
            }
        }
        for (String selector : List.of(
                "meta[property=product:price:amount]",
                "meta[property=og:price:amount]",
                "meta[itemprop=price]"
        )) {
            Element meta = document.selectFirst(selector);
            if (meta != null) {
                BigDecimal price = parsePrice(meta.hasAttr("content") ? meta.attr("content") : meta.attr("value"));
                if (price != null) {
                    return price;
                }
            }
        }
        return null;
    }

    private ProductData fetchMercadoLivreApi(String url) {
        if (!isMercadoLivre(url) || !hasMercadoLivreCredentials()) {
            return null;
        }
        ProductData catalogProduct = fetchMercadoLivreCatalog(url);
        if (catalogProduct != null) {
            return catalogProduct;
        }
        Matcher matcher = MERCADO_LIVRE_ITEM_ID.matcher(url);
        if (!matcher.find()) {
            return null;
        }
        String itemId = matcher.group(1).toUpperCase(Locale.ROOT) + matcher.group(2);
        try {
            String json = Jsoup.connect("https://api.mercadolibre.com/items/" + itemId)
                    .ignoreContentType(true)
                    .header("Authorization", "Bearer " + mercadoLivreAccessToken)
                    .timeout(15_000)
                    .execute()
                    .body();
            JsonNode item = objectMapper.readTree(json);
            String title = blankToNull(item.path("title").asText(null));
            BigDecimal price = parsePrice(item.path("price").asText(null));
            return title == null || price == null ? null : new ProductData(url, title, price);
        } catch (Exception exception) {
            return null;
        }
    }

    private ProductData fetchMercadoLivreCatalog(String url) {
        Matcher productMatcher = MERCADO_LIVRE_PRODUCT_ID.matcher(url);
        if (!productMatcher.find()) {
            return null;
        }
        String productId = productMatcher.group(1).toUpperCase(Locale.ROOT);
        Matcher widMatcher = MERCADO_LIVRE_WID.matcher(url);
        String preferredItemId = widMatcher.find() ? widMatcher.group(1).toUpperCase(Locale.ROOT) : null;
        try {
            String json = mercadoLivreGet("https://api.mercadolibre.com/products/" + productId + "/items");
            JsonNode results = objectMapper.readTree(json).path("results");
            JsonNode selected = null;
            for (JsonNode item : results) {
                if (selected == null || item.path("price").asDouble(Double.MAX_VALUE)
                        < selected.path("price").asDouble(Double.MAX_VALUE)) {
                    selected = item;
                }
                if (preferredItemId != null && preferredItemId.equalsIgnoreCase(item.path("item_id").asText())) {
                    selected = item;
                    break;
                }
            }
            BigDecimal price = selected == null ? null : parsePrice(selected.path("price").asText(null));
            String title = titleFromMercadoLivreUrl(url);
            return title == null || price == null ? null : new ProductData(url, title, price);
        } catch (Exception exception) {
            return null;
        }
    }

    private String mercadoLivreGet(String url) throws IOException {
        if (mercadoLivreAccessToken.isBlank()) {
            refreshMercadoLivreAccessToken();
        }
        try {
            return mercadoLivreGetWithCurrentToken(url);
        } catch (HttpStatusException exception) {
            if ((exception.getStatusCode() != 401 && exception.getStatusCode() != 403)
                    || mercadoLivreClientId.isBlank() || mercadoLivreClientSecret.isBlank()) {
                throw exception;
            }
            refreshMercadoLivreAccessToken();
            return mercadoLivreGetWithCurrentToken(url);
        }
    }

    private String mercadoLivreGetWithCurrentToken(String url) throws IOException {
        return Jsoup.connect(url)
                .ignoreContentType(true)
                .header("Authorization", "Bearer " + mercadoLivreAccessToken)
                .timeout(15_000)
                .execute()
                .body();
    }

    private synchronized void refreshMercadoLivreAccessToken() throws IOException {
        if (mercadoLivreClientId.isBlank() || mercadoLivreClientSecret.isBlank()) {
            throw new IllegalStateException("Credenciais do Mercado Livre nao configuradas");
        }
        String body = Jsoup.connect("https://api.mercadolibre.com/oauth/token")
                .ignoreContentType(true)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .data("grant_type", "client_credentials")
                .data("client_id", mercadoLivreClientId)
                .data("client_secret", mercadoLivreClientSecret)
                .method(org.jsoup.Connection.Method.POST)
                .timeout(15_000)
                .execute()
                .body();
        try {
            String token = blankToNull(objectMapper.readTree(body).path("access_token").asText(null));
            if (token == null) {
                throw new IllegalStateException("Mercado Livre nao retornou access token");
            }
            mercadoLivreAccessToken = token;
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new IllegalStateException("Resposta OAuth invalida do Mercado Livre", exception);
        }
    }

    private boolean hasMercadoLivreCredentials() {
        return !mercadoLivreAccessToken.isBlank()
                || (!mercadoLivreClientId.isBlank() && !mercadoLivreClientSecret.isBlank());
    }

    private String titleFromMercadoLivreUrl(String url) {
        try {
            String path = URI.create(url).getPath();
            int productMarker = path.indexOf("/up/");
            if (productMarker <= 1) {
                return null;
            }
            String slug = path.substring(1, productMarker).replace('-', ' ').trim();
            if (slug.isBlank()) {
                return null;
            }
            return Character.toUpperCase(slug.charAt(0)) + slug.substring(1);
        } catch (Exception exception) {
            return null;
        }
    }

    private boolean isBlockedMercadoLivrePage(Document document) {
        String location = document.location().toLowerCase(Locale.ROOT);
        return location.contains("account-verification") || location.contains("captcha");
    }

    private boolean isAmazon(String url) {
        return host(url).endsWith("amazon.com.br");
    }

    private boolean isMercadoLivre(String url) {
        String host = host(url);
        return host.endsWith("mercadolivre.com.br") || host.endsWith("mercadolivre.com");
    }

    private boolean isAliExpress(String url) {
        return host(url).endsWith("aliexpress.com");
    }

    private boolean isShopee(String url) {
        return host(url).endsWith("shopee.com.br");
    }

    private boolean isShein(String url) {
        return host(url).endsWith("shein.com");
    }


    private String host(String url) {
        try {
            String host = URI.create(url).getHost();
            return host == null ? "" : host.toLowerCase(Locale.ROOT);
        } catch (Exception ignored) {
            return "";
        }
    }

    private String findJsonLdText(Document document, String field) {
        for (Element script : document.select("script[type=application/ld+json]")) {
            try {
                String value = findText(objectMapper.readTree(script.data()), field);
                if (value != null) {
                    return value;
                }
            } catch (Exception ignored) {
                // Tenta proximo bloco.
            }
        }
        return null;
    }

    private String findText(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        if (isProduct(node) && node.path(field).isTextual()) {
            return blankToNull(node.path(field).asText());
        }
        for (JsonNode child : node) {
            String value = findText(child, field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private BigDecimal findPrice(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (isProduct(node)) {
            BigDecimal price = priceFromNode(node.path("offers"));
            if (price != null) {
                return price;
            }
        }
        for (JsonNode child : node) {
            BigDecimal price = findPrice(child);
            if (price != null) {
                return price;
            }
        }
        return null;
    }

    private BigDecimal priceFromNode(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                BigDecimal price = priceFromNode(child);
                if (price != null) {
                    return price;
                }
            }
            return null;
        }
        for (String field : List.of("price", "lowPrice")) {
            BigDecimal price = parsePrice(node.path(field).asText(null));
            if (price != null) {
                return price;
            }
        }
        return priceFromNode(node.path("priceSpecification"));
    }

    private boolean isProduct(JsonNode node) {
        JsonNode type = node.path("@type");
        if (type.isTextual()) {
            return "product".equalsIgnoreCase(type.asText());
        }
        if (type.isArray()) {
            for (JsonNode value : type) {
                if ("product".equalsIgnoreCase(value.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private BigDecimal parsePrice(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("[^0-9,.-]", "");
        if (normalized.contains(",") && normalized.contains(".")) {
            normalized = normalized.lastIndexOf(',') > normalized.lastIndexOf('.')
                    ? normalized.replace(".", "").replace(',', '.')
                    : normalized.replace(",", "");
        } else if (normalized.contains(",")) {
            normalized = normalized.replace(',', '.');
        }
        try {
            BigDecimal price = new BigDecimal(normalized);
            return price.signum() >= 0 ? price : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record ProductData(String url, String title, BigDecimal price) {
    }
}
