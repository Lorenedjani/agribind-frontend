import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiClientService } from './api-client.service';
import { Router } from '@angular/router';

export interface LoginRequest {
  username: string;
  password: string;
  deviceId?: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userInfo: {
    userId: string;
    username: string;
    email: string;
    role: string;
    cooperativeId?: string;
    preferredLanguage: string;
    firstLogin: boolean;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private apiClient: ApiClientService,
    private router: Router
  ) {
    // Load user from localStorage on service init
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      this.currentUserSubject.next(JSON.parse(storedUser));
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.apiClient.post<LoginResponse>('/api/v1/auth/login', credentials).pipe(
      tap(response => {
        // Store tokens and user info
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.setItem('refreshToken', response.refreshToken);
        localStorage.setItem('user', JSON.stringify(response.userInfo));

        this.currentUserSubject.next(response.userInfo);
      })
    );
  }

  logout(): void {
    const userId = this.currentUserSubject.value?.userId;

    if (userId) {
      // Call logout endpoint
      this.apiClient.post('/api/v1/auth/logout', { userId }).subscribe();
    }

    // Clear local storage
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');

    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('accessToken');
  }

  getCurrentUser(): any {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }
}
