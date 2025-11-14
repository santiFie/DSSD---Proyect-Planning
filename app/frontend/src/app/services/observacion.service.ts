import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  Observacion, 
  CreateObservacionRequest, 
  Correccion, 
  CreateCorreccionRequest 
} from '../models/observacion.model';

@Injectable({
  providedIn: 'root'
})
export class ObservacionService {
  private apiUrl = 'http://localhost:8081/api/observaciones';

  constructor(private http: HttpClient) {}

  /**
   * Crear una nueva observación (solo DIRECTIVO)
   */
  createObservacion(request: CreateObservacionRequest): Observable<Observacion> {
    return this.http.post<Observacion>(this.apiUrl, request);
  }

  /**
   * Obtener todas las observaciones de un proyecto
   */
  getObservacionesByProyecto(proyectoId: number): Observable<Observacion[]> {
    return this.http.get<Observacion[]>(`${this.apiUrl}/proyecto/${proyectoId}`);
  }

  /**
   * Obtener todas las observaciones de una ONG
   */
  getObservacionesByOng(ongId: number): Observable<Observacion[]> {
    return this.http.get<Observacion[]>(`${this.apiUrl}/ong/${ongId}`);
  }

  /**
   * Obtener una observación por ID
   */
  getObservacionById(id: number): Observable<Observacion> {
    return this.http.get<Observacion>(`${this.apiUrl}/${id}`);
  }

  /**
   * Crear una corrección para una observación
   */
  createCorreccion(request: CreateCorreccionRequest): Observable<Correccion> {
    return this.http.post<Correccion>(`${this.apiUrl}/correcciones`, request);
  }

  /**
   * Obtener todas las correcciones de una observación
   */
  getCorreccionesByObservacion(observacionId: number): Observable<Correccion[]> {
    return this.http.get<Correccion[]>(`${this.apiUrl}/${observacionId}/correcciones`);
  }

  /**
   * Resolver una observación (solo DIRECTIVO)
   */
  resolverObservacion(id: number): Observable<Observacion> {
    return this.http.put<Observacion>(`${this.apiUrl}/${id}/resolver`, {});
  }

  /**
   * Rechazar una corrección (solo DIRECTIVO)
   */
  rechazarCorreccion(id: number, motivoRechazo: string): Observable<Observacion> {
    return this.http.put<Observacion>(`${this.apiUrl}/${id}/rechazar`, { motivoRechazo });
  }

  /**
   * Verificar observaciones vencidas
   */
  verificarObservacionesVencidas(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/verificar-vencidas`, {});
  }
}
