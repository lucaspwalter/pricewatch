package com.pricewatch.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductPageClientTest {

    private ProductPageClient client;

    @BeforeEach
    void setUp() {
        client = new ProductPageClient(
                new ObjectMapper(),
                List.of("amazon.com.br", "mercadolivre.com.br"),
                "",
                "",
                "",
                ""
        );
    }

    @Test
    void extractsAmazonProductPage() {
        String url = "https://www.amazon.com.br/example/dp/B012345678";
        Document document = Jsoup.parse("""
                <html><body>
                  <span id="productTitle">Notebook Example</span>
                  <div id="corePriceDisplay_desktop_feature_div">
                    <span class="a-price"><span class="a-offscreen">R$ 3.499,90</span></span>
                  </div>
                </body></html>
                """, url);

        ProductPageClient.ProductData product = client.extractProduct(document, url);

        assertThat(product.title()).isEqualTo("Notebook Example");
        assertThat(product.price()).isEqualByComparingTo(new BigDecimal("3499.90"));
    }

    @Test
    void extractsMercadoLivreProductPage() {
        String url = "https://produto.mercadolivre.com.br/MLB-1234567890-example-_JM";
        Document document = Jsoup.parse("""
                <html><body>
                  <h1 class="ui-pdp-title">Notebook Example</h1>
                  <div class="ui-pdp-price__second-line">
                    <span class="andes-money-amount">
                      <span class="andes-money-amount__fraction">3.879</span>
                      <span class="andes-money-amount__cents">90</span>
                    </span>
                  </div>
                </body></html>
                """, url);

        ProductPageClient.ProductData product = client.extractProduct(document, url);

        assertThat(product.title()).isEqualTo("Notebook Example");
        assertThat(product.price()).isEqualByComparingTo(new BigDecimal("3879.90"));
    }

    @Test
    void ignoresMercadoLivreVerificationPage() {
        String url = "https://www.mercadolivre.com.br/gz/account-verification";
        Document document = Jsoup.parse("<html><title>Verification</title></html>", url);

        assertThat(client.extractProduct(document, url)).isNull();
    }
}
