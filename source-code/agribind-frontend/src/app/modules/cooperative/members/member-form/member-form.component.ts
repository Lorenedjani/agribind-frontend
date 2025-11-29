// src/app/modules/cooperative/members/member-form/member-form.component.ts
import { Component, EventEmitter, Output, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { environment } from '../../../../../environments/environment';

declare var google: any;
declare global {
  interface Window {
    google: any;
  }
}

export interface NewMember {
  type: 'FARMER' | 'COOPERATIVE';
  name: string;
  email?: string;
  phoneNumber: string;

  // Residential address
  region: string;
  department?: string;
  district?: string;
  village?: string;
  preferredLanguage: string;

  // Farm location GPS coordinates
  gpsCoordinates?: string;

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
export class MemberFormComponent implements OnInit, AfterViewInit, OnDestroy {
  @Output() memberAdded = new EventEmitter<NewMember>();
  @Output() modalClosed = new EventEmitter<void>();

  isModalOpen = false;
  isSubmitting = false;
  memberForm!: FormGroup;

  // Google Maps variables
  private map: any;
  private marker: any;
  private autocomplete: any;
  private mapInitialized = false;
  private geocoder: any;
  private mapLoadTimeout: any;

  userRole: string = 'COOPERATIVE';
  selectedMemberType: 'FARMER' | 'COOPERATIVE' = 'FARMER';

  // Options
  regionOptions = ['ADAMAOUA', 'CENTRE', 'EST', 'EXTREME_NORD', 'LITTORAL',
                   'NORD', 'NORD_OUEST', 'OUEST', 'SUD', 'SUD_OUEST'];
  agriculturalTypeOptions = ['CROP', 'LIVESTOCK', 'MIXED'];
  cropOptions = ['COCOA', 'COFFEE', 'MAIZE', 'CASSAVA', 'RICE', 'COTTON',
                 'PALM_OIL', 'PLANTAINS', 'BANANAS', 'BEANS'];
  cooperativeTypeOptions = ['PRODUCTION', 'MARKETING', 'CREDIT', 'CONSUMER', 'MULTIPURPOSE'];

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initForm();
  }

  ngAfterViewInit(): void {
    this.initializeGoogleMaps();
  }

  ngOnDestroy(): void {
    this.cleanupGoogleMaps();
    if (this.mapLoadTimeout) {
      clearTimeout(this.mapLoadTimeout);
    }
  }

  private initForm(): void {
    this.memberForm = this.fb.group({
      // Common fields
      type: ['FARMER', Validators.required],
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.email]],
      // FIXED: Simplified phone regex to avoid stack overflow
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\+237\s?[6-9]\d{8}$/)]],

      // Residential address
      region: ['', Validators.required],
      department: [''],
      district: [''],
      village: [''],
      preferredLanguage: ['fr', Validators.required],

      // Farm location
      farmLocationSearch: [''],
      farmGpsCoordinates: [''],

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

    // Initialize validators
    this.updateValidators();
  }

  private updateValidators(): void {
    const farmGpsControl = this.memberForm.get('farmGpsCoordinates');
    const agricTypeControl = this.memberForm.get('agriculturalType');
    const landAreaControl = this.memberForm.get('landArea');
    const coopTypeControl = this.memberForm.get('cooperativeType');
    const legalRegControl = this.memberForm.get('legalRegistrationNumber');
    const contactPersonControl = this.memberForm.get('contactPerson');

    // Clear all validators first
    [farmGpsControl, agricTypeControl, landAreaControl, coopTypeControl, legalRegControl, contactPersonControl]
      .forEach(control => control?.clearValidators());

    if (this.selectedMemberType === 'FARMER') {
      farmGpsControl?.setValidators([Validators.required]);
      agricTypeControl?.setValidators([Validators.required]);
      landAreaControl?.setValidators([Validators.required, Validators.min(0.1)]);
    } else if (this.selectedMemberType === 'COOPERATIVE') {
      coopTypeControl?.setValidators([Validators.required]);
      legalRegControl?.setValidators([Validators.required]);
      contactPersonControl?.setValidators([Validators.required]);
    }

    // Update validity
    [farmGpsControl, agricTypeControl, landAreaControl, coopTypeControl, legalRegControl, contactPersonControl]
      .forEach(control => control?.updateValueAndValidity({ emitEvent: false }));
  }

  private initializeGoogleMaps(): void {
    if (!window.google) {
      this.loadGoogleMapsScript();
    } else {
      this.mapInitialized = true;
      this.geocoder = new google.maps.Geocoder();
    }
  }

  private loadGoogleMapsScript(): void {
    const script = document.createElement('script');
    const apiKey = (environment as any).googleMapsApiKey;
    const src = apiKey
      ? `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(apiKey)}&libraries=places`
      : `https://maps.googleapis.com/maps/api/js?libraries=places`;

    script.src = src;
    script.async = true;
    script.defer = true;
    script.onload = () => {
      console.log('✅ Google Maps API loaded');
      this.mapInitialized = true;
      this.geocoder = new google.maps.Geocoder();
    };
    script.onerror = (error) => {
      console.error('❌ Failed to load Google Maps API:', error);
    };
    document.head.appendChild(script);
  }

  private initMapInstance(): void {
    if (!this.mapInitialized || !window.google) {
      console.warn('⚠️ Google Maps not initialized yet');
      return;
    }

    const mapContainer = document.getElementById('farm-map-container');
    if (!mapContainer) {
      console.warn('⚠️ Farm map container not found');
      return;
    }

    const defaultCenter = { lat: 7.3697, lng: 12.3547 }; // Cameroon center

    this.map = new google.maps.Map(mapContainer, {
      center: defaultCenter,
      zoom: 7,
      mapTypeControl: true,
      streetViewControl: false,
      fullscreenControl: true
    });

    this.marker = new google.maps.Marker({
      map: this.map,
      draggable: true,
      animation: google.maps.Animation.DROP,
      title: 'Farm Location',
      visible: false
    });

    if (!this.geocoder) {
      this.geocoder = new google.maps.Geocoder();
    }

    this.initFarmLocationAutocomplete();

    this.map.addListener('click', (event: any) => {
      this.placeFarmMarker(event.latLng);
    });

    this.marker.addListener('dragend', () => {
      this.updateFarmCoordinatesFromMarker();
    });
  }

  private initFarmLocationAutocomplete(): void {
    const searchInput = document.getElementById('farm-location-search') as HTMLInputElement;
    if (!searchInput) return;

    this.autocomplete = new google.maps.places.Autocomplete(searchInput, {
      types: ['geocode', 'establishment'],
      componentRestrictions: { country: 'cm' }
    });

    this.autocomplete.addListener('place_changed', () => {
      const place = this.autocomplete.getPlace();

      if (!place.geometry) {
        console.warn('⚠️ Farm location not found');
        return;
      }

      this.map.setCenter(place.geometry.location);
      this.map.setZoom(15);
      this.placeFarmMarker(place.geometry.location);
    });
  }

  private placeFarmMarker(location: any): void {
    this.marker.setPosition(location);
    this.marker.setVisible(true);
    this.updateFarmCoordinatesFromMarker();
  }

  private updateFarmCoordinatesFromMarker(): void {
    const position = this.marker.getPosition();
    if (position) {
      const lat = position.lat();
      const lng = position.lng();
      const coordinates = `${lat.toFixed(6)},${lng.toFixed(6)}`;

      this.memberForm.patchValue({
        farmGpsCoordinates: coordinates
      }, { emitEvent: false });

      console.log('📍 Farm coordinates updated:', coordinates);
    }
  }

  private cleanupGoogleMaps(): void {
    if (this.map) {
      google.maps.event.clearInstanceListeners(this.map);
      this.map = null;
    }
    if (this.marker) {
      this.marker.setMap(null);
      this.marker = null;
    }
    if (this.autocomplete) {
      google.maps.event.clearInstanceListeners(this.autocomplete);
      this.autocomplete = null;
    }
  }

  openModal(): void {
    this.isModalOpen = true;
    this.selectedMemberType = 'FARMER';
    this.memberForm.reset({
      type: 'FARMER',
      phoneNumber: '+237 ',
      preferredLanguage: 'fr',
      agriculturalType: '',
      cropTypes: []
    });
    document.body.style.overflow = 'hidden';

    // Initialize map after modal opens
    this.mapLoadTimeout = setTimeout(() => {
      this.initMapInstance();
    }, 300);
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.isSubmitting = false;
    this.memberForm.reset();
    this.cleanupGoogleMaps();
    document.body.style.overflow = 'auto';
    this.modalClosed.emit();
  }

  onSubmit(): void {
    console.log('📝 Form submitted');
    console.log('✅ Form valid:', this.memberForm.valid);
    console.log('📊 Form values:', this.memberForm.value);

    if (this.memberForm.valid) {
      this.isSubmitting = true;
      const formData = this.memberForm.value;

      const newMember: NewMember = {
        type: formData.type,
        name: formData.name.trim(),
        email: formData.email?.trim() || undefined,
        phoneNumber: formData.phoneNumber.replace(/\s/g, ''),
        region: formData.region,
        department: formData.department?.trim() || undefined,
        district: formData.district?.trim() || undefined,
        village: formData.village?.trim() || undefined,
        preferredLanguage: formData.preferredLanguage
      };

      // Add farm GPS coordinates for farmers
      if (formData.type === 'FARMER' && formData.farmGpsCoordinates) {
        newMember.gpsCoordinates = formData.farmGpsCoordinates;
      }

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

      console.log('🚀 Emitting new member:', newMember);
      this.memberAdded.emit(newMember);

      setTimeout(() => {
        this.closeModal();
      }, 1000);
    } else {
      console.log('❌ Form is invalid');
      this.memberForm.markAllAsTouched();

      // Log validation errors
      Object.keys(this.memberForm.controls).forEach(key => {
        const control = this.memberForm.get(key);
        if (control?.invalid) {
          console.log(`❌ ${key} is invalid:`, control.errors);
        }
      });
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const control = this.memberForm.get(fieldName);
    return !!(control && control.invalid && control.touched);
  }

  toggleCrop(crop: string): void {
    const cropTypes = this.memberForm.get('cropTypes')?.value || [];
    const index = cropTypes.indexOf(crop);

    if (index > -1) {
      cropTypes.splice(index, 1);
    } else {
      cropTypes.push(crop);
    }

    this.memberForm.patchValue({ cropTypes }, { emitEvent: false });
  }

  isCropSelected(crop: string): boolean {
    const cropTypes = this.memberForm.get('cropTypes')?.value || [];
    return cropTypes.includes(crop);
  }

  canAddCooperative(): boolean {
    return this.userRole === 'ADMIN';
  }

  clearFarmLocation(): void {
    this.memberForm.patchValue({
      farmLocationSearch: '',
      farmGpsCoordinates: ''
    }, { emitEvent: false });

    if (this.marker) {
      this.marker.setVisible(false);
    }
  }
}