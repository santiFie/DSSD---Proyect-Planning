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

  getPedidoById(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.apiUrl}/${id}`);
  }

  createCompromiso(compromiso: NewCompromisoDto): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${compromiso.pedido.id}/compromisos`, compromiso);
  }

}
