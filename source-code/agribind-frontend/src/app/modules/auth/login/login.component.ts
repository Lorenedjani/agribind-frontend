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

    this.loginForm = this.fb.group({
      username: ['', [Validators.required]], // Can be email or phone
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

    if (this.loginForm.invalid) {
      // Mark all fields as touched to show validation errors
      Object.keys(this.loginForm.controls).forEach(key => {
        this.loginForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isLoading = true;

    const credentials = {
      username: this.loginForm.value.username.trim(),
      password: this.loginForm.value.password
    };

    console.log('🔐 Submitting login for:', credentials.username);

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
      }
    });
  }

  private navigateToDashboard() {
    const user = this.authService.getCurrentUser();
    if (!user) {
      this.router.navigate(['/login']);
      return;
    }

    // Route based on user role
    switch (user.role.toUpperCase()) {
      case 'FARMER':
        this.router.navigate(['/farmer/dashboard']);
        break;
      case 'COOPERATIVE':
        this.router.navigate(['/cooperative/dashboard']);
        break;
      case 'GOVERNMENT':
        this.router.navigate(['/government/dashboard']);
        break;
      default:
        this.router.navigate(['/dashboard']);
    }
  }

  private getErrorMessage(error: any): string {
    if (error.message) {
      return error.message;
    }

    if (error.status === 401) {
      return 'Invalid username or password';
    }

    if (error.status === 403) {
      return 'Account is disabled or locked';
    }

    if (error.status === 0) {
      return 'Cannot connect to server. Please check your internet connection.';
    }

    return 'An error occurred during login. Please try again.';
  }

  // Optional: QR Login for farmers
  onQRLogin() {
    // Navigate to QR scanner page
    this.router.navigate(['/qr-login']);
  }

  // Navigate to forgot password
  onForgotPassword() {
    this.router.navigate(['/forgot-password']);
  }
}