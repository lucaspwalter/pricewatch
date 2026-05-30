"use client";

import { getToken } from "@/lib/auth";
import { usePathname, useRouter } from "next/navigation";
import { ReactNode, useEffect, useState } from "react";

const PUBLIC_ROUTES = ["/login", "/register"];

export function AuthGuard({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!PUBLIC_ROUTES.includes(pathname) && !getToken()) {
      router.replace("/login");
      return;
    }
    setReady(true);
  }, [pathname, router]);

  if (!ready && !PUBLIC_ROUTES.includes(pathname)) {
    return (
      <div className="flex min-h-screen items-center justify-center text-sm text-slate-500">
        Carregando...
      </div>
    );
  }

  return <>{children}</>;
}
