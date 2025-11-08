import { Component, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Pedido } from '../../models/pedido.model';
import { ActivatedRoute, Router } from '@angular/router';
import { NewCompromisoDto } from '../../models/compromiso.model';
import { PedidoService } from '../../services/pedido.service';
<<<<<<< HEAD
import { AuthService } from '../../services/auth.service';

=======
import { finalize } from 'rxjs';
>>>>>>> branch-eze

@Component({
  selector: 'app-compromiso-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compromiso-form.component.html',
  styleUrls: ['./compromiso-form.component.scss']
})
export class CompromisoFormComponent implements OnInit {

<<<<<<< HEAD
    pedidoId: number = -1;
    
    compromiso: NewCompromisoDto = {
      pedidoId: 0,
      ongColaboranteId: 0,
      descripcion: '',
      fechaCompromiso: '',
      estado: 'PENDIENTE', // Este es el estado inicial del compromiso
      version: 1
    };

    // estados posibles ['PENDIENTE', 'EN_PROGRESO', 'COMPLETADO', 'CANCELADO'];

    private ongId = -1;

    isSubmitting = false;
=======
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
>>>>>>> branch-eze

  constructor(
    private pedidoService: PedidoService,
    private route: ActivatedRoute,
    private router: Router,
<<<<<<< HEAD
    private authService: AuthService // Inyectar AuthService

    ) { }

  ngOnInit(): void {
    this.pedidoId = this.route.snapshot.params['id'];
    this.compromiso.pedidoId = this.pedidoId;
    
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        // Asignar el ID de la ONG colaborante desde el usuario logueado
        this.compromiso.ongColaboranteId = user.ongId;
      }
    });

  }

  onSubmit(): void {
    if (this.pedidoId === -1) {
      console.error('Pedido ID no válido');
      return;
=======
    private readonly location: Location
  ) { }

  ngOnInit(): void {
    const state = this.location.getState() as { pedido: Pedido };
    this.pedido = state.pedido;
    this.compromiso.pedido = this.pedido;
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
>>>>>>> branch-eze
    }
  }

  onCancel(): void {
    // Regresar a la lista de pedidos o página anterior
    this.router.navigate(['/pedidos']);
  }
}