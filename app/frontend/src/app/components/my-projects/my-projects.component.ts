import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProjectService } from '../../services/project.service';
import { PedidoService } from '../../services/pedido.service';
import { Project } from '../../models/project.model';
import { Pedido } from '../../models/pedido.model';

interface ProjectWithPedidos extends Project {
  pedidos?: Pedido[];
  showPedidos?: boolean;
  loadingPedidos?: boolean;
}

@Component({
  selector: 'app-my-projects',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './my-projects.component.html',
  styleUrls: ['./my-projects.component.scss']
})
export class MyProjectsComponent implements OnInit {
  projects: ProjectWithPedidos[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private projectService: ProjectService,
    private pedidoService: PedidoService
  ) {}

  ngOnInit(): void {
    this.loadMyProjects();
  }

  loadMyProjects(): void {
    this.loading = true;
    this.error = null;
    
    this.projectService.getMyProjects().subscribe({
      next: (projects) => {
        this.projects = projects.map(p => ({
          ...p,
          pedidos: [],
          showPedidos: false,
          loadingPedidos: false
        }));
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading my projects:', error);
        this.error = 'Error al cargar tus proyectos. Por favor, intenta de nuevo.';
        this.loading = false;
      }
    });
  }

  togglePedidos(project: ProjectWithPedidos): void {
    if (!project.showPedidos && !project.pedidos?.length) {
      // Cargar pedidos si no están cargados
      this.loadPedidos(project);
    }
    project.showPedidos = !project.showPedidos;
  }

  loadPedidos(project: ProjectWithPedidos): void {
    if (!project.id) return;
    
    project.loadingPedidos = true;
    this.pedidoService.getPedidosByProyecto(project.id).subscribe({
      next: (pedidos) => {
        project.pedidos = pedidos;
        project.loadingPedidos = false;
      },
      error: (error) => {
        console.error('Error loading pedidos:', error);
        project.pedidos = [];
        project.loadingPedidos = false;
      }
    });
  }

  getEstadoClass(estado?: string): string {
    switch (estado?.toLowerCase()) {
      case 'pendiente':
        return 'estado-pendiente';
      case 'en_proceso':
      case 'en proceso':
        return 'estado-en-proceso';
      case 'completado':
        return 'estado-completado';
      default:
        return 'estado-default';
    }
  }
}
