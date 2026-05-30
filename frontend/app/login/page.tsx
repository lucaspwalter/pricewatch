"use client";

import { AuthCard } from "@/components/AuthCard";
import { FormField } from "@/components/FormField";
import { StatusMessage } from "@/components/StatusMessage";
import { SubmitButton } from "@/components/SubmitButton";
import { apiFetch } from "@/lib/api";
import { setToken } from "@/lib/auth";
import type { AuthResponse } from "@/lib/types";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await apiFetch<AuthResponse>("/auth/login", {
        method: "POST",
        auth: false,
        body: JSON.stringify({ email, password })
      });
      setToken(response.token);
      router.push("/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel entrar");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard title="Entrar" subtitle="Acesse seus alertas de preco">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <StatusMessage type="error" message={error} />}
        <FormField label="E-mail" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
        <FormField label="Senha" type="password" value={password} onChange={(event) => setPassword(event.target.value)} required />
        <SubmitButton loading={loading}>Entrar</SubmitButton>
      </form>
      <p className="mt-4 text-center text-sm text-slate-500">
        Ainda nao tem conta?{" "}
        <Link href="/register" className="font-medium text-brand-700">
          Criar cadastro
        </Link>
      </p>
    </AuthCard>
  );
}
