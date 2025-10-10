export interface Pedido {
  id: number;
  title?: string;
  description?: string;
  status?: string;
  createdAt?: string; // ISO date
  [key: string]: any;
}
