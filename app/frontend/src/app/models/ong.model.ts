export interface Ong {
  id: number;
  name: string;
  originCountry: string;
}

export interface CreateOngRequest {
  name: string;
  originCountry: string;
}
