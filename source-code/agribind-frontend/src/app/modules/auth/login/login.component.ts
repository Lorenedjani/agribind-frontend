// src/app/modules/auth/login/login.component.ts
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

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

    // ✅ FIXED: Match form control names
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]], // ✅ Use 'username' not 'email'
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
    this.errorMessage = '';

    // ✅ Mark all fields as touched to show validation errors
    if (this.loginForm.invalid) {
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isLoading = true;

    // ✅ FIXED: Prepare credentials with correct field names
    const credentials = {
      username: this.loginForm.value.username.trim(),
      password: this.loginForm.value.password
    };

    console.log('🔐 Submitting login credentials:', {
      username: credentials.username,
      passwordLength: credentials.password.length
    });

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
      error: (error) => {
        console.error('❌ Login failed:', error);
        this.isLoading = false;
        this.errorMessage = this.getErrorMessage(error);

        // ✅ Show error message in UI
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

  private getErrorMessage(error: any): string {
    if (error.message) {
      return error.message;
    }

    if (error.status === 401) {
      return 'Invalid email or password. Please check your credentials.';
    }

    if (error.status === 403) {
      return 'Account is disabled or locked. Please contact support.';
    }

    if (error.status === 0) {
      return 'Cannot connect to server. Please check your internet connection.';
    }

    if (error.status === 400) {
      return 'Invalid login request. Please check your input.';
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