import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Project, NewProjectDto, Stage} from '../models/project.model';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = 'http://localhost:8081/api/projects';

  constructor(private http: HttpClient) { }

  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/all`);
  }

  getMyProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/my-projects`);
  }

  getMyCoveredProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/my-projects/covered`);
  }

  getProjectsByOng(ongId: number): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/ong/${ongId}`);
  }

  getProjectById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/id/${id}`);
  }

  getProjectByName(name: string): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${name}`);
  }

  createProject(project: NewProjectDto): Observable<Project> {
    return this.http.post<Project>(this.apiUrl, project);
  }

  updateProject(id: number, project: NewProjectDto): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${id}`, project);
  }

  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getStagesByProjectId(projectId: number): Observable<Stage[]> {
    return this.http.get<Stage[]>(`${this.apiUrl}/${projectId}/stages`);
  }

  executeStageByProjectId(projectId: number, stageId: number): Observable<Stage> {
    return this.http.put<Stage>(`${this.apiUrl}/my-projects/${projectId}/execute-stage/${stageId}`, {});
  }
}