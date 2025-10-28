import { Pedido } from "./pedido.model";

export interface NewCompromisoDto {
    ongColaboranteId: number;
    descripcion: string;
    fechaCompromiso: string;
    estado: string;
    version: number;
    pedido: Pedido;
}