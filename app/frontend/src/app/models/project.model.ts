export interface Stage {
  id?: number;
  name: string;
  needs: string;
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
  stages: Stage[];
}

export interface NewProjectDto {
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  stages: Stage[];
}