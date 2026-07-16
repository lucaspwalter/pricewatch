"use client";

import { AppShell } from "@/components/AppShell";
import { FormField } from "@/components/FormField";
import { StatusMessage } from "@/components/StatusMessage";
import { SubmitButton } from "@/components/SubmitButton";
import { apiFetch } from "@/lib/api";
import { saveStoredProduct } from "@/lib/products-storage";
import type { Product } from "@/lib/types";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";

export default function NewProductPage() {
  const router = useRouter();
  const [url, setUrl] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const product = await apiFetch<Product>("/products", {
        method: "POST",
        body: JSON.stringify({ url })
      });
      saveStoredProduct(product);
      router.push(`/alerts/new?productId=${product.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel cadastrar produto");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AppShell>
      <div className="max-w-xl">
        <h1 className="text-2xl font-semibold text-slate-950">Novo produto</h1>
        <p className="mt-1 text-sm text-slate-500">Cole o link do produto em uma loja suportada.</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4 rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          {error && <StatusMessage type="error" message={error} />}
          <FormField
            label="URL do produto"
            type="url"
            value={url}
            onChange={(event) => setUrl(event.target.value)}
            required
          />
          <SubmitButton loading={loading}>Cadastrar produto</SubmitButton>
        </form>
      </div>
    </AppShell>
  );
}
