// src/app/modules/cooperative/members/member-form/member-form.component.ts
// ✅ COMPLETE FIXED VERSION WITH WORKING GOOGLE MAPS

import { Component, EventEmitter, Output, OnInit, AfterViewInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { environment } from '../../../../../environments/environment';

declare global {
  interface Window {
    google: any;
    initMap: () => void;
  }
}

export interface NewMember {
  type: 'FARMER' | 'COOPERATIVE';
  name: string;
  email?: string;
  phoneNumber: string;
  region: string;
  department?: string;
  district?: string;
  village?: string;
  gpsCoordinates?: string;
  preferredLanguage: string;
  agriculturalType?: string;
  cropTypes?: string[];
  landArea?: number;
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

  // ✅ Google Maps variables
  private map: any = null;
  private marker: any = null;
  private autocomplete: any = null;
  private geocoder: any = null;
  private mapInitialized = false;
  private initAttempts = 0;
  private maxInitAttempts = 10;

  userRole: string = 'COOPERATIVE';
  selectedMemberType: 'FARMER' | 'COOPERATIVE' = 'FARMER';

  // Options
  regionOptions = ['ADAMAOUA', 'CENTRE', 'EST', 'EXTREME_NORD', 'LITTORAL',
                   'NORD', 'NORD_OUEST', 'OUEST', 'SUD', 'SUD_OUEST'];
  agriculturalTypeOptions = ['CROP', 'LIVESTOCK', 'MIXED'];
  cropOptions = ['COCOA', 'COFFEE', 'MAIZE', 'CASSAVA', 'RICE', 'COTTON',
                 'PALM_OIL', 'PLANTAINS', 'BANANAS', 'BEANS'];
  cooperativeTypeOptions = ['PRODUCTION', 'MARKETING', 'CREDIT', 'CONSUMER', 'MULTIPURPOSE'];

  constructor(
    private fb: FormBuilder,
    private ngZone: NgZone
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadGoogleMapsScript();
  }

  ngAfterViewInit(): void {
    // Map will be initialized when modal opens
  }

  ngOnDestroy(): void {
    this.cleanupGoogleMaps();
  }

  private initForm(): void {
    this.memberForm = this.fb.group({
      type: ['FARMER', Validators.required],
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\+237\s?[6-9]\d{8}$/)]],
      region: ['', Validators.required],
      department: [''],
      district: [''],
      village: [''],
      preferredLanguage: ['fr', Validators.required],

      // Farm location
      farmLocationSearch: [''],
      farmGpsCoordinates: [''],
      farmFullAddress: [''],

      // Farmer fields
      agriculturalType: [''],
      cropTypes: [[]],
      landArea: [null, [Validators.min(0.1)]],

      // Cooperative fields
      cooperativeType: [''],
      legalRegistrationNumber: [''],
      establishmentYear: [null, [Validators.min(1900), Validators.max(new Date().getFullYear())]],
      contactPerson: ['']
    });

    this.memberForm.get('type')?.valueChanges.subscribe(type => {
      this.selectedMemberType = type;
      this.updateValidators();
    });

    this.updateValidators();
  }

  private updateValidators(): void {
    const farmGpsControl = this.memberForm.get('farmGpsCoordinates');
    const agricTypeControl = this.memberForm.get('agriculturalType');
    const landAreaControl = this.memberForm.get('landArea');
    const coopTypeControl = this.memberForm.get('cooperativeType');
    const legalRegControl = this.memberForm.get('legalRegistrationNumber');
    const contactPersonControl = this.memberForm.get('contactPerson');

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

    [farmGpsControl, agricTypeControl, landAreaControl, coopTypeControl, legalRegControl, contactPersonControl]
      .forEach(control => control?.updateValueAndValidity({ emitEvent: false }));
  }

  // ===== GOOGLE MAPS INTEGRATION - COMPLETE FIX =====

  private loadGoogleMapsScript(): void {
    // Check if already loaded
    if (window.google && window.google.maps) {
      console.log('✅ Google Maps already loaded');
      return;
    }

    // Check if script already exists
    if (document.querySelector('script[src*="maps.googleapis.com"]')) {
      console.log('⏳ Google Maps script already loading...');
      return;
    }

    console.log('📍 Loading Google Maps script...');

    const script = document.createElement('script');
    const apiKey = environment.googleMapsApiKey || 'AIzaSyB41DRUbKWJHPxaFjMAwdrzWzbVKartNGg';

    // Set callback before adding script
    window.initMap = () => {
      console.log('✅ Google Maps loaded successfully');
      this.geocoder = new window.google.maps.Geocoder();
    };

    script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places&callback=initMap`;
    script.async = true;
    script.defer = true;

    script.onerror = () => {
      console.error('❌ Failed to load Google Maps');
    };

    document.head.appendChild(script);
  }

  private async initMapInstance(): Promise<void> {
    console.log('🗺️ Attempting to initialize map...');

    // Wait for Google Maps to be ready
    if (!window.google || !window.google.maps) {
      console.log('⏳ Waiting for Google Maps to load...');
      if (this.initAttempts < this.maxInitAttempts) {
        this.initAttempts++;
        setTimeout(() => this.initMapInstance(), 500);
        return;
      } else {
        console.error('❌ Google Maps failed to load after maximum attempts');
        return;
      }
    }

    // Wait for map container
    const mapContainer = document.getElementById('farm-map-container');
    if (!mapContainer) {
      console.log('⏳ Waiting for map container...');
      if (this.initAttempts < this.maxInitAttempts) {
        this.initAttempts++;
        setTimeout(() => this.initMapInstance(), 300);
        return;
      } else {
        console.error('❌ Map container not found after maximum attempts');
        return;
      }
    }

    // All conditions met, create the map
    this.ngZone.run(() => {
      try {
        console.log('✅ Creating map instance...');

        const cameroonCenter = { lat: 5.9631, lng: 10.1591 };

        this.map = new window.google.maps.Map(mapContainer, {
          center: cameroonCenter,
          zoom: 6,
          mapTypeControl: true,
          streetViewControl: false,
          fullscreenControl: true,
          styles: [
            {
              featureType: 'poi',
              elementType: 'labels',
              stylers: [{ visibility: 'on' }]
            }
          ]
        });

        this.marker = new window.google.maps.Marker({
          map: this.map,
          draggable: true,
          animation: window.google.maps.Animation.DROP,
          title: 'Farm Location',
          visible: false
        });

        // Event listeners
        this.map.addListener('click', (event: any) => {
          this.ngZone.run(() => {
            this.placeFarmMarker(event.latLng);
          });
        });

        this.marker.addListener('dragend', () => {
          this.ngZone.run(() => {
            this.updateFarmCoordinatesFromMarker();
          });
        });

        this.mapInitialized = true;
        console.log('✅ Map initialized successfully');

        // Initialize autocomplete
        this.initFarmLocationAutocomplete();

      } catch (error) {
        console.error('❌ Error creating map:', error);
      }
    });
  }

  private initFarmLocationAutocomplete(): void {
    const searchInput = document.getElementById('farm-location-search') as HTMLInputElement;
    if (!searchInput || !window.google || !window.google.maps) {
      console.warn('⚠️ Cannot initialize autocomplete');
      return;
    }

    try {
      this.autocomplete = new window.google.maps.places.Autocomplete(searchInput, {
        types: ['geocode', 'establishment'],
        componentRestrictions: { country: 'cm' },
        fields: ['geometry', 'formatted_address', 'name']
      });

      this.autocomplete.addListener('place_changed', () => {
        this.ngZone.run(() => {
          const place = this.autocomplete.getPlace();

          if (!place.geometry || !place.geometry.location) {
            console.warn('⚠️ No geometry found for selected place');
            return;
          }

          this.map.setCenter(place.geometry.location);
          this.map.setZoom(15);
          this.placeFarmMarker(place.geometry.location);

          this.memberForm.patchValue({
            farmFullAddress: place.formatted_address || place.name
          }, { emitEvent: false });

          console.log('✅ Location selected from autocomplete');
        });
      });

      console.log('✅ Autocomplete initialized');
    } catch (error) {
      console.error('❌ Error initializing autocomplete:', error);
    }
  }

  private placeFarmMarker(location: any): void {
    if (!this.marker || !this.map) return;

    this.marker.setPosition(location);
    this.marker.setVisible(true);
    this.map.panTo(location);
    this.updateFarmCoordinatesFromMarker();
  }

  private updateFarmCoordinatesFromMarker(): void {
    if (!this.marker) return;

    const position = this.marker.getPosition();
    if (position) {
      const lat = position.lat();
      const lng = position.lng();
      const coordinates = `${lat.toFixed(6)},${lng.toFixed(6)}`;

      this.memberForm.patchValue({
        farmGpsCoordinates: coordinates
      }, { emitEvent: false });

      console.log('📍 Farm GPS coordinates updated:', coordinates);
    }
  }

  clearFarmLocation(): void {
    this.memberForm.patchValue({
      farmLocationSearch: '',
      farmGpsCoordinates: '',
      farmFullAddress: ''
    }, { emitEvent: false });

    if (this.marker) {
      this.marker.setVisible(false);
    }

    if (this.map) {
      this.map.setCenter({ lat: 5.9631, lng: 10.1591 });
      this.map.setZoom(6);
    }
  }

  private cleanupGoogleMaps(): void {
    if (this.map && window.google) {
      window.google.maps.event.clearInstanceListeners(this.map);
      this.map = null;
    }
    if (this.marker) {
      this.marker.setMap(null);
      this.marker = null;
    }
    if (this.autocomplete && window.google) {
      window.google.maps.event.clearInstanceListeners(this.autocomplete);
      this.autocomplete = null;
    }
    this.mapInitialized = false;
    this.initAttempts = 0;
  }

  // ===== MODAL MANAGEMENT =====

  openModal(): void {
    console.log('🔓 Opening member form modal...');
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

    // ✅ Initialize map after a delay to ensure DOM is ready
    this.initAttempts = 0;
    setTimeout(() => {
      if (this.isModalOpen) {
        this.initMapInstance();
      }
    }, 500);
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.isSubmitting = false;
    this.memberForm.reset();
    this.cleanupGoogleMaps();
    document.body.style.overflow = 'auto';
    this.modalClosed.emit();
  }

  // ===== FORM SUBMISSION =====

  onSubmit(): void {
    console.log('📝 Form submitted');

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

      // ✅ Add farm GPS coordinates for farmers
      if (formData.type === 'FARMER' && formData.farmGpsCoordinates) {
        newMember.gpsCoordinates = formData.farmGpsCoordinates;
      }

      // Type-specific fields
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

      Object.keys(this.memberForm.controls).forEach(key => {
        const control = this.memberForm.get(key);
        if (control?.invalid) {
          console.log(`❌ ${key} is invalid:`, control.errors);
        }
      });
    }
  }

  // ===== HELPER METHODS =====

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
}
