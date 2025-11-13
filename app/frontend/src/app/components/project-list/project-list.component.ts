import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProjectService } from '../../services/project.service';
import { ObservacionService } from '../../services/observacion.service';
import { OngService } from '../../services/ong.service';
import { AuthService } from '../../services/auth.service';
import { Project } from '../../models/project.model';
import { Ong } from '../../models/ong.model';
import { CreateObservacionRequest } from '../../models/observacion.model';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss']
})
export class ProjectListComponent implements OnInit {
  projects: Project[] = [];
  ongs: Ong[] = [];
  loading = true;
  error: string | null = null;
  successMessage: string | null = null;
  
  // Modal para crear observación
  showObservacionModal = false;
  selectedProject: Project | null = null;
  loadingObservacion = false;
  
  newObservacion: CreateObservacionRequest = {
    descripcion: '',
    proyectoId: 0,
    ongId: 0
  };

  constructor(
    private projectService: ProjectService,
    private observacionService: ObservacionService,
    private ongService: OngService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProjects();
    this.loadOngs();
  }

  loadProjects(): void {
    this.loading = true;
    this.error = null;
    
    this.projectService.getAllProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading projects:', error);
        this.error = 'Error al cargar los proyectos. Por favor, intenta de nuevo.';
        this.loading = false;
      }
    });
  }

  loadOngs(): void {
    this.ongService.getAllOngs().subscribe({
      next: (ongs) => {
        this.ongs = ongs;
      },
      error: (error) => {
        console.error('Error loading ONGs:', error);
      }
    });
  }

  deleteProject(id: number): void {
    if (confirm('¿Estás seguro de que quieres eliminar este proyecto?')) {
      this.projectService.deleteProject(id).subscribe({
        next: () => {
          this.loadProjects(); // Reload the list
        },
        error: (error) => {
          console.error('Error deleting project:', error);
          this.error = 'Error al eliminar el proyecto.';
        }
      });
    }
  }

  canCreateObservacion(): boolean {
    const user = this.authService.getCurrentUser();
    return user?.role === 'ADMIN' || user?.role === 'DIRECTIVO';
  }

  openObservacionModal(project: Project): void {
    this.selectedProject = project;
    this.newObservacion = {
      descripcion: '',
      proyectoId: project.id || 0,
      ongId: project.ongOriginante || 0  // Establecer automáticamente la ONG del proyecto
    };
    this.showObservacionModal = true;
    this.error = null;
    this.successMessage = null;
  }

  closeObservacionModal(): void {
    this.showObservacionModal = false;
    this.selectedProject = null;
    this.newObservacion = {
      descripcion: '',
      proyectoId: 0,
      ongId: 0
    };
  }

  createObservacion(): void {
    if (!this.newObservacion.descripcion) {
      this.error = 'Por favor ingrese la descripción de la observación';
      return;
    }

    if (!this.newObservacion.ongId) {
      this.error = 'No se pudo determinar la ONG del proyecto';
      return;
    }

    this.loadingObservacion = true;
    this.error = null;
    this.successMessage = null;

    this.observacionService.createObservacion(this.newObservacion).subscribe({
      next: (observacion) => {
        this.successMessage = `Observación creada exitosamente para el proyecto "${this.selectedProject?.name}". Plazo: 5 días.`;
        this.loadingObservacion = false;
        
        // Cerrar modal después de 2 segundos
        setTimeout(() => {
          this.closeObservacionModal();
          this.successMessage = null;
        }, 2000);
      },
      error: (error) => {
        console.error('Error al crear observación', error);
        this.error = error.error?.message || 'Error al crear la observación';
        this.loadingObservacion = false;
      }
    });
  }
  
  getOngNameForObservacion(): string {
    if (!this.newObservacion.ongId) {
      return 'ONG del proyecto';
    }
    
    const ong = this.ongs.find(o => o.id === this.newObservacion.ongId);
    return ong?.name || 'ONG del proyecto';
  }
}