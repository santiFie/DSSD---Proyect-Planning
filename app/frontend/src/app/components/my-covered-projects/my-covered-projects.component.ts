import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProjectService } from '../../services/project.service';
import { PedidoService } from '../../services/pedido.service';
import { Project } from '../../models/project.model';
import { Pedido } from '../../models/pedido.model';
import {FormsModule} from "@angular/forms";

interface ProjectWithStages extends Project {
  pedidos?: Pedido[];
  showStages?: boolean;
  loadingStages?: boolean;
  showCloseForm?: boolean;
  closeDescriptionInput: string;
  isClosed?: boolean;
}

@Component({
  selector: 'app-my-projects',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './my-covered-projects.component.html',
  styleUrls: ['./my-covered-projects.component.scss']
})
export class MyCoveredProjectsComponent implements OnInit {
  projects: ProjectWithStages[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private projectService: ProjectService
  ) {}

  ngOnInit(): void {
    this.loadMyCoveredProjects();
  }

  loadMyCoveredProjects(): void {
    this.loading = true;
    this.error = null;
    
    this.projectService.getMyCoveredProjects().subscribe({
      next: (projects) => {
        this.projects = projects.map(p => {
          const allStagesExecuted = p.stages?.length
              ? p.stages.every(s => s.executed === true)
              : false;

          return {
            ...p,
            pedidos: [],
            showStages: false,
            loadingStages: false,
            showCloseForm: false,
            closeDescriptionInput: '',
            isClosed: allStagesExecuted
          };
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading my covered projects:', error);
        this.error = 'Error al cargar tus proyectos cubiertos. Por favor, intenta de nuevo.';
        this.loading = false;
      }
    });
  }

  toggleStages(project: ProjectWithStages): void {
    if (!project.showStages && !project.stages?.length) {
      // Cargar pedidos si no están cargados
      this.loadStages(project);
    }
    project.showStages = !project.showStages;
  }

  loadStages(project: ProjectWithStages): void {
    if (!project.id) return;
    
    project.loadingStages = true;
    this.projectService.getStagesByProjectId(project.id).subscribe({
      next: (stages) => {
        project.stages = stages;
        project.loadingStages = false;
      },
      error: (error) => {
        console.error('Error loading stages:', error);
        project.stages = [];
        project.loadingStages = false;
      }
    });
  }

  getFinishStateClass(endDate?: string): string {
    if (endDate) {
      return 'terminada';
    } else {
      return 'pendiente';
    }
  }

  executeStage(project: ProjectWithStages, stageId?: number): void {
    if (!project.id || !stageId) return;

    this.projectService.executeStageByProjectId(project.id, stageId).subscribe({
      next: (updatedStage) => {
        console.log('✅ Etapa ejecutada con éxito:', updatedStage);
        // Podés actualizar el estado local si querés reflejar cambios en la UI
        const stageIndex = project.stages.findIndex(s => s.id === stageId);
        if (stageIndex !== -1) {
          project.stages[stageIndex] = updatedStage;
        }
      },
      error: (err) => {
        console.error('❌ Error al ejecutar la etapa:', err);
        alert('Ocurrió un error al ejecutar la etapa.');
      },
      complete: () => {
        window.location.reload();
      }
    });
  }

  finalizeProject(project: ProjectWithStages): void {
    if (!project.id) return;

    const desc = project.closeDescriptionInput?.trim();
    if (!desc) {
      alert("Por favor, ingresa una descripción para cerrar el proyecto.");
      return;
    }

    console.log("Finalizando proyecto:", project.id, "con descripción:", desc);

    this.projectService.finalizeProject(project.id, desc).subscribe({
      next: () => {
        alert("Proyecto finalizado con éxito");
        this.loadMyCoveredProjects();
      },
      error: (err) => {
        console.error("Error al finalizar proyecto:", err);
        alert("Error al finalizar proyecto");
      }
    });
  }

}
