import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OngService } from '../../services/ong.service';
import { Ong, CreateOngRequest } from '../../models/ong.model';

@Component({
  selector: 'app-ong-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ong-list.component.html',
  styleUrls: ['./ong-list.component.scss']
})
export class OngListComponent implements OnInit {
  ongs: Ong[] = [];
  showCreateForm = false;
  loading = false;
  errorMessage = '';
  successMessage = '';

  newOng: CreateOngRequest = {
    name: '',
    originCountry: ''
  };

  constructor(private ongService: OngService) {}

  ngOnInit(): void {
    this.loadOngs();
  }

  loadOngs(): void {
    this.ongService.getAllOngs().subscribe({
      next: (ongs) => {
        this.ongs = ongs;
      },
      error: (error) => {
        console.error('Error al cargar ONGs', error);
        this.errorMessage = 'Error al cargar la lista de ONGs';
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

  createOng(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.loading = true;

    this.ongService.createOng(this.newOng).subscribe({
      next: (ong) => {
        this.successMessage = `ONG ${ong.name} creada exitosamente`;
        this.loadOngs();
        this.resetForm();
        this.showCreateForm = false;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al crear ONG', error);
        this.errorMessage = error.error?.message || 'Error al crear la ONG';
        this.loading = false;
      }
    });
  }

  resetForm(): void {
    this.newOng = {
      name: '',
      originCountry: ''
    };
  }
}
