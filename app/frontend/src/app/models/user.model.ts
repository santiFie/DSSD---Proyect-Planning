export interface User {
  id: number;
  username: string;
  role: 'ADMIN' | 'USER';
  ongId: number;
  ongName: string;
}

export interface UserResponse {
  id: number;
  username: string;
  role: string;
  ongId: number;
  ongName: string;
}
