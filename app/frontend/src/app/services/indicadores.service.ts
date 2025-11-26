import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { IndicadorProyectoFechaFin } from "../models/indicador.proyecto.fecfin.modelo";
import { IndicadorEtapaPedido } from "../models/indicador.etapa.pedido.model";

@Injectable({
    providedIn: 'root'
})
export class IndicadoresService {
    private apiUrl = 'http://localhost:8081/api/indicadores';

    constructor(private http: HttpClient) { }

    getProjectEndDates(): Observable<IndicadorProyectoFechaFin[]> {
        return this.http.get<IndicadorProyectoFechaFin[]>(`${this.apiUrl}/proyecto-fecha-finalizacion`);
    }

    getStagesAndCommitments(): Observable<IndicadorEtapaPedido[]> {
        return this.http.get<IndicadorEtapaPedido[]>(`${this.apiUrl}/etapa-compromiso`);
    }
}