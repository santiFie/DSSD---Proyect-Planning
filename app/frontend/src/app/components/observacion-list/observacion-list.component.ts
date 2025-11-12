import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ObservacionService } from '../../services/observacion.service';
import { ProjectService } from '../../services/project.service';
import { OngService } from '../../services/ong.service';
import { AuthService } from '../../services/auth.service';
import { 
  Observacion, 
  CreateObservacionRequest 
} from '../../models/observacion.model';
import { Project } from '../../models/project.model';
import { Ong } from '../../models/ong.model';

@Component({
  selector: 'app-observacion-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './observacion-list.component.html',
  styleUrls: ['./observacion-list.component.scss']
})
export class ObservacionListComponent implements OnInit {
  observaciones: Observacion[] = [];
  proyectos: Project[] = [];
  ongs: Ong[] = [];
  showCreateForm = false;
  loading = false;
  errorMessage = '';
  successMessage = '';
  isDirectivo = false;
  currentUserOngId: number | null = null;

  newObservacion: CreateObservacionRequest = {
    descripcion: '',
    proyectoId: 0,
    ongId: 0
  };

  constructor(
    private observacionService: ObservacionService,
    private projectService: ProjectService,
    private ongService: OngService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.isDirectivo = this.authService.isDirectivo();
    const currentUser = this.authService.getCurrentUser();
    this.currentUserOngId = currentUser?.ongId || null;

    this.loadData();
  }

  loadData(): void {
    if (this.isDirectivo) {
      // Si es directivo, cargar todas las observaciones y proyectos
      this.loadAllObservaciones();
      this.loadProyectos();
      this.loadOngs();
    } else if (this.currentUserOngId) {
      // Si es usuario normal, cargar solo las observaciones de su ONG
      this.loadObservacionesByOng(this.currentUserOngId);
    }
  }

  loadAllObservaciones(): void {
    // Por ahora, cargamos observaciones por proyecto
    // Podrías agregar un endpoint para obtener todas
    this.loadProyectos();
  }

  loadObservacionesByOng(ongId: number): void {
    this.observacionService.getObservacionesByOng(ongId).subscribe({
      next: (observaciones) => {
        this.observaciones = observaciones;
      },
      error: (error) => {
        console.error('Error al cargar observaciones', error);
        this.errorMessage = 'Error al cargar las observaciones';
      }
    });
  }

  loadProyectos(): void {
    this.projectService.getAllProjects().subscribe({
      next: (proyectos) => {
        this.proyectos = proyectos;
      },
      error: (error) => {
        console.error('Error al cargar proyectos', error);
      }
    });
  }

  loadOngs(): void {
    this.ongService.getAllOngs().subscribe({
      next: (ongs) => {
        this.ongs = ongs;
      },
      error: (error) => {
        console.error('Error al cargar ONGs', error);
      }
    });
  }

  loadObservacionesByProyecto(proyectoId: number): void {
    if (!proyectoId) return;
    
    this.observacionService.getObservacionesByProyecto(proyectoId).subscribe({
      next: (observaciones) => {
        this.observaciones = observaciones;
      },
      error: (error) => {
        console.error('Error al cargar observaciones del proyecto', error);
        this.errorMessage = 'Error al cargar las observaciones del proyecto';
      }
    });
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.showCreateForm) {
      this.resetForm();
    }
  }

  createObservacion(): void {
    if (!this.newObservacion.descripcion || !this.newObservacion.proyectoId || !this.newObservacion.ongId) {
      this.errorMessage = 'Por favor complete todos los campos';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.loading = true;

    this.observacionService.createObservacion(this.newObservacion).subscribe({
      next: (observacion) => {
        this.successMessage = `Observación creada exitosamente. Plazo: 5 días.`;
        this.loadData();
        this.resetForm();
        this.showCreateForm = false;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al crear observación', error);
        this.errorMessage = error.error?.message || 'Error al crear la observación';
        this.loading = false;
      }
    });
  }

  verDetalles(observacionId: number): void {
    this.router.navigate(['/observaciones', observacionId]);
  }

  resolverObservacion(observacionId: number): void {
    if (!confirm('¿Está seguro de marcar esta observación como resuelta?')) {
      return;
    }

    this.observacionService.resolverObservacion(observacionId).subscribe({
      next: () => {
        this.successMessage = 'Observación marcada como resuelta';
        this.loadData();
      },
      error: (error) => {
        console.error('Error al resolver observación', error);
        this.errorMessage = 'Error al resolver la observación';
      }
    });
  }

  resetForm(): void {
    this.newObservacion = {
      descripcion: '',
      proyectoId: 0,
      ongId: 0
    };
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
