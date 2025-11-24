// src/app/core/services/user.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiClientService } from './api-client.service';

export interface User {
phone: any;
primaryCrop: any;
  id?: string;
  userId: string;
  name: string;
  email?: string;
  phoneNumber: string;
  type: 'FARMER' | 'COOPERATIVE' | 'GOVERNMENT';
  status: 'ACTIVE' | 'INACTIVE' | 'PENDING' | 'SUSPENDED' | 'DELETED';
  registrationNumber?: string;
  region?: string;
  department?: string;
  district?: string;
  village?: string;
  gpsCoordinates?: string;
  preferredLanguage?: string;
  createdAt?: string;
  updatedAt?: string;

  // Farmer specific
  agriculturalType?: 'CROP' | 'LIVESTOCK' | 'MIXED';
  cropTypes?: string[];
  livestockTypes?: string[];
  landArea?: number;
  cooperativeId?: string;

  // Cooperative specific
  cooperativeType?: string;
  activeMemberCount?: number;

  // Government specific
  governmentRole?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface CreateUserCommand {
  type: 'FARMER' | 'COOPERATIVE' | 'GOVERNMENT';
  name: string;
  email?: string;
  phoneNumber: string;
  region?: string;
  department?: string;
  district?: string;
  village?: string;
  preferredLanguage?: string;

  // Farmer fields
  agriculturalType?: string;
  cropTypes?: string[];
  livestockTypes?: string[];
  landArea?: number;
  cooperativeId?: string;

  // Cooperative fields
  cooperativeType?: string;
  legalRegistrationNumber?: string;
  establishmentYear?: number;

  // Government fields
  governmentRole?: string;
  employeeId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiClient: ApiClientService) {}

  /**
   * Get paginated users with filters
   */
  getUsers(page: number = 0, size: number = 10, filters: any = {}): Observable<PageResponse<User>> {
    const params = {
      page: page.toString(),
      size: size.toString(),
      ...filters
    };

    return this.apiClient.get<PageResponse<User>>('/api/v1/users', params);
  }

  /**
   * Get farmers only
   */
  getFarmers(page: number = 0, size: number = 10): Observable<PageResponse<User>> {
    return this.getUsers(page, size, { type: 'FARMER' });
  }

  /**
   * Get cooperatives only
   */
  getCooperatives(page: number = 0, size: number = 10): Observable<PageResponse<User>> {
    return this.getUsers(page, size, { type: 'COOPERATIVE' });
  }

  /**
   * Get government officials only
   */
  getGovernmentOfficials(page: number = 0, size: number = 10): Observable<PageResponse<User>> {
    return this.getUsers(page, size, { type: 'GOVERNMENT' });
  }

  /**
   * Get user by ID
   */
  getUserById(userId: string): Observable<User> {
    return this.apiClient.get<User>(`/api/v1/users/${userId}`);
  }

  /**
   * Get user by phone number
   */
  getUserByPhone(phoneNumber: string): Observable<User> {
    return this.apiClient.get<User>(`/api/v1/users/phone/${phoneNumber}`);
  }

  /**
   * Get user by registration number
   */
  getUserByRegistration(registrationNumber: string): Observable<User> {
    return this.apiClient.get<User>(`/api/v1/users/registration/${registrationNumber}`);
  }

  /**
   * Create new user
   */
  createUser(user: CreateUserCommand): Observable<User> {
    return this.apiClient.post<User>('/api/v1/users', user);
  }

  /**
   * Update user
   */
  updateUser(userId: string, updates: Partial<CreateUserCommand>): Observable<User> {
    return this.apiClient.put<User>(`/api/v1/users/${userId}`, updates);
  }

  /**
   * Update user status
   */
  updateUserStatus(userId: string, status: string): Observable<User> {
    return this.apiClient.patch<User>(`/api/v1/users/${userId}/status?status=${status}`, {});
  }

  /**
   * Delete user (soft delete)
   */
  deleteUser(userId: string): Observable<void> {
    return this.apiClient.delete<void>(`/api/v1/users/${userId}`);
  }

  /**
   * Check if phone exists
   */
  checkPhoneExists(phoneNumber: string): Observable<boolean> {
    return this.apiClient.get<boolean>(`/api/v1/users/exists/phone/${phoneNumber}`);
  }

  /**
   * Check if email exists
   */
  checkEmailExists(email: string): Observable<boolean> {
    return this.apiClient.get<boolean>(`/api/v1/users/exists/email/${email}`);
  }

  /**
   * Search users with advanced filters
   */
  searchUsers(filters: any, page: number = 0, size: number = 10): Observable<PageResponse<User>> {
    const searchQuery = {
      ...filters,
      page,
      size
    };

    return this.apiClient.post<PageResponse<User>>('/api/v1/users/search', searchQuery);
  }

  /**
   * Export users to file
   */
  exportUsers(format: 'CSV' | 'EXCEL' | 'PDF', filters: any = {}): Observable<Blob> {
    const exportQuery = {
      format,
      ...filters
    };

    return this.apiClient.post<Blob>('/api/v1/exports/generate', exportQuery);
  }

  /**
   * Upload profile picture
   */
  uploadProfilePicture(userId: string, file: File): Observable<User> {
    const formData = new FormData();
    formData.append('file', file);

    // Note: This might need special handling as it's multipart/form-data
    return this.apiClient.post<User>(`/api/v1/users/${userId}/profile-picture`, formData);
  }
}