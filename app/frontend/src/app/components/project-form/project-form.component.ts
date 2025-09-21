import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ProjectService } from '../../services/project.service';
import { NewProjectDto } from '../../models/project.model';

@Component({
  selector: 'app-project-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="project-form">
      <h2>{{ isEdit ? 'Editar Proyecto' : 'Nuevo Proyecto' }}</h2>
      
      <div *ngIf="error" class="alert alert-error">
        {{ error }}
      </div>
      
      <div *ngIf="success" class="alert alert-success">
        {{ success }}
      </div>
      
      <form (ngSubmit)="onSubmit()" #projectForm="ngForm">
        <div class="form-group">
          <label for="name">Nombre del Proyecto *</label>
          <input
            type="text"
            id="name"
            name="name"
            [(ngModel)]="project.name"
            required
            #name="ngModel"
            class="form-control"
            placeholder="Ingresa el nombre del proyecto"
          >
          <div *ngIf="name.invalid && name.touched" class="error-message">
            El nombre es obligatorio.
          </div>
        </div>
        
        <div class="form-group">
          <label for="description">Descripción *</label>
          <textarea
            id="description"
            name="description"
            [(ngModel)]="project.description"
            required
            #description="ngModel"
            class="form-control"
            rows="4"
            placeholder="Describe el proyecto"
          ></textarea>
          <div *ngIf="description.invalid && description.touched" class="error-message">
            La descripción es obligatoria.
          </div>
        </div>
        
        <div class="form-group">
          <label for="startDate">Fecha de Inicio *</label>
          <input
            type="date"
            id="startDate"
            name="startDate"
            [(ngModel)]="project.startDate"
            required
            #startDate="ngModel"
            class="form-control"
          >
          <div *ngIf="startDate.invalid && startDate.touched" class="error-message">
            La fecha de inicio es obligatoria.
          </div>
        </div>
        
        <div class="form-group">
          <label for="endDate">Fecha de Fin *</label>
          <input
            type="date"
            id="endDate"
            name="endDate"
            [(ngModel)]="project.endDate"
            required
            #endDate="ngModel"
            class="form-control"
            [min]="project.startDate"
          >
          <div *ngIf="endDate.invalid && endDate.touched" class="error-message">
            La fecha de fin es obligatoria.
          </div>
        </div>
        
        <div class="form-actions">
          <button 
            type="submit" 
            class="btn btn-primary" 
            [disabled]="!projectForm.form.valid || loading"
          >
            {{ loading ? 'Guardando...' : (isEdit ? 'Actualizar' : 'Crear') }} Proyecto
          </button>
          
          <button 
            type="button" 
            class="btn btn-secondary" 
            (click)="goBack()"
          >
            Cancelar
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .project-form {
      max-width: 600px;
      margin: 0 auto;
      padding: 20px 0;
    }
    
    .form-control {
      width: 100%;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
    }
    
    .form-control:focus {
      outline: none;
      border-color: #007bff;
      box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
    }
    
    .error-message {
      color: #dc3545;
      font-size: 12px;
      margin-top: 5px;
    }
    
    .form-actions {
      margin-top: 30px;
      display: flex;
      gap: 10px;
    }
    
    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  `]
})
export class ProjectFormComponent implements OnInit {
  project: NewProjectDto = {
    name: '',
    description: '',
    startDate: '',
    endDate: ''
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
          endDate: project.endDate
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
      error: (error) => {
        console.error('Error saving project:', error);
        this.error = `Error al ${this.isEdit ? 'actualizar' : 'crear'} el proyecto.`;
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/projects']);
  }
}