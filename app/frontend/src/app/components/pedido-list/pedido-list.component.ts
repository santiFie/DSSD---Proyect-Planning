import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Pedido } from '../../models/pedido.model';
import { PedidoService } from '../../services/pedido.service';

@Component({
  selector: 'app-pedido-list',
  standalone: true,
  imports: [CommonModule],
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
    // optimistically mark as helping
    this.pedidoService.helpPedido(p.id).subscribe({
      next: res => {
        console.log('Ayuda brindada', res);
        // optionally refresh list or update status locally
        this.loadPedidos();
      },
      error: err => { console.error('Error al brindar ayuda', err); alert('Error al brindar ayuda'); }
    });
  }
}
