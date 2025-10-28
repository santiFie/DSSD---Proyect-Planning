import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ong, CreateOngRequest } from '../models/ong.model';

@Injectable({
  providedIn: 'root'
})
export class OngService {
  private apiUrl = 'http://localhost:8081/api/admin';

  constructor(private http: HttpClient) {}

  getAllOngs(): Observable<Ong[]> {
    return this.http.get<Ong[]>(`${this.apiUrl}/ongs`);
  }

  createOng(request: CreateOngRequest): Observable<Ong> {
    return this.http.post<Ong>(`${this.apiUrl}/ongs`, request);
  }
}
