import { Component, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Pedido } from '../../models/pedido.model';
import { Router } from '@angular/router';
import { NewCompromisoDto } from '../../models/compromiso.model';
import { PedidoService } from '../../services/pedido.service';
import { finalize } from 'rxjs';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-compromiso-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compromiso-form.component.html',
  styleUrls: ['./compromiso-form.component.scss']
})
export class CompromisoFormComponent implements OnInit {

  pedido: Pedido = { id: -1 };

  compromiso: NewCompromisoDto = {
    ongColaboranteId: 0,
    descripcion: '',
    fechaCompromiso: '',
    estado: 'PENDIENTE',
    version: 1,
    pedido: this.pedido
  };

  estados = ['PENDIENTE', 'EN_PROGRESO', 'COMPLETADO', 'CANCELADO'];
  isSubmitting = false;

  constructor(
    private pedidoService: PedidoService,
    private router: Router,
    private readonly location: Location,
    private authSvc: AuthService
  ) { }

  ngOnInit(): void {
    const state = this.location.getState() as { pedido: Pedido };
    this.pedido = state.pedido;
    this.compromiso.pedido = this.pedido;
    this.compromiso.ongColaboranteId = this.authSvc.getCurrentUser()?.ongId || 0;
  }

  onSubmit(): void {
    if (this.pedido?.id === -1) {
      console.error('Pedido ID no válido');
      return;
    }
    if (!this.isSubmitting) {
      this.isSubmitting = true;
      console.log('Creando compromiso para el pedido ID:', this.pedido.id, this.compromiso);
      this.pedidoService.createCompromiso(this.compromiso).pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (response) => {
          console.log('Compromiso creado exitosamente:', response);
          this.router.navigate(['/pedidos']);
        },
        error: (error) => {
          console.error('Error al crear el compromiso:', error);
          alert('Error al crear el compromiso. Por favor intente nuevamente.');
        }
      });
    }
  }

  onCancel(): void {
    // Regresar a la lista de pedidos o página anterior
    this.router.navigate(['/pedidos']);
  }
}