import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { OngService } from '../../services/ong.service';
import { UserResponse } from '../../models/user.model';
import { RegisterUserRequest } from '../../models/auth.model';
import { Ong } from '../../models/ong.model';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {
  users: UserResponse[] = [];
  ongs: Ong[] = [];
  showCreateForm = false;
  loading = false;
  errorMessage = '';
  successMessage = '';

  newUser: RegisterUserRequest = {
    username: '',
    password: '',
    email: '',
    role: 'USER',
    ongId: 0
  };

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private ongService: OngService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadOngs();
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
      },
      error: (error) => {
        console.error('Error al cargar usuarios', error);
        this.errorMessage = 'Error al cargar la lista de usuarios';
      }
    });
  }

  loadOngs(): void {
    this.ongService.getAllOngs().subscribe({
      next: (ongs) => {
        this.ongs = ongs;
        if (ongs.length > 0) {
          this.newUser.ongId = ongs[0].id;
        }
      },
      error: (error) => {
        console.error('Error al cargar ONGs', error);
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

  createUser(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.loading = true;

    this.authService.register(this.newUser).subscribe({
      next: (response) => {
        this.successMessage = `Usuario ${response.username} creado exitosamente`;
        this.loadUsers();
        this.resetForm();
        this.showCreateForm = false;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al crear usuario', error);
        this.errorMessage = error.error?.message || 'Error al crear el usuario';
        this.loading = false;
      }
    });
  }

  resetForm(): void {
    this.newUser = {
      username: '',
      password: '',
      email: '',
      role: 'USER',
      ongId: this.ongs.length > 0 ? this.ongs[0].id : 0
    };
  }
}
