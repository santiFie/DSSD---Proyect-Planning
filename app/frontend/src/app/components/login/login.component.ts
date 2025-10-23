import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  credentials: LoginRequest = {
    username: '',
    password: ''
  };
  errorMessage = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    // Si ya está autenticado, redirigir
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/']);
    }
  }

  login(): void {
    this.errorMessage = '';
    this.loading = true;

    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        console.log('Login exitoso', response);
        this.router.navigate(['/projects']);
      },
      error: (error) => {
        console.error('Error en login', error);
        this.errorMessage = 'Usuario o contraseña incorrectos';
        this.loading = false;
      }
    });
  }
}
