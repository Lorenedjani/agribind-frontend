import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';


export interface NewMember {
  id?: string;
  Name: string;
  contact: string;
  Type: string;
  location: string;
  primaryCrop: string;
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
    this.initForm();
  }

  private initForm(): void {
    this.memberForm = this.fb.group({
      FirstName: ['', [Validators.required, Validators.minLength(2), Validators.pattern(/^[a-zA-Z\s]+$/)]],
      LastName: ['', [Validators.required, Validators.minLength(2), Validators.pattern(/^[a-zA-Z\s]+$/)]],
      contact: ['', [Validators.required, Validators.pattern(/^\+237\s[0-9]{8}$/)]],
      Type: ['', Validators.required],
      location: ['', Validators.required],
      primaryCrop: ['', Validators.required],
      status: ['Active', Validators.required],
      email: ['', [Validators.email]],
      joinDate: [new Date().toISOString().split('T')[0]],
      farmSize: [''],
      address: [''],
      lastProduction: [''],
      creditStatus: ['']
    });
  }

  openModal(): void {
    this.isModalOpen = true;
    this.memberForm.reset({
      FirstName: '',
      LastName: '',
      contact: '+237 ',
      Type: '',
      location: '',
      primaryCrop: '',
      status: 'Active'
    });
    document.body.style.overflow = 'hidden';
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
    console.log('Form valid:', this.memberForm.valid);
    console.log('Form values:', this.memberForm.value);

    if (this.memberForm.valid) {
      console.log('Form is valid, proceeding...');
      const formData = this.memberForm.value;
      const fullName = `${formData.FirstName} ${formData.LastName}`.trim();
      const newMember = {
        ...formData,
        Name: fullName
      };

      console.log('Emitting new member:', newMember);
      this.memberAdded.emit(newMember);
      this.closeModal();
    } else {
      console.log('Form is invalid');
      // Mark all fields as touched to show validation errors
      this.memberForm.markAllAsTouched();
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
