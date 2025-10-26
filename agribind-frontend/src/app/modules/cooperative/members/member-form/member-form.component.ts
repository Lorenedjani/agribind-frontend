import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';


export interface NewMember {
  id?: string;
  firstName: string;
  lastName: string;
  phone: string;
  email?: string;
  memberType: string;
  region: string;
  location: string;
  primaryCrop: string;
  farmSize?: number;
  registrationDate?: string;
  notes?: string;
  status?: string;
}

@Component({
  selector: 'app-member-form',
  templateUrl: './member-form.component.html',
  styleUrls: ['./member-form.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class MemberFormComponent implements OnInit {
  @Output() memberAdded = new EventEmitter<NewMember>();
  @Output() modalClosed = new EventEmitter<void>();

  isModalOpen = false;
  isSubmitting = false;
  memberForm!: FormGroup;

  constructor(private fb: FormBuilder) {
    console.log('MemberFormComponent constructor called');
  }

  ngOnInit(): void {
    console.log('MemberFormComponent ngOnInit called');
    this.initForm();
  }

  private initForm(): void {
    this.memberForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      phone: ['', [Validators.required, Validators.pattern(/^\+?[\d\s-]+$/)]],
      email: ['', [Validators.email]],
      memberType: ['', Validators.required],
      region: ['', Validators.required],
      location: ['', Validators.required],
      primaryCrop: ['', Validators.required],
      farmSize: [''],
      registrationDate: [''],
      notes: ['']
    });
  }

  openModal(): void {
    console.log('✅ openModal() called');
    this.isModalOpen = true;
    this.memberForm.reset();
    document.body.style.overflow = 'hidden';
    alert('Modal opened!');
  }


  closeModal(): void {
    console.log('MemberFormComponent closeModal called');
    this.isModalOpen = false;
    this.isSubmitting = false;
    this.memberForm.reset();
    document.body.style.overflow = 'auto';
    this.modalClosed.emit();
  }

  onSubmit(): void {
    console.log('Form submitted');
    if (this.memberForm.valid) {
      this.isSubmitting = true;
      const newMember: NewMember = {
        ...this.memberForm.value,
        id: 'M' + Math.floor(1000 + Math.random() * 9000),
        status: 'Active',
        registrationDate: new Date().toISOString().split('T')[0]
      };

      setTimeout(() => {
        this.memberAdded.emit(newMember);
        this.closeModal();
        this.isSubmitting = false;
      }, 800);
    }  else {
      this.markFormGroupTouched(this.memberForm);
    }
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      } else {
        control?.markAsTouched();
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const control = this.memberForm.get(fieldName);
    return !!(control && control.invalid && control.touched);
  }
}
