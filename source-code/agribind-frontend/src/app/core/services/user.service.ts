import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';

export interface User {
  id?: string;
  userId?: string;
  type: 'FARMER' | 'COOPERATIVE' | 'GOVERNMENT';
  name: string;
  email?: string;
  phoneNumber: string;
  status?: string;
  region?: string;
  department?: string;
  district?: string;
  village?: string;
  preferredLanguage?: string;
  agriculturalType?: string;
  cropTypes?: string[];
  livestockTypes?: string[];
  landArea?: number;
  cooperativeId?: number;
}

export interface PageResponse<T> {
  content: T[];
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiClient: ApiClientService) {}

  // Get all users with pagination
  getUsers(page: number = 0, size: number = 20, filters?: any): Observable<PageResponse<User>> {
    const params = { page, size, ...filters };
    return this.apiClient.get<PageResponse<User>>('/api/v1/users', params);
  }

  // Get user by ID
  getUserById(userId: string): Observable<User> {
    return this.apiClient.get<User>(`/api/v1/users/${userId}`);
  }

  // Create new user (member)
  createUser(user: User): Observable<User> {
    return this.apiClient.post<User>('/api/v1/users', user);
  }

  // Update user
  updateUser(userId: string, user: Partial<User>): Observable<User> {
    return this.apiClient.put<User>(`/api/v1/users/${userId}`, user);
  }

  // Delete user
  deleteUser(userId: string): Observable<void> {
    return this.apiClient.delete<void>(`/api/v1/users/${userId}`);
  }

  // Update user status
  updateUserStatus(userId: string, status: string): Observable<User> {
    return this.apiClient.put<User>(
      `/api/v1/users/${userId}/status?status=${status}`,
      {}
    );
  }

  // Check if phone exists
  checkPhoneExists(phoneNumber: string): Observable<boolean> {
    return this.apiClient.get<boolean>(`/api/v1/users/exists/phone/${phoneNumber}`);
  }

  // Check if email exists
  checkEmailExists(email: string): Observable<boolean> {
    return this.apiClient.get<boolean>(`/api/v1/users/exists/email/${email}`);
  }

  // Get farmers only
  getFarmers(page: number = 0, size: number = 20): Observable<PageResponse<User>> {
    return this.getUsers(page, size, { type: 'FARMER' });
  }

  // Get cooperatives only
  getCooperatives(page: number = 0, size: number = 20): Observable<PageResponse<User>> {
    return this.getUsers(page, size, { type: 'COOPERATIVE' });
  }

  // Advanced search
  searchUsers(filters: any): Observable<PageResponse<User>> {
    return this.apiClient.post<PageResponse<User>>('/api/v1/users/search', filters);
  }

  // Export users
  exportUsers(format: string, filters?: any): Observable<Blob> {
    const exportData = {
      format,
      ...filters
    };
    return this.apiClient.post<Blob>('/api/v1/exports/generate', exportData);
  }
}
