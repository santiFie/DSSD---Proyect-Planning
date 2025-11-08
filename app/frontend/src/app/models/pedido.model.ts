export interface Pedido {
  id: number;
  proyectoId?: string;
  etapaId?: string;
  descripcion?: string;
  estado?: string;
  createdAt?: string; // ISO date
  [key: string]: any;
}
