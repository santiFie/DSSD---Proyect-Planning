import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pedido } from '../models/pedido.model';
import { NewCompromisoDto } from '../models/compromiso.model';

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

  // Método para obtener compromisos por ID de pedido, le pega primero al backend local
  getCompromisosByPedidoId(pedidoId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${pedidoId}/compromisos`);
  }

}
