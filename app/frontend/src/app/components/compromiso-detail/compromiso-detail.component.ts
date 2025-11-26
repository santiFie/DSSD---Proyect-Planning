import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PedidoService } from '../../services/pedido.service';
import { Compromiso } from '../../models/compromiso.model';
import { Pedido } from '../../models/pedido.model';

@Component({
  selector: 'app-compromiso-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './compromiso-detail.component.html',
  styleUrls: ['./compromiso-detail.component.scss']
})
export class CompromisoDetailComponent implements OnInit {
  compromisos: Compromiso[] = [];
  pedido: Pedido | null = null;
  pedidoId: number = 0;
  proyectoId: number = 0;
  loading = true;
  error: string | null = null;
  successMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pedidoService: PedidoService
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.pedidoId = +params['pedidoId'];
      this.proyectoId = +params['proyectoId'];
      this.loadPedido();
      this.loadCompromisos();
    });
  }

  loadPedido(): void {
    this.pedidoService.getPedidoById(this.pedidoId).subscribe({
      next: (pedido) => {
        this.pedido = pedido;
      },
      error: (error) => {
        console.error('Error loading pedido:', error);
      }
    });
  }

  loadCompromisos(): void {
    this.loading = true;
    this.error = null;
    this.successMessage = null;

    this.pedidoService.getCompromisosByPedidoId(this.pedidoId).subscribe({
      next: (compromisos) => {
        this.compromisos = compromisos;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading compromisos:', error);
        this.error = 'Error al cargar los compromisos. Por favor, intenta de nuevo.';
        this.loading = false;
      }
    });
  }

  aceptarCompromiso(compromiso: Compromiso): void {
    if (!confirm('¿Estás seguro de que quieres aceptar este compromiso?')) {
      return;
    }

    this.pedidoService.aceptarCompromiso(this.pedidoId, compromiso.id, this.proyectoId).subscribe({
      next: () => {
        this.successMessage = 'Compromiso aceptado exitosamente';
        compromiso.estado = 'ACEPTADO';
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (error) => {
        console.error('Error accepting compromiso:', error);
        this.error = 'Error al aceptar el compromiso: ' + (error.error?.message || error.message);
        setTimeout(() => this.error = null, 5000);
      }
    });
  }

  rechazarCompromiso(compromiso: Compromiso): void {
    if (!confirm('¿Estás seguro de que quieres rechazar este compromiso?')) {
      return;
    }

    this.pedidoService.rechazarCompromiso(this.pedidoId, compromiso.id, this.proyectoId).subscribe({
      next: () => {
        this.successMessage = 'Compromiso rechazado exitosamente';
        compromiso.estado = 'RECHAZADO';
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (error) => {
        console.error('Error rejecting compromiso:', error);
        this.error = 'Error al rechazar el compromiso: ' + (error.error?.message || error.message);
        setTimeout(() => this.error = null, 5000);
      }
    });
  }

  getEstadoClass(estado: string): string {
    switch (estado?.toUpperCase()) {
      case 'PENDIENTE':
        return 'estado-pendiente';
      case 'ACEPTADO':
        return 'estado-aceptado';
      case 'RECHAZADO':
        return 'estado-rechazado';
      default:
        return 'estado-default';
    }
  }

  canManageCompromiso(compromiso: Compromiso): boolean {
    // Solo se pueden gestionar compromisos en estado PENDIENTE
    return compromiso.estado === 'PENDIENTE';
  }

  volver(): void {
    this.router.navigate(['/my-projects']);
  }
}
