// FIXED VERSION - Replace your login.component.ts
// source-code/agribind-frontend/src/app/modules/auth/login/login.component.ts

import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;
  showPassword = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    if (this.authService.isLoggedIn()) {
      this.navigateToDashboard();
    }

    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  get username() {
    return this.loginForm.get('username');
  }

  get password() {
    return this.loginForm.get('password');
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    const timestamp = new Date().toISOString();
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log('🚀 LOGIN ATTEMPT:', timestamp);
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

    this.errorMessage = '';

    if (this.loginForm.invalid) {
      console.log('❌ Form is invalid');
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
      this.errorMessage = 'Please enter both username and password';
      return;
    }

    if (!this.loginForm.value.username || !this.loginForm.value.password) {
      console.error('❌ Username or password is empty');
      this.errorMessage = 'Please enter both username and password';
      return;
    }

    this.isLoading = true;

    const credentials = {
      username: this.loginForm.value.username.trim(),
      password: this.loginForm.value.password
    };

    console.log('📤 REQUEST DETAILS:');
    console.log('   Username:', credentials.username);
    console.log('   Password Length:', credentials.password.length);
    console.log('   API URL: http://localhost:8080/api/v1/auth/login');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

    this.authService.login(credentials).subscribe({
      next: (response) => {
        console.log('✅ LOGIN SUCCESS:', timestamp);
        console.log('   Response:', response);
        console.log('   User Role:', response.userInfo.role);
        console.log('   First Login:', response.userInfo.firstLogin);
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

        this.isLoading = false;

        if (response.userInfo.firstLogin) {
          console.log('⚠️ First login detected, redirecting to password change');
          this.router.navigate(['/change-password']);
        } else {
          this.navigateToDashboard();
        }
      },
      error: (error: HttpErrorResponse) => {
        console.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        console.error('❌ LOGIN FAILED:', timestamp);
        console.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

        this.isLoading = false;

        const errorDetails = {
          status: error.status || 0,
          statusText: error.statusText || 'Unknown',
          message: error.message || 'Unknown error',
          url: error.url || 'Unknown URL',
          error: error.error || {},
          timestamp: timestamp
        };

        console.error('📊 ERROR DETAILS:');
        console.error('   Status Code:', errorDetails.status);
        console.error('   Status Text:', errorDetails.statusText);
        console.error('   URL:', errorDetails.url);
        console.error('   Error Message:', errorDetails.message);
        console.error('   Error Object:', errorDetails.error);
        console.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');

        if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please check if the backend is running.';
        } else if (error.status === 401) {
          this.errorMessage = 'Invalid email or password. Please check your credentials.';
        } else if (error.status === 403) {
          this.errorMessage = 'Account is disabled or locked. Please contact support.';
        } else if (error.status === 400) {
          this.errorMessage = 'Invalid login request. Please check your input.';
        } else {
          this.errorMessage = this.getErrorMessage(error);
        }

        alert(`❌ Login Failed!\n\n${this.errorMessage}\n\nCheck browser console for details.`);
      }
    });
  }

  private navigateToDashboard() {
    const user = this.authService.getCurrentUser();
    if (!user) {
      console.error('❌ No user data after successful login');
      this.router.navigate(['/login']);
      return;
    }

    console.log('📍 Routing user with role:', user.role);

    // ✅ FIXED: Correct route paths based on app.routes.ts
    switch (user.role.toUpperCase()) {
      case 'FARMER':
        console.log('📍 Navigating to farmer dashboard');
        this.router.navigate(['/farmer/dashboard']);
        break;
      case 'COOPERATIVE':
        console.log('📍 Navigating to cooperative dashboard');
        // ✅ FIX: Changed from '/cooperative/dashboard' to '/cooperative'
        this.router.navigate(['/cooperative']).then(success => {
          if (success) {
            console.log('✅ Navigation successful to /cooperative');
          } else {
            console.error('❌ Navigation failed to /cooperative');
          }
        });
        break;
      case 'GOVERNMENT':
        console.log('📍 Navigating to government dashboard');
        this.router.navigate(['/government/dashboard']);
        break;
      default:
        console.warn('⚠️ Unknown role:', user.role, '- navigating to login');
        this.router.navigate(['/login']);
    }
  }

  private getErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Cannot connect to server. Ensure backend is running on http://localhost:8080';
    }

    if (error.status === 401) {
      return 'Invalid email or password.';
    }

    if (error.status === 403) {
      return 'Account is disabled or locked.';
    }

    if (error.status === 400) {
      return 'Invalid login request.';
    }

    if (error.error?.message) {
      return error.error.message;
    }

    if (error.error?.data?.message) {
      return error.error.data.message;
    }

    return 'An error occurred during login. Please try again.';
  }

  onQRLogin() {
    console.log('📱 QR Login requested');
    this.router.navigate(['/qr-login']);
  }

  onForgotPassword() {
    console.log('🔑 Forgot password requested');
    this.router.navigate(['/change-password']);
  }
}