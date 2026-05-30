"use client";

import { AppShell } from "@/components/AppShell";
import { FormField } from "@/components/FormField";
import { StatusMessage } from "@/components/StatusMessage";
import { SubmitButton } from "@/components/SubmitButton";
import { apiFetch } from "@/lib/api";
import { getStoredProducts } from "@/lib/products-storage";
import type { Alert, StoredProduct } from "@/lib/types";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

export default function NewAlertPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [products, setProducts] = useState<StoredProduct[]>([]);
  const [productId, setProductId] = useState("");
  const [targetPrice, setTargetPrice] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const storedProducts = getStoredProducts();
    const selectedProductId = searchParams.get("productId") || storedProducts[0]?.id.toString() || "";
    setProducts(storedProducts);
    setProductId(selectedProductId);
  }, [searchParams]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      await apiFetch<Alert>("/alerts", {
        method: "POST",
        body: JSON.stringify({
          productId: Number(productId),
          targetPrice: Number(targetPrice)
        })
      });
      router.push("/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel criar alerta");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell>
      <div className="max-w-xl">
        <h1 className="text-2xl font-semibold text-slate-950">Novo alerta</h1>
        <p className="mt-1 text-sm text-slate-500">Selecione um produto cadastrado e defina o preco-alvo.</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4 rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          {error && <StatusMessage type="error" message={error} />}

          {products.length === 0 ? (
            <div className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              Cadastre um produto antes de criar o alerta.{" "}
              <Link href="/products/new" className="font-medium underline">
                Novo produto
              </Link>
            </div>
          ) : (
            <label className="block">
              <span className="text-sm font-medium text-slate-700">Produto</span>
              <select
                value={productId}
                onChange={(event) => setProductId(event.target.value)}
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-950 shadow-sm focus:border-brand-600 focus:ring-2 focus:ring-brand-50"
                required
              >
                {products.map((product) => (
                  <option key={product.id} value={product.id}>
                    #{product.externalId} - {product.title}
                  </option>
                ))}
              </select>
            </label>
          )}

          <FormField
            label="Preco-alvo"
            type="number"
            min="0"
            step="0.01"
            value={targetPrice}
            onChange={(event) => setTargetPrice(event.target.value)}
            required
          />
          <SubmitButton loading={loading || products.length === 0}>Criar alerta</SubmitButton>
        </form>
      </div>
    </AppShell>
  );
}
