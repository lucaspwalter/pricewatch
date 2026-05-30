export type User = {
  id: number;
  email: string;
  phone: string;
  createdAt: string;
};

export type AuthResponse = {
  token: string;
  user: User;
};

export type Product = {
  id: number;
  externalId: string;
  title: string;
  url: string;
  createdAt: string;
};

export type Alert = {
  id: number;
  productId: number;
  productTitle: string;
  targetPrice: number;
  active: boolean;
  createdAt: string;
};

export type Notification = {
  id: number;
  alertId: number;
  productTitle: string;
  channel: "EMAIL" | "WHATSAPP";
  sentAt: string;
  status: string;
};

export type StoredProduct = Pick<Product, "id" | "externalId" | "title" | "url">;
