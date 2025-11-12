export interface Observacion {
  id: number;
  descripcion: string;
  estado: EstadoObservacion;
  proyectoId: number;
  proyectoNombre: string;
  ongId: number;
  ongNombre: string;
  bonitaCaseId?: string;
  fechaCreacion: string;
  fechaLimite: string;
  fechaResolucion?: string;
  correcciones?: Correccion[];
}

export interface CreateObservacionRequest {
  descripcion: string;
  proyectoId: number;
  ongId: number;
}

export interface Correccion {
  id: number;
  observacionId: number;
  detalle: string;
  usuarioId: number;
  usuarioNombre: string;
  fechaCreacion: string;
}

export interface CreateCorreccionRequest {
  observacionId: number;
  detalle: string;
}

export type EstadoObservacion = 'PENDIENTE' | 'EN_REVISION' | 'RESUELTA' | 'RECHAZADA' | 'VENCIDA';
