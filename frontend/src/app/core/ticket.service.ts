import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Category,
  Dashboard,
  TicketDetail,
  TicketPriority,
  TicketStatus,
  TicketSummary,
  User
} from './models';

@Injectable({ providedIn: 'root' })
export class TicketService {
  constructor(private http: HttpClient) {}

  dashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${environment.apiUrl}/dashboard`);
  }

  listTickets(): Observable<TicketSummary[]> {
    return this.http.get<TicketSummary[]>(`${environment.apiUrl}/tickets`);
  }

  getTicket(id: number): Observable<TicketDetail> {
    return this.http.get<TicketDetail>(`${environment.apiUrl}/tickets/${id}`);
  }

  createTicket(payload: { title: string; description: string; categoryId: number; priority: TicketPriority }): Observable<TicketDetail> {
    return this.http.post<TicketDetail>(`${environment.apiUrl}/tickets`, payload);
  }

  assign(id: number, assigneeId?: number): Observable<TicketDetail> {
    return this.http.post<TicketDetail>(`${environment.apiUrl}/tickets/${id}/assign`, { assigneeId: assigneeId ?? null });
  }

  changeStatus(id: number, status: TicketStatus, note?: string): Observable<TicketDetail> {
    return this.http.patch<TicketDetail>(`${environment.apiUrl}/tickets/${id}/status`, { status, note });
  }

  addComment(id: number, content: string): Observable<TicketDetail> {
    return this.http.post<TicketDetail>(`${environment.apiUrl}/tickets/${id}/comments`, { content });
  }

  uploadAttachment(id: number, file: File): Observable<TicketDetail> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<TicketDetail>(`${environment.apiUrl}/tickets/${id}/attachments`, form);
  }

  categories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${environment.apiUrl}/categories`);
  }

  activeCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${environment.apiUrl}/categories/active`);
  }

  createCategory(payload: { name: string; description: string }): Observable<Category> {
    return this.http.post<Category>(`${environment.apiUrl}/categories`, payload);
  }

  updateCategory(id: number, payload: { name: string; description: string }): Observable<Category> {
    return this.http.put<Category>(`${environment.apiUrl}/categories/${id}`, payload);
  }

  toggleCategory(id: number): Observable<Category> {
    return this.http.patch<Category>(`${environment.apiUrl}/categories/${id}/toggle`, {});
  }

  users(): Observable<User[]> {
    return this.http.get<User[]>(`${environment.apiUrl}/users`);
  }

  createUser(payload: { name: string; email: string; password: string; role: string }): Observable<User> {
    return this.http.post<User>(`${environment.apiUrl}/users`, payload);
  }

  updateUser(id: number, payload: { name: string; role: string; active: boolean }): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/users/${id}`, payload);
  }

  toggleUser(id: number): Observable<User> {
    return this.http.patch<User>(`${environment.apiUrl}/users/${id}/toggle`, {});
  }
}
