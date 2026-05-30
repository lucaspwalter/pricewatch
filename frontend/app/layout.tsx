import type { Metadata } from "next";
import { AuthGuard } from "@/components/AuthGuard";
import "./globals.css";

export const metadata: Metadata = {
  title: "PriceWatch",
  description: "Monitoramento de precos"
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR">
      <body>
        <AuthGuard>{children}</AuthGuard>
      </body>
    </html>
  );
}
