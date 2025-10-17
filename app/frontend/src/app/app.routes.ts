import { Routes } from '@angular/router';
import { ProjectListComponent } from './components/project-list/project-list.component';
import { ProjectFormComponent } from './components/project-form/project-form.component';
import { PedidoListComponent } from './components/pedido-list/pedido-list.component';
import { CompromisoFormComponent } from './components/compromiso-form/compromiso-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/projects', pathMatch: 'full' },
  { path: 'projects', component: ProjectListComponent },
  { path: 'projects/new', component: ProjectFormComponent },
  { path: 'projects/edit/:id', component: ProjectFormComponent },
  { path: 'pedidos', component: PedidoListComponent },
  { path: 'compromisos/new/:id', component: CompromisoFormComponent }, // Id del pedido
  { path: '**', redirectTo: '/projects' }
];