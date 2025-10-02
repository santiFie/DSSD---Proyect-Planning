import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ProjectService } from '../../services/project.service';
import { NewProjectDto, Stage } from '../../models/project.model';

@Component({
  selector: 'app-project-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './project-form.component.html',
  styleUrls: ['./project-form.component.scss']
})
export class ProjectFormComponent implements OnInit {
  project: NewProjectDto = {
    name: '',
    description: '',
    startDate: '',
    endDate: '',
    stages: []
  };
  
  isEdit = false;
  projectId: number | null = null;
  loading = false;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private projectService: ProjectService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.projectId = +id;
      this.loadProject(this.projectId);
    }
  }

  loadProject(id: number): void {
    this.loading = true;
    this.projectService.getProjectById(id).subscribe({
      next: (project) => {
        this.project = {
          name: project.name,
          description: project.description,
          startDate: project.startDate,
          endDate: project.endDate,
          stages: project.stages || []
        };
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading project:', error);
        this.error = 'Error al cargar el proyecto.';
        this.loading = false;
      }
    });
  }

  addStage(): void {
    const newStage: Stage = {
      name: '',
      needs: null,
      covered: false,
      startDate: this.project.startDate || '',
      endDate: ''
    };
    this.project.stages.push(newStage);
  }

  removeStage(index: number): void {
    if (index >= 0 && index < this.project.stages.length) {
      this.project.stages.splice(index, 1);
    }
  }

  toggleCovered(stage: Stage) {
    if (stage.covered) {
      stage.needs = null; // borra la necesidad si está cubierta
    }
  }

  onSubmit(): void {
    if (this.loading) return;
    
    this.loading = true;
    this.error = null;
    this.success = null;

    const operation = this.isEdit 
      ? this.projectService.updateProject(this.projectId!, this.project)
      : this.projectService.createProject(this.project);

    operation.subscribe({
      next: (result) => {
        this.success = `Proyecto ${this.isEdit ? 'actualizado' : 'creado'} exitosamente.`;
        this.loading = false;
        
        // Redirect after a short delay
        setTimeout(() => {
          this.router.navigate(['/projects']);
        }, 1500);
      },
      error: (err) => {
        console.error('Error saving project:', err);

        if (err.error && err.error.message) {
          this.error = err.error.message;
        } else if (err.message) {
          this.error = err.message;
        } else {
          this.error = `Error al ${this.isEdit ? 'actualizar' : 'crear'} el proyecto.`;
        }

        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/projects']);
  }
}