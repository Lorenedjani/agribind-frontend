import { Component, EventEmitter, Output, OnInit } from '@angular/core';
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

  isModalOpen = false;
  isSubmitting = false;
  memberForm!: FormGroup;
  currentMember: Member | null = null;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.memberForm = this.fb.group({
      id: [{ value: '', disabled: true }],
      name: ['', [Validators.required, Validators.minLength(2)]],
      phone: ['', [Validators.required, Validators.pattern(/^\+237\s[0-9]{8}$/)]],
      type: ['', Validators.required],
      region: ['', Validators.required],
      primaryCrop: ['', Validators.required],
      status: ['Active', Validators.required]
    });
  }

  openModal(member: Member): void {
    console.log('Opening edit modal for member:', member);
    this.currentMember = member;
    this.isModalOpen = true;

    // Populate form with member data
    this.memberForm.patchValue({
      id: member.id,
      name: member.name,
      phone: member.phone,
      type: member.type,
      region: member.region,
      primaryCrop: member.primaryCrop,
      status: member.status
    });

    document.body.style.overflow = 'hidden';
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.isSubmitting = false;
    this.currentMember = null;
    this.memberForm.reset();
    document.body.style.overflow = 'auto';
    this.modalClosed.emit();
  }

  onSubmit(): void {
    if (this.memberForm.valid && this.currentMember) {
      this.isSubmitting = true;

      const formData = this.memberForm.getRawValue();
      const updatedMember: Member = {
        id: formData.id,
        name: formData.name,
        phone: formData.phone,
        type: formData.type,
        region: formData.region,
        primaryCrop: formData.primaryCrop,
        status: formData.status
      };

      console.log('Emitting updated member:', updatedMember);
      this.memberUpdated.emit(updatedMember);
      this.closeModal();
    } else {
      this.memberForm.markAllAsTouched();
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const control = this.memberForm.get(fieldName);
    return !!(control && control.invalid && control.touched);
  }
}
