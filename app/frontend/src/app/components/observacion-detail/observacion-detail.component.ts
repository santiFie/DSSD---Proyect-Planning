import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ObservacionService } from '../../services/observacion.service';
import { AuthService } from '../../services/auth.service';
import { 
  Observacion, 
  Correccion, 
  CreateCorreccionRequest 
} from '../../models/observacion.model';

@Component({
  selector: 'app-observacion-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './observacion-detail.component.html',
  styleUrls: ['./observacion-detail.component.scss']
})
export class ObservacionDetailComponent implements OnInit {
  observacion: Observacion | null = null;
  correcciones: Correccion[] = [];
  showCorreccionForm = false;
  showRechazoForm = false;
  loading = false;
  errorMessage = '';
  successMessage = '';
  canAddCorreccion = false;
  isDirectivo = false;
  canApproveReject = false; // ADMIN o DIRECTIVO pueden aprobar/rechazar
  motivoRechazo = '';

  newCorreccion: CreateCorreccionRequest = {
    observacionId: 0,
    detalle: ''
  };

  constructor(
    private observacionService: ObservacionService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.isDirectivo = this.authService.isDirectivo();
    const currentUser = this.authService.getCurrentUser();
    
    // ADMIN o DIRECTIVO pueden aprobar/rechazar observaciones
    this.canApproveReject = this.authService.isAdmin() || this.authService.isDirectivo();
    
    const id = this.route.snapshot.params['id'];
    this.loadObservacion(id);
  }

  loadObservacion(id: number): void {
    this.observacionService.getObservacionById(id).subscribe({
      next: (observacion) => {
        this.observacion = observacion;
        this.correcciones = observacion.correcciones || [];
        this.newCorreccion.observacionId = observacion.id;
        
        // Verificar si el usuario puede agregar correcciones
        const currentUser = this.authService.getCurrentUser();
        this.canAddCorreccion = 
          currentUser?.ongId === observacion.ongId && 
          (observacion.estado === 'PENDIENTE' || observacion.estado === 'EN_REVISION');
      },
      error: (error) => {
        console.error('Error al cargar observación', error);
        this.errorMessage = 'Error al cargar la observación';
      }
    });
  }

  toggleCorreccionForm(): void {
    this.showCorreccionForm = !this.showCorreccionForm;
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.showCorreccionForm) {
      this.newCorreccion.detalle = '';
    }
  }

  createCorreccion(): void {
    if (!this.newCorreccion.detalle.trim()) {
      this.errorMessage = 'Por favor ingrese el detalle de la corrección';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.loading = true;

    this.observacionService.createCorreccion(this.newCorreccion).subscribe({
      next: (correccion) => {
        this.successMessage = 'Corrección registrada exitosamente';
        this.correcciones.push(correccion);
        if (this.observacion) {
          this.observacion.estado = 'EN_REVISION';
        }
        this.newCorreccion.detalle = '';
        this.showCorreccionForm = false;
        this.loading = false;
        
        // Recargar la observación para obtener el estado actualizado
        if (this.observacion) {
          this.loadObservacion(this.observacion.id);
        }
      },
      error: (error) => {
        console.error('Error al crear corrección', error);
        this.errorMessage = error.error?.message || 'Error al registrar la corrección';
        this.loading = false;
      }
    });
  }

  resolverObservacion(): void {
    if (!this.observacion) return;

    if (!confirm('¿Está seguro de marcar esta observación como resuelta?')) {
      return;
    }

    this.loading = true;
    this.observacionService.resolverObservacion(this.observacion.id).subscribe({
      next: (observacion) => {
        this.observacion = observacion;
        this.successMessage = 'Observación marcada como resuelta';
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al resolver observación', error);
        this.errorMessage = 'Error al resolver la observación';
        this.loading = false;
      }
    });
  }

  toggleRechazoForm(): void {
    this.showRechazoForm = !this.showRechazoForm;
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.showRechazoForm) {
      this.motivoRechazo = '';
    }
  }

  rechazarCorreccion(): void {
    if (!this.observacion) return;

    if (!this.motivoRechazo.trim()) {
      this.errorMessage = 'Por favor ingrese el motivo del rechazo';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.observacionService.rechazarCorreccion(this.observacion.id, this.motivoRechazo).subscribe({
      next: (observacion) => {
        this.observacion = observacion;
        this.successMessage = 'Corrección rechazada exitosamente';
        this.motivoRechazo = '';
        this.showRechazoForm = false;
        this.loading = false;
        // Recargar para obtener el comentario de rechazo
        this.loadObservacion(observacion.id);
      },
      error: (error) => {
        console.error('Error al rechazar corrección', error);
        this.errorMessage = error.error?.message || 'Error al rechazar la corrección';
        this.loading = false;
      }
    });
  }

  volver(): void {
    this.router.navigate(['/observaciones']);
  }

  getEstadoClass(estado: string): string {
    switch (estado) {
      case 'PENDIENTE':
        return 'badge-warning';
      case 'EN_REVISION':
        return 'badge-info';
      case 'RESUELTA':
        return 'badge-success';
      case 'RECHAZADA':
        return 'badge-danger';
      case 'VENCIDA':
        return 'badge-danger';
      default:
        return 'badge-secondary';
    }
  }

  getDiasRestantes(fechaLimite: string): number {
    const limite = new Date(fechaLimite);
    const hoy = new Date();
    const diff = limite.getTime() - hoy.getTime();
    return Math.ceil(diff / (1000 * 3600 * 24));
  }
}
