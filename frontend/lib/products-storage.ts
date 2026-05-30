"use client";

import type { Product, StoredProduct } from "@/lib/types";

const PRODUCTS_KEY = "pricewatch_products";

export function getStoredProducts(): StoredProduct[] {
  if (typeof window === "undefined") {
    return [];
  }

  const raw = window.localStorage.getItem(PRODUCTS_KEY);
  if (!raw) {
    return [];
  }

  try {
    return JSON.parse(raw) as StoredProduct[];
  } catch {
    return [];
  }
}

export function saveStoredProduct(product: Product) {
  const stored = getStoredProducts();
  const nextProduct: StoredProduct = {
    id: product.id,
    externalId: product.externalId,
    title: product.title,
    url: product.url
  };
  const next = [nextProduct, ...stored.filter((item) => item.id !== product.id)];
  window.localStorage.setItem(PRODUCTS_KEY, JSON.stringify(next));
}
