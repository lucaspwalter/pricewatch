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

export default function RegisterPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await apiFetch<AuthResponse>("/auth/register", {
        method: "POST",
        auth: false,
        body: JSON.stringify({ email, phone, password })
      });
      setToken(response.token);
      router.push("/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nao foi possivel cadastrar");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard title="Criar conta" subtitle="Cadastre seus dados para receber notificacoes">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <StatusMessage type="error" message={error} />}
        <FormField label="E-mail" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
        <FormField label="Telefone" type="tel" value={phone} onChange={(event) => setPhone(event.target.value)} required />
        <FormField label="Senha" type="password" value={password} onChange={(event) => setPassword(event.target.value)} required />
        <SubmitButton loading={loading}>Criar conta</SubmitButton>
      </form>
      <p className="mt-4 text-center text-sm text-slate-500">
        Ja tem conta?{" "}
        <Link href="/login" className="font-medium text-brand-700">
          Entrar
        </Link>
      </p>
    </AuthCard>
  );
}
