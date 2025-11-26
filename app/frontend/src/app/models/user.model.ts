export interface User {
  id: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'USER' | 'DIRECTIVO';
  ongId: number;
  ongName: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: string;
  ongId: number;
  ongName: string;
}
