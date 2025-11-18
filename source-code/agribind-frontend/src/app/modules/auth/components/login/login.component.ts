import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiClientService } from '../../../../core/services/api-client.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-page">
      <div class="login-card">
        <div class="logo">
          <h1>Agribind</h1>
          <p>Agricultural Management Platform</p>
        </div>

        <form (ngSubmit)="onLogin()" class="login-form">
          <div class="form-group">
            <label>Email or Phone</label>
            <input
              type="text"
              [(ngModel)]="credentials.username"
              name="username"
              placeholder="Enter your email or phone"
              required>
          </div>

          <div class="form-group">
            <label>Password</label>
            <input
              type="password"
              [(ngModel)]="credentials.password"
              name="password"
              placeholder="Enter your password"
              required>
          </div>

          <button type="submit" class="login-btn" [disabled]="!isFormValid() || loading">
            {{ loading ? 'Signing In...' : 'Sign In' }}
          </button>

          <div *ngIf="error" class="error-message">
            {{ error }}
          </div>
        </form>

        <div class="demo-section">
          <p class="demo-text">Demo Account:</p>
          <button type="button" class="demo-btn" (click)="useDemoAccount()">
            Use Cooperative Manager Demo
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #0b6e4f 0%, #16a34a 100%);
      padding: 20px;
    }

    .login-card {
      background: white;
      border-radius: 12px;
      padding: 40px;
      width: 100%;
      max-width: 400px;
      box-shadow: 0 20px 40px rgba(0,0,0,0.1);
    }

    .logo {
      text-align: center;
      margin-bottom: 30px;
    }

    .logo h1 {
      color: #0b6e4f;
      font-size: 2rem;
      margin-bottom: 5px;
    }

    .logo p {
      color: #6b7280;
      font-size: 0.9rem;
    }

    .form-group {
      margin-bottom: 20px;
    }

    .form-group label {
      display: block;
      margin-bottom: 5px;
      color: #374151;
      font-weight: 500;
    }

    .form-group input {
      width: 100%;
      padding: 12px;
      border: 1px solid #d1d5db;
      border-radius: 8px;
      font-size: 1rem;
      box-sizing: border-box;
    }

    .form-group input:focus {
      outline: none;
      border-color: #0b6e4f;
      box-shadow: 0 0 0 3px rgba(11, 110, 79, 0.1);
    }

    .login-btn {
      width: 100%;
      background: #0b6e4f;
      color: white;
      border: none;
      padding: 12px;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      margin-bottom: 20px;
    }

    .login-btn:hover:not(:disabled) {
      background: #065a41;
    }

    .login-btn:disabled {
      background: #d1d5db;
      cursor: not-allowed;
    }

    .error-message {
      color: #dc2626;
      background: #fef2f2;
      border: 1px solid #fecaca;
      padding: 10px;
      border-radius: 6px;
      margin-top: 10px;
      font-size: 0.9rem;
      text-align: center;
    }

    .demo-section {
      border-top: 1px solid #e5e7eb;
      padding-top: 20px;
      text-align: center;
    }

    .demo-text {
      color: #6b7280;
      font-size: 0.9rem;
      margin-bottom: 10px;
    }

    .demo-btn {
      background: #f3f4f6;
      border: 1px solid #d1d5db;
      color: #374151;
      padding: 10px 16px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.9rem;
    }

    .demo-btn:hover {
      background: #e5e7eb;
    }
  `]
})
export class LoginComponent {
  credentials = {
    username: '',
    password: ''
  };

  loading = false;
  error = '';

  constructor(
    private router: Router,
    private apiClient: ApiClientService
  ) {}

  isFormValid(): boolean {
    return !!(this.credentials.username && this.credentials.password);
  }

  useDemoAccount() {
    this.credentials.username = 'coop@agribind.cm';
    this.credentials.password = 'Coop2025@Agribind';
  }

  onLogin() {
    if (!this.isFormValid()) return;

    this.loading = true;
    this.error = '';

    const loginData = {
      username: this.credentials.username,
      password: this.credentials.password,
      deviceId: this.generateDeviceId()
    };

    // ✅ Single API call with proper structure
    this.apiClient.post('/api/v1/auth/login', loginData).subscribe({
      next: (response: any) => {
        console.log('Login response:', response);

        // Check if response is wrapped in data object
        const loginResponse = response.data || response;

        if (loginResponse.accessToken) {
          // Store tokens and user info
          localStorage.setItem('accessToken', loginResponse.accessToken);
          localStorage.setItem('refreshToken', loginResponse.refreshToken);
          localStorage.setItem('user', JSON.stringify(loginResponse.userInfo));

          console.log('User role:', loginResponse.userInfo.role);

          // Navigate based on role
          this.navigateByRole(loginResponse.userInfo.role);
        } else {
          this.error = response.message || 'Login failed';
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Login error:', err);
        this.error = err.error?.message || 'Connection error. Please check credentials and try again.';
        this.loading = false;
      }
    });
  }

  private navigateByRole(role: string) {
    const roleUpper = role.toUpperCase();

    console.log('Navigating for role:', roleUpper);

    if (roleUpper === 'COOPERATIVE') {
      this.router.navigate(['/cooperative']).then(success => {
        console.log('Navigation to cooperative:', success);
      });
    } else if (roleUpper === 'GOVERNMENT') {
      this.router.navigate(['/government']).then(success => {
        console.log('Navigation to government:', success);
      });
    } else if (roleUpper === 'FARMER') {
      this.router.navigate(['/farmer']).then(success => {
        console.log('Navigation to farmer:', success);
      });
    } else if (roleUpper === 'ADMIN') {
      this.router.navigate(['/admin']).then(success => {
        console.log('Navigation to admin:', success);
      });
    } else {
      console.warn('Unknown role:', role);
      this.router.navigate(['/dashboard']);
    }
  }

  private generateDeviceId(): string {
    let deviceId = localStorage.getItem('deviceId');
    if (!deviceId) {
      deviceId = 'WEB_' + Math.random().toString(36).substring(2, 15);
      localStorage.setItem('deviceId', deviceId);
    }
    return deviceId;
  }
}
