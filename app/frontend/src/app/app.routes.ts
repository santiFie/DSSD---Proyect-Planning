import { Routes } from '@angular/router';
import { ProjectListComponent } from './components/project-list/project-list.component';
import { ProjectFormComponent } from './components/project-form/project-form.component';
import { MyProjectsComponent } from './components/my-projects/my-projects.component';
import { PedidoListComponent } from './components/pedido-list/pedido-list.component';
import { CompromisoFormComponent } from './components/compromiso-form/compromiso-form.component';
import { CompromisoDetailComponent } from './components/compromiso-detail/compromiso-detail.component';
import { ObservacionListComponent } from './components/observacion-list/observacion-list.component';
import { ObservacionDetailComponent } from './components/observacion-detail/observacion-detail.component';
import { LoginComponent } from './components/login/login.component';
import { UserListComponent } from './components/user-list/user-list.component';
import { OngListComponent } from './components/ong-list/ong-list.component';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import {MyCoveredProjectsComponent} from "./components/my-covered-projects/my-covered-projects.component";
import { directivoGuard } from './guards/directivo.guard';
import { IndicadoresComponent } from './components/indicadores/indicadores.component';

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
      { path: 'my-projects', component: MyProjectsComponent },
      { path: 'my-projects/covered', component: MyCoveredProjectsComponent },
      { path: 'pedidos', component: PedidoListComponent },
      { path: 'pedidos/:pedidoId/compromisos/:proyectoId', component: CompromisoDetailComponent },
      { path: 'compromisos/new/:id', component: CompromisoFormComponent },
      { path: 'observaciones', component: ObservacionListComponent },
      { path: 'observaciones/:id', component: ObservacionDetailComponent },
      {
        path: 'admin',
        canActivate: [adminGuard],
        children: [
          { path: 'users', component: UserListComponent },
          { path: 'ongs', component: OngListComponent }
        ]
      },
      { path: 'indicadores', component: IndicadoresComponent }
    ]
  },
  { path: '**', redirectTo: '/projects' }
];
