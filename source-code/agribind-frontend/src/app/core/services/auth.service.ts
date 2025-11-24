// src/app/core/services/auth.service.ts
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { ApiClientService } from './api-client.service';
import { Router } from '@angular/router';

export interface LoginRequest {
  username: string; // Can be email or phone number
  password: string;
  deviceId?: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userInfo: UserInfo;
}

export interface UserInfo {
  userId: string;
  username: string;
  email: string;
  role: string; // FARMER, COOPERATIVE, GOVERNMENT
  cooperativeId?: string;
  preferredLanguage: string;
  firstLogin: boolean;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<UserInfo | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private apiClient: ApiClientService,
    private router: Router
  ) {
    this.loadStoredUser();
  }

  private loadStoredUser(): void {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        const user = JSON.parse(storedUser);
        this.currentUserSubject.next(user);
        console.log('✅ Loaded stored user:', user);
      } catch (e) {
        console.error('❌ Failed to parse stored user:', e);
        this.clearStoredData();
      }
    }
  }

  /**
   * Login with username (email or phone) and password
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    console.log('🔐 Attempting login with:', credentials.username);

    // Add device ID if not provided
    if (!credentials.deviceId) {
      credentials.deviceId = this.getOrCreateDeviceId();
    }

    return this.apiClient.post<ApiResponse<LoginResponse>>('/api/v1/auth/login', credentials).pipe(
      map(response => {
        // Handle wrapped response
        return response.data || response as any;
      }),
      tap(loginData => {
        console.log('✅ Login response received:', loginData);
        this.handleLoginSuccess(loginData);
      }),
      catchError(error => {
        console.error('❌ Login failed:', error);
        return throwError(() => this.formatError(error));
      })
    );
  }

  /**
   * QR Code login for farmers
   */
  qrLogin(registrationNumber: string): Observable<LoginResponse> {
    console.log('📱 Attempting QR login with registration:', registrationNumber);

    const deviceId = this.getOrCreateDeviceId();

    return this.apiClient.post<ApiResponse<LoginResponse>>('/api/v1/auth/qr-login', {
      registrationNumber,
      deviceId
    }).pipe(
      map(response => response.data || response as any),
      tap(loginData => this.handleLoginSuccess(loginData)),
      catchError(error => {
        console.error('❌ QR Login failed:', error);
        return throwError(() => this.formatError(error));
      })
    );
  }

  /**
   * Change password for authenticated user
   */
  changePassword(request: PasswordChangeRequest): Observable<void> {
    const userId = this.getCurrentUser()?.userId;
    if (!userId) {
      return throwError(() => ({ message: 'User not authenticated' }));
    }

    console.log('🔑 Changing password for user:', userId);

    return this.apiClient.put<ApiResponse<void>>(`/api/v1/auth/password/change`, request).pipe(
      tap(() => console.log('✅ Password changed successfully')),
      map(() => undefined),
      catchError(error => {
        console.error('❌ Password change failed:', error);
        return throwError(() => this.formatError(error));
      })
    );
  }

  /**
   * Set password on first login
   */
  setFirstLoginPassword(newPassword: string, confirmPassword: string): Observable<void> {
    const userId = this.getCurrentUser()?.userId;
    if (!userId) {
      return throwError(() => ({ message: 'User not authenticated' }));
    }

    return this.apiClient.post<ApiResponse<void>>('/api/v1/auth/password/first-login', {
      newPassword,
      confirmPassword,
      preferredLanguage: this.getCurrentUser()?.preferredLanguage || 'fr'
    }).pipe(
      tap(() => {
        console.log('✅ First login password set successfully');
        // Update user to mark first login complete
        const user = this.getCurrentUser();
        if (user) {
          user.firstLogin = false;
          localStorage.setItem('user', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }
      }),
      map(() => undefined),
      catchError(error => throwError(() => this.formatError(error)))
    );
  }

  /**
   * Refresh access token
   */
  refreshToken(): Observable<LoginResponse> {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      return throwError(() => ({ message: 'No refresh token available' }));
    }

    return this.apiClient.post<ApiResponse<LoginResponse>>('/api/v1/auth/refresh', {
      refreshToken
    }).pipe(
      map(response => response.data || response as any),
      tap(loginData => {
        // Update tokens
        localStorage.setItem('accessToken', loginData.accessToken);
        console.log('✅ Token refreshed successfully');
      }),
      catchError(error => {
        console.error('❌ Token refresh failed:', error);
        this.logout();
        return throwError(() => this.formatError(error));
      })
    );
  }

  /**
   * Logout current user
   */
  logout(): void {
    const userId = this.currentUserSubject.value?.userId;
    const deviceId = this.getDeviceId();

    if (userId) {
      // Call logout endpoint (fire and forget)
      this.apiClient.post('/api/v1/auth/logout', { userId, deviceId }).subscribe({
        next: () => console.log('✅ Logout successful'),
        error: (err) => console.error('❌ Logout error:', err)
      });
    }

    this.clearStoredData();
    this.router.navigate(['/login']);
  }

  /**
   * Handle successful login
   */
  private handleLoginSuccess(loginData: LoginResponse): void {
    if (loginData.accessToken && loginData.userInfo) {
      // Store tokens
      localStorage.setItem('accessToken', loginData.accessToken);
      localStorage.setItem('refreshToken', loginData.refreshToken);
      localStorage.setItem('user', JSON.stringify(loginData.userInfo));

      // Update current user
      this.currentUserSubject.next(loginData.userInfo);

      console.log('✅ Login successful:', {
        userId: loginData.userInfo.userId,
        role: loginData.userInfo.role,
        firstLogin: loginData.userInfo.firstLogin
      });
    } else {
      console.error('❌ Invalid login response structure:', loginData);
      throw new Error('Invalid login response');
    }
  }

  /**
   * Clear stored authentication data
   */
  private clearStoredData(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
    console.log('🗑️ Authentication data cleared');
  }

  /**
   * Get or create device ID for tracking
   */
  private getOrCreateDeviceId(): string {
    let deviceId = localStorage.getItem('deviceId');
    if (!deviceId) {
      deviceId = this.generateDeviceId();
      localStorage.setItem('deviceId', deviceId);
    }
    return deviceId;
  }

  private getDeviceId(): string | null {
    return localStorage.getItem('deviceId');
  }

  private generateDeviceId(): string {
    return 'WEB-' + Math.random().toString(36).substr(2, 9) + '-' + Date.now();
  }

  /**
   * Format error for consistent error handling
   */
  private formatError(error: any): any {
    if (error.error?.message) {
      return { message: error.error.message, status: error.status };
    }
    return { message: error.message || 'An error occurred', status: error.status };
  }

  // Public getters
  isLoggedIn(): boolean {
    return !!localStorage.getItem('accessToken');
  }

  getCurrentUser(): UserInfo | null {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getUserRole(): string | null {
    const user = this.getCurrentUser();
    return user?.role || null;
  }

  requiresFirstLoginPasswordChange(): boolean {
    const user = this.getCurrentUser();
    return user?.firstLogin || false;
  }
}