import { Component, EventEmitter, Output, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

export interface Member {
  id: string;
  name: string;
  phone: string;
  type: string;
  region: string;
  primaryCrop: string;
  status: string;
  firstName?: string;
  lastName?: string;
}

@Component({
  selector: 'app-edit-member-form',
  templateUrl: './edit-member-form.component.html',
  styleUrls: ['./edit-member-form.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class EditMemberFormComponent implements OnInit {
  @Output() memberUpdated = new EventEmitter<Member>();
  @Output() modalClosed = new EventEmitter<void>();
  @Input() memberToEdit!: Member;

  // Add these properties to resolve the template errors
  isModalOpen = false;
  isSubmitting = false;
  memberForm!: FormGroup;

  constructor(private fb: FormBuilder) {}

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
      status: ['Active', Validators.required]
    });
  }

  // Add this method to open the modal
  openModal(member: Member): void {
    console.log('Opening edit modal for member:', member);

    // Split the name into first and last name
    const nameParts = member.name.split(' ');
    const firstName = nameParts[0] || '';
    const lastName = nameParts.slice(1).join(' ') || '';

    // Pre-populate the form with member data
    this.memberForm.patchValue({
      FirstName: firstName,
      LastName: lastName,
      contact: member.phone,
      Type: member.type,
      location: member.region,
      primaryCrop: member.primaryCrop,
      status: member.status
    });

    this.memberToEdit = member;
    this.isModalOpen = true;
    document.body.style.overflow = 'hidden';
  }

  // Add this method to close the modal
  closeModal(): void {
    this.isModalOpen = false;
    this.isSubmitting = false;
    this.memberForm.reset();
    document.body.style.overflow = 'auto';
    this.modalClosed.emit();
  }

  // Add this method for form submission
  onSubmit(): void {
    console.log('Edit form submitted');

    if (this.memberForm.valid) {
      this.isSubmitting = true;
      const formData = this.memberForm.value;

      // Create updated member object
      const updatedMember: Member = {
        ...this.memberToEdit,
        name: `${formData.FirstName} ${formData.LastName}`.trim(),
        phone: formData.contact,
        type: formData.Type,
        region: formData.location,
        primaryCrop: formData.primaryCrop,
        status: formData.status,
        firstName: formData.FirstName,
        lastName: formData.LastName
      };

      console.log('Emitting updated member:', updatedMember);
      this.memberUpdated.emit(updatedMember);
      this.closeModal();
    } else {
      // Mark all fields as touched to show validation errors
      this.memberForm.markAllAsTouched();
    }
  }

  // Add this method for field validation
  isFieldInvalid(fieldName: string): boolean {
    const control = this.memberForm.get(fieldName);
    return !!(control && control.invalid && control.touched);
  }
}
