import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Ong, CreateOngRequest } from '../models/ong.model';

@Injectable({
  providedIn: 'root'
})
export class OngService {
  private apiUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  getAllOngs(): Observable<Ong[]> {
    return this.http.get<any>(`${this.apiUrl}/ongs/all`).pipe(
      map(response => response.ongs || [])
    );
  }

  createOng(request: CreateOngRequest): Observable<Ong> {
    return this.http.post<Ong>(`${this.apiUrl}/admin/ongs`, request);
  }
}
