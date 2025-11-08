import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pedido } from '../models/pedido.model';
import { NewCompromisoDto, Compromiso } from '../models/compromiso.model';

@Injectable({
  providedIn: 'root'
})
export class PedidoService {
  private apiUrl = 'http://localhost:8081/api/pedidos';
  

  constructor(private http: HttpClient) { }

  getAllPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.apiUrl}/all`);
  }

  getPedidosByProyecto(proyectoId: number): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.apiUrl}/proyecto/${proyectoId}`);
  }

  getPedidoById(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.apiUrl}/${id}`);
  }

  createCompromiso(compromiso: NewCompromisoDto): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${compromiso.pedido.id}/compromisos`, compromiso);
  }

  // Método para obtener compromisos por ID de pedido
  getCompromisosByPedidoId(pedidoId: number): Observable<Compromiso[]> {
    return this.http.get<Compromiso[]>(`${this.apiUrl}/${pedidoId}/compromisos`);
  }

  // Método para aceptar un compromiso
  aceptarCompromiso(pedidoId: number, compromisoId: number, proyectoId: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${pedidoId}/compromisos/${compromisoId}/aceptar/${proyectoId}`, {});
  }

  // Método para rechazar un compromiso
  rechazarCompromiso(pedidoId: number, compromisoId: number, proyectoId: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${pedidoId}/compromisos/${compromisoId}/rechazar/${proyectoId}`, {});
  }

}
