// src/app/modules/cooperative/members/member-form/member-form.component.ts
import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

export interface NewMember {
  type: 'FARMER' | 'COOPERATIVE';
  name: string;
  email?: string;
  phoneNumber: string;
  region: string;
  department?: string;
  district?: string;
  village?: string;
  preferredLanguage: string;

  // Farmer-specific fields
  agriculturalType?: string;
  cropTypes?: string[];
  landArea?: number;

  // Cooperative-specific fields
  cooperativeType?: string;
  legalRegistrationNumber?: string;
  establishmentYear?: number;
  contactPerson?: string;
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

  // User role - determines what can be added
  userRole: string = 'COOPERATIVE'; // Default, should come from AuthService

  // Form type - dynamic based on selection
  selectedMemberType: 'FARMER' | 'COOPERATIVE' = 'FARMER';

  // Options
  regionOptions = ['ADAMAOUA', 'CENTRE', 'EST', 'EXTREME_NORD', 'LITTORAL',
                   'NORD', 'NORD_OUEST', 'OUEST', 'SUD', 'SUD_OUEST'];

  agriculturalTypeOptions = ['CROP', 'LIVESTOCK', 'MIXED'];

  cropOptions = ['COCOA', 'COFFEE', 'MAIZE', 'CASSAVA', 'RICE', 'COTTON',
                 'PALM_OIL', 'PLANTAINS', 'BANANAS', 'BEANS'];

  cooperativeTypeOptions = ['PRODUCTION', 'MARKETING', 'CREDIT', 'CONSUMER', 'MULTIPURPOSE'];

  constructor(private fb: FormBuilder) {
    console.log('MemberFormComponent constructor called');
  }

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.memberForm = this.fb.group({
      // Common fields
      type: ['FARMER', Validators.required],
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\+237\s?[6-9][0-9]{8}$/)]],
      region: ['', Validators.required],
      department: [''],
      district: [''],
      village: [''],
      preferredLanguage: ['fr', Validators.required],

      // Farmer-specific fields
      agriculturalType: [''],
      cropTypes: [[]],
      landArea: [null, [Validators.min(0.1)]],

      // Cooperative-specific fields
      cooperativeType: [''],
      legalRegistrationNumber: [''],
      establishmentYear: [null, [Validators.min(1900), Validators.max(new Date().getFullYear())]],
      contactPerson: ['']
    });

    // Listen to type changes
    this.memberForm.get('type')?.valueChanges.subscribe(type => {
      this.selectedMemberType = type;
      this.updateValidators();
    });
  }

  private updateValidators(): void {
    // Clear all conditional validators
    this.memberForm.get('agriculturalType')?.clearValidators();
    this.memberForm.get('cropTypes')?.clearValidators();
    this.memberForm.get('landArea')?.clearValidators();
    this.memberForm.get('cooperativeType')?.clearValidators();
    this.memberForm.get('legalRegistrationNumber')?.clearValidators();

    if (this.selectedMemberType === 'FARMER') {
      // Add farmer validators
      this.memberForm.get('agriculturalType')?.setValidators([Validators.required]);
      this.memberForm.get('landArea')?.setValidators([Validators.required, Validators.min(0.1)]);
    } else if (this.selectedMemberType === 'COOPERATIVE') {
      // Add cooperative validators
      this.memberForm.get('cooperativeType')?.setValidators([Validators.required]);
      this.memberForm.get('legalRegistrationNumber')?.setValidators([Validators.required]);
      this.memberForm.get('contactPerson')?.setValidators([Validators.required]);
    }

    // Update validity
    Object.keys(this.memberForm.controls).forEach(key => {
      this.memberForm.get(key)?.updateValueAndValidity();
    });
  }

  openModal(): void {
    this.isModalOpen = true;
    this.selectedMemberType = 'FARMER'; // Default
    this.memberForm.reset({
      type: 'FARMER',
      phoneNumber: '+237 ',
      preferredLanguage: 'fr',
      agriculturalType: '',
      cropTypes: []
    });
    this.updateValidators();
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
      this.isSubmitting = true;

      const formData = this.memberForm.value;

      // Build member object based on type
      const newMember: NewMember = {
        type: formData.type,
        name: formData.name.trim(),
        email: formData.email?.trim() || undefined,
        phoneNumber: formData.phoneNumber.replace(/\s/g, ''), // Remove spaces
        region: formData.region,
        department: formData.department?.trim() || undefined,
        district: formData.district?.trim() || undefined,
        village: formData.village?.trim() || undefined,
        preferredLanguage: formData.preferredLanguage
      };

      // Add type-specific fields
      if (formData.type === 'FARMER') {
        newMember.agriculturalType = formData.agriculturalType;
        newMember.cropTypes = formData.cropTypes || [];
        newMember.landArea = formData.landArea;
      } else if (formData.type === 'COOPERATIVE') {
        newMember.cooperativeType = formData.cooperativeType;
        newMember.legalRegistrationNumber = formData.legalRegistrationNumber;
        newMember.establishmentYear = formData.establishmentYear;
        newMember.contactPerson = formData.contactPerson;
      }

      console.log('Emitting new member:', newMember);
      this.memberAdded.emit(newMember);
      this.closeModal();
    } else {
      console.log('Form is invalid');
      this.memberForm.markAllAsTouched();
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const control = this.memberForm.get(fieldName);
    return !!(control && control.invalid && control.touched);
  }

  // Crop selection helper
  toggleCrop(crop: string): void {
    const cropTypes = this.memberForm.get('cropTypes')?.value || [];
    const index = cropTypes.indexOf(crop);

    if (index > -1) {
      cropTypes.splice(index, 1);
    } else {
      cropTypes.push(crop);
    }

    this.memberForm.patchValue({ cropTypes });
  }

  isCropSelected(crop: string): boolean {
    const cropTypes = this.memberForm.get('cropTypes')?.value || [];
    return cropTypes.includes(crop);
  }

  // Show/hide cooperative type selector
  canAddCooperative(): boolean {
    return this.userRole === 'ADMIN';
  }
}