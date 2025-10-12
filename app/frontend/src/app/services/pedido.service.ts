import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pedido } from '../models/pedido.model';
import { NewCompromisoDto } from '../models/compromiso.model';

@Injectable({
  providedIn: 'root'
})
export class PedidoService {
  private apiCloudUrl = 'http://localhost:8085/Dssd2025Cloud/api/v1/pedidos'; 
  private apiUrl = 'http://localhost:8081/api/pedidos';

  constructor(private http: HttpClient) { }

  getAllPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.apiCloudUrl);
  }

  getPedidoById(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.apiCloudUrl}/${id}`);
  }

  createCompromiso(id: number, compromiso: NewCompromisoDto, proyectId: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/compromisos`, { compromiso, proyectId });
  }

}
