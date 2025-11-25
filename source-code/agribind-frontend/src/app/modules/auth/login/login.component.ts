// src/app/modules/auth/login/login.component.ts
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
    // Check if already logged in
    if (this.authService.isLoggedIn()) {
      this.navigateToDashboard();
    }

    // ✅ Initialize form with proper field names
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
    console.log('🚀 Form submission started');

    // Reset error message
    this.errorMessage = '';

    // ✅ Validate form
    if (this.loginForm.invalid) {
      console.log('❌ Form is invalid');
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
      return;
    }

    // ✅ Check if form values exist
    if (!this.loginForm.value.username || !this.loginForm.value.password) {
      console.error('❌ Username or password is empty');
      this.errorMessage = 'Please enter both username and password';
      return;
    }

    this.isLoading = true;

    // ✅ Prepare credentials - CRITICAL FIX
    const credentials = {
      username: this.loginForm.value.username.trim(),
      password: this.loginForm.value.password
    };

    console.log('📤 Sending login request:', {
      username: credentials.username,
      passwordLength: credentials.password.length,
      apiUrl: 'http://localhost:8080/api/v1/auth/login'
    });

    // ✅ Call auth service
    this.authService.login(credentials).subscribe({
      next: (response) => {
        console.log('✅ Login successful:', response);
        this.isLoading = false;

        // Check if user needs to change password on first login
        if (response.userInfo.firstLogin) {
          console.log('⚠️ First login detected, redirecting to password change');
          this.router.navigate(['/change-password']);
        } else {
          this.navigateToDashboard();
        }
      },
      error: (error: HttpErrorResponse) => {
        console.error('❌ Login failed:', error);
        this.isLoading = false;

        // ✅ Enhanced error handling
        if (error.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please check if the backend is running.';
          console.error('🔌 Network error - Backend may be down or CORS issue');
        } else {
          this.errorMessage = this.getErrorMessage(error);
        }

        // Show error in UI
        alert(this.errorMessage);
      }
    });
  }

  private navigateToDashboard() {
    const user = this.authService.getCurrentUser();
    if (!user) {
      this.router.navigate(['/login']);
      return;
    }

    // ✅ Route based on user role
    console.log('📍 Routing user with role:', user.role);

    switch (user.role.toUpperCase()) {
      case 'FARMER':
        console.log('📍 Navigating to farmer dashboard');
        this.router.navigate(['/farmer/dashboard']);
        break;
      case 'COOPERATIVE':
        console.log('📍 Navigating to cooperative dashboard');
        this.router.navigate(['/cooperative']);
        break;
      case 'GOVERNMENT':
        console.log('📍 Navigating to government dashboard');
        this.router.navigate(['/government/dashboard']);
        break;
      default:
        console.warn('⚠️ Unknown role, navigating to default dashboard');
        this.router.navigate(['/dashboard']);
    }
  }

  private getErrorMessage(error: HttpErrorResponse): string {
    console.log('🔍 Error details:', {
      status: error.status,
      message: error.message,
      error: error.error
    });

    if (error.status === 0) {
      return 'Cannot connect to server. Please ensure the backend is running on http://localhost:8080';
    }

    if (error.status === 401) {
      return 'Invalid email or password. Please check your credentials.';
    }

    if (error.status === 403) {
      return 'Account is disabled or locked. Please contact support.';
    }

    if (error.status === 400) {
      return 'Invalid login request. Please check your input.';
    }

    if (error.error?.message) {
      return error.error.message;
    }

    return 'An error occurred during login. Please try again.';
  }

  onQRLogin() {
    this.router.navigate(['/qr-login']);
  }

  onForgotPassword() {
    this.router.navigate(['/forgot-password']);
  }
}