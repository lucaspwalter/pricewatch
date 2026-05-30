"use client";

import { AppShell } from "@/components/AppShell";
import { StatusMessage } from "@/components/StatusMessage";
import { apiFetch } from "@/lib/api";
import type { Notification } from "@/lib/types";
import { useEffect, useState } from "react";

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  function formatStatus(status: string) {
    if (status === "SENT" || status === "SKIPPED_EVOLUTION_NOT_CONFIGURED") {
      return "Registrado";
    }

    if (status === "ERROR") {
      return "Erro";
    }

    return status;
  }

  useEffect(() => {
    async function loadNotifications() {
      try {
        setNotifications(await apiFetch<Notification[]>("/notifications"));
      } catch (err) {
        setError(err instanceof Error ? err.message : "Nao foi possivel carregar notificacoes");
      } finally {
        setLoading(false);
      }
    }

    loadNotifications();
  }, []);

  return (
    <AppShell>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-slate-950">Notificacoes</h1>
        <p className="mt-1 text-sm text-slate-500">Historico de alertas de preco disparados.</p>
      </div>

      {error && <StatusMessage type="error" message={error} />}

      <div className="mt-4 overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="grid grid-cols-12 border-b border-slate-200 bg-slate-100 px-4 py-3 text-xs font-semibold uppercase text-slate-500">
          <span className="col-span-4">Produto</span>
          <span className="col-span-2">Canal</span>
          <span className="col-span-3">Status</span>
          <span className="col-span-3">Data</span>
        </div>
        {loading ? (
          <p className="px-4 py-6 text-sm text-slate-500">Carregando notificacoes...</p>
        ) : notifications.length === 0 ? (
          <p className="px-4 py-6 text-sm text-slate-500">Nenhuma notificacao enviada.</p>
        ) : (
          notifications.map((notification) => (
            <div key={notification.id} className="grid grid-cols-12 items-center border-b border-slate-100 px-4 py-4 text-sm last:border-0">
              <span className="col-span-4 font-medium text-slate-900">{notification.productTitle}</span>
              <span className="col-span-2 text-slate-700">Sistema</span>
              <span className="col-span-3 text-slate-700">{formatStatus(notification.status)}</span>
              <span className="col-span-3 text-slate-500">{new Date(notification.sentAt).toLocaleString("pt-BR")}</span>
            </div>
          ))
        )}
      </div>
    </AppShell>
  );
}
