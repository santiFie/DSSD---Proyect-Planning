import { Routes } from '@angular/router';
import { ProjectListComponent } from './components/project-list/project-list.component';
import { ProjectFormComponent } from './components/project-form/project-form.component';
import { PedidoListComponent } from './components/pedido-list/pedido-list.component';
import { CompromisoFormComponent } from './components/compromiso-form/compromiso-form.component';
import { LoginComponent } from './components/login/login.component';
import { UserListComponent } from './components/user-list/user-list.component';
import { OngListComponent } from './components/ong-list/ong-list.component';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { 
    path: '', 
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: '/projects', pathMatch: 'full' },
      { path: 'projects', component: ProjectListComponent },
      { path: 'projects/new', component: ProjectFormComponent },
      { path: 'projects/edit/:id', component: ProjectFormComponent },
      { path: 'pedidos', component: PedidoListComponent },
      { path: 'compromisos/new/:id', component: CompromisoFormComponent },
      {
        path: 'admin',
        canActivate: [adminGuard],
        children: [
          { path: 'users', component: UserListComponent },
          { path: 'ongs', component: OngListComponent }
        ]
      }
    ]
  },
  { path: '**', redirectTo: '/projects' }
];
