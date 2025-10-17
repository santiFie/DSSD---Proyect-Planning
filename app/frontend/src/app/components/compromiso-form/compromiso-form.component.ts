import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Pedido } from '../../models/pedido.model';
import { ActivatedRoute, Router } from '@angular/router';
import { NewCompromisoDto } from '../../models/compromiso.model';
import { PedidoService } from '../../services/pedido.service';

@Component({
  selector: 'app-compromiso-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compromiso-form.component.html',
  styleUrls: ['./compromiso-form.component.scss']
})
export class CompromisoFormComponent implements OnInit {

    pedidoId: number = -1;
    
    compromiso: NewCompromisoDto = {
      pedidoId: 0,
      ongColaboranteId: 0,
      descripcion: '',
      fechaCompromiso: '',
      estado: 'PENDIENTE',
      version: 1
    };

    estados = ['PENDIENTE', 'EN_PROGRESO', 'COMPLETADO', 'CANCELADO'];
    isSubmitting = false;

  constructor(
    private pedidoService: PedidoService, 
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.pedidoId = this.route.snapshot.params['id'];
    this.compromiso.pedidoId = this.pedidoId;
  }

  onSubmit(): void {
    if (this.pedidoId === -1) {
        console.error('Pedido ID no válido');
        return;
    }
    
    this.isSubmitting = true;
    // Creo que no hace falta: this.compromiso.pedidoId = this.pedidoId;
    
    console.log('Creando compromiso para el pedido ID:', this.pedidoId, this.compromiso);
    this.pedidoService.getPedidoById(this.pedidoId).subscribe({
      next: (pedido) => {
        this.pedidoService.createCompromiso(this.pedidoId, this.compromiso, pedido.proyectoId).subscribe({
          next: (response) => {
            console.log('Compromiso creado exitosamente:', response);
            this.router.navigate(['/pedidos']);
          },
          error: (error) => {
            console.error('Error al crear el compromiso:', error);
            alert('Error al crear el compromiso. Por favor intente nuevamente.');
          },
          complete: () => {
            this.isSubmitting = false;
          }
        });
      }
    });
  }

  onCancel(): void {
    // Regresar a la lista de pedidos o página anterior
    this.router.navigate(['/pedidos']);
  }
}