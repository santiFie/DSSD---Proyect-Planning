export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
  role: 'ADMIN' | 'USER';
  ongId: number;
  ongName: string;
}

export interface RegisterUserRequest {
  username: string;
  password: string;
  role: 'ADMIN' | 'USER';
  ongId: number;
}
