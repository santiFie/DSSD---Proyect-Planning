import { Pedido } from "./pedido.model";

export interface Compromiso {
    id: number;
    pedidoId: number;
    ongColaboranteId: number;
    descripcion: string;
    fechaCompromiso: string;
    estado: 'PENDIENTE' | 'ACEPTADO' | 'RECHAZADO';
    version: number;
}

export interface NewCompromisoDto {
    ongColaboranteId: number;
    descripcion: string;
    fechaCompromiso: string;
    estado: string;
    version: number;
    pedido: Pedido;
}