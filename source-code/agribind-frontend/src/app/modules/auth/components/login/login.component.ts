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
            <label>Email</label>
            <input type="email" [(ngModel)]="credentials.email" name="email" placeholder="Enter your email" required>
          </div>
          
          <div class="form-group">
            <label>Password</label>
            <input type="password" [(ngModel)]="credentials.password" name="password" placeholder="Enter your password" required>
          </div>
          

          
          <button type="submit" class="login-btn" [disabled]="!isFormValid() || loading">
            {{ loading ? 'Signing In...' : 'Sign In' }}
          </button>
          
          <div *ngIf="error" class="error-message">
            {{ error }}
          </div>
        </form>
        

        

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
    
    .form-group input,
    .form-group select {
      width: 100%;
      padding: 12px;
      border: 1px solid #d1d5db;
      border-radius: 8px;
      font-size: 1rem;
      box-sizing: border-box;
    }
    
    .form-group input:focus,
    .form-group select:focus {
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
    
    .demo-accounts {
      border-top: 1px solid #e5e7eb;
      padding-top: 20px;
      text-align: center;
    }
    
    .demo-accounts p {
      color: #6b7280;
      margin-bottom: 10px;
      font-size: 0.9rem;
    }
    
    .demo-btn {
      background: #f3f4f6;
      border: 1px solid #d1d5db;
      color: #374151;
      padding: 8px 16px;
      border-radius: 6px;
      margin: 0 5px;
      cursor: pointer;
      font-size: 0.85rem;
    }
    
    .demo-btn:hover {
      background: #e5e7eb;
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
  `]
})
export class LoginComponent {
  credentials = {
    email: '',
    password: ''
  };
  
  loading = false;
  error = '';

  constructor(
    private router: Router,
    private apiClient: ApiClientService
  ) {}

  isFormValid(): boolean {
    return !!(this.credentials.email && this.credentials.password);
  }

  onLogin() {
    if (!this.isFormValid()) return;
    
    this.loading = true;
    this.error = '';
    
    const loginData = {
      email: this.credentials.email,
      password: this.credentials.password
    };
    
    this.apiClient.post('/auth/login', loginData).subscribe({
      next: (response: any) => {
        if (response.success) {
          localStorage.setItem('accessToken', response.token);
          localStorage.setItem('userRole', response.user.role);
          localStorage.setItem('user', JSON.stringify(response.user));
          
          this.navigateByRole(response.user.role);
        } else {
          this.error = response.error || 'Login failed';
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Connection error. Please try again.';
        this.loading = false;
        console.error('Login error:', err);
      }
    });
  }



  private navigateByRole(role: string) {
    if (role === 'cooperative') {
      this.router.navigate(['/cooperative']);
    } else if (role === 'government') {
      this.router.navigate(['/government']);
    }
  }


}