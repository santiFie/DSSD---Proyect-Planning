import { Component, OnInit } from '@angular/core';
import { forkJoin, map } from 'rxjs';
import { IndicadorEtapaPedido } from 'src/app/models/indicador.etapa.pedido.model';
import { IndicadoresService } from 'src/app/services/indicadores.service';
import { OngService } from 'src/app/services/ong.service';

@Component({
  selector: 'app-indicadores',
  standalone: true,
  imports: [],
  templateUrl: './indicadores.component.html'
})
export class IndicadoresComponent implements OnInit {

  proyectosEnTermino: number = 52;
  proyectosFueraTermino: number = 48;
  ongMasColaboradora: string = 'ONG Dinero';
  rubroMasSolicitado: string = 'Material';
  top3Ongs: string[] = ['ONG Dinero', 'ONG Voluntarios', 'ONG Ayuda'];

  constructor(
    private indicadorSvc: IndicadoresService,
    private ongSvc: OngService
  ) { }

  ngOnInit(): void {
    this.indicadorSvc.getProjectEndDates().subscribe({
      next: (list) => {
        const totalProyectos = list.length;
        this.proyectosEnTermino = Math.round(list.filter(item => item.endDateCase <= item.endDateProject).length * 100 / totalProyectos);
        this.proyectosFueraTermino = Math.round(list.filter(item => item.endDateCase > item.endDateProject).length * 100 / totalProyectos);
        console.log('Indicadores de Fechas de Finalización de Proyectos:', list);
      },
      error: (error) => {
        console.error('Error al obtener los indicadores de fechas de finalización de proyectos:', error);
      }
    });
    this.ongMasColaboradora = 'N/A';
    this.rubroMasSolicitado = 'N/A';
    this.top3Ongs = [];
    this.indicadorSvc.getStagesAndCommitments().subscribe({
      next: (list) => {
        const ongCount = list.reduce((acc, item) => {
          acc[item.ongColaboranteId] = (acc[item.ongColaboranteId] || 0) + 1;
          return acc;
        }, {} as Record<number, number>);
        const ongMasFrecuente = Number(Object.entries(ongCount)
          .sort((a, b) => b[1] - a[1])[0][0]);
        this.ongSvc.findById(ongMasFrecuente).subscribe(ong => {
          this.ongMasColaboradora = ong?.name ?? 'ONG Desconocida';
        });

        const agrupadoPorCategoria = list.reduce((acc, item) => {
          acc[item.categoryStage] = acc[item.categoryStage] || [];
          acc[item.categoryStage].push(item);
          return acc;
        }, {} as Record<string, IndicadorEtapaPedido[]>);

        this.rubroMasSolicitado = Object.entries(agrupadoPorCategoria)
          .sort((a, b) => b[1].length - a[1].length)[0][0];

        const listaTopCategoria = agrupadoPorCategoria[this.rubroMasSolicitado];
        const conteoONG = listaTopCategoria.reduce((acc, item) => {
          acc[item.ongColaboranteId] = (acc[item.ongColaboranteId] || 0) + 1;
          return acc;
        }, {} as Record<number, number>);

        const top3Observables = Object.entries(conteoONG)
          .sort((a, b) => b[1] - a[1])
          .slice(0, 3)
          .map(([ongId]) => {
            const id = Number(ongId);
            return this.ongSvc.findById(id).pipe(
              map(ong => ong?.name ?? "ONG Desconocida")  // devuelve string
            );
          });

        forkJoin(top3Observables).subscribe(result => {
          this.top3Ongs = result;  // result es string[]
        });
        console.log('Indicadores de Etapas y Pedidos:', list);
      },
      error: (error) => {
        console.error('Error al obtener los indicadores de etapas y pedidos:', error);
      }
    });
  }
}
