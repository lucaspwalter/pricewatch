"use client";

import { AppShell } from "@/components/AppShell";
import { StatusMessage } from "@/components/StatusMessage";
import { apiFetch } from "@/lib/api";
import type { Alert } from "@/lib/types";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function DashboardPage() {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadAlerts() {
      try {
        setAlerts(await apiFetch<Alert[]>("/alerts"));
      } catch (err) {
        setError(err instanceof Error ? err.message : "Nao foi possivel carregar alertas");
      } finally {
        setLoading(false);
      }
    }

    loadAlerts();
  }, []);

  return (
    <AppShell>
      <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-950">Alertas</h1>
          <p className="mt-1 text-sm text-slate-500">Produtos monitorados e status atual.</p>
        </div>
        <Link href="/products/new" className="rounded-md bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">
          Novo produto
        </Link>
      </div>

      {error && <StatusMessage type="error" message={error} />}

      <div className="mt-4 overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="grid grid-cols-12 border-b border-slate-200 bg-slate-100 px-4 py-3 text-xs font-semibold uppercase text-slate-500">
          <span className="col-span-6">Produto</span>
          <span className="col-span-3">Preco-alvo</span>
          <span className="col-span-3">Status</span>
        </div>
        {loading ? (
          <p className="px-4 py-6 text-sm text-slate-500">Carregando alertas...</p>
        ) : alerts.length === 0 ? (
          <p className="px-4 py-6 text-sm text-slate-500">Nenhum alerta cadastrado.</p>
        ) : (
          alerts.map((alert) => (
            <div key={alert.id} className="grid grid-cols-12 items-center border-b border-slate-100 px-4 py-4 text-sm last:border-0">
              <span className="col-span-6 font-medium text-slate-900">{alert.productTitle}</span>
              <span className="col-span-3 text-slate-700">R$ {Number(alert.targetPrice).toFixed(2)}</span>
              <span className="col-span-3">
                <span
                  className={`rounded-full px-2 py-1 text-xs font-medium ${
                    alert.active ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-600"
                  }`}
                >
                  {alert.active ? "Ativo" : "Inativo"}
                </span>
              </span>
            </div>
          ))
        )}
      </div>
    </AppShell>
  );
}
