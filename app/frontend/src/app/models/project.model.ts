export interface Need {
    id?: number;
    description: string;
}

export interface Stage {
  id?: number;
  name: string;
  needs: Need | null;
  covered: boolean;
  startDate: string;
  endDate?: string;
}

export interface Project {
  id?: number;
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  neighborhood?: string;
  stages: Stage[];
}

export interface NewProjectDto {
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  neighborhood?: string;
  stages: Stage[];
}