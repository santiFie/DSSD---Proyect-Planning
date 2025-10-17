import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Pedido } from '../../models/pedido.model';
import { PedidoService } from '../../services/pedido.service';

@Component({
  selector: 'app-pedido-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './pedido-list.component.html',
  styleUrls: ['./pedido-list.component.scss']
})
export class PedidoListComponent implements OnInit {
  pedidos: Pedido[] = [];
  loading = false;
  error: string | null = null;

  constructor(private pedidoService: PedidoService) { }

  ngOnInit(): void {
    this.loadPedidos();
  }

  loadPedidos(): void {
    this.loading = true;
    this.error = null;
    this.pedidoService.getAllPedidos().subscribe({
      next: data => { this.pedidos = data; this.loading = false; },
      error: err => { this.error = 'Error cargando pedidos'; console.error(err); this.loading = false; }
    });
  }

  brindarAyuda(p: Pedido): void {
    // Redirigir al formulario de compromiso
    console.log('Redirigiendo al formulario de compromiso para pedido:', p.id);
    // Aquí podrías implementar la navegación si tienes Router inyectado
    // this.router.navigate(['/compromisos/new', p.id]);
  }
}
