import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Production } from '../production-list/production-list.component';

@Component({
  selector: 'app-production-form',
  templateUrl: './production-form.component.html',
  styleUrls: ['./production-form.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class ProductionFormComponent implements OnInit {
  @Output() productionAdded = new EventEmitter<any>();
  @Output() productionUpdated = new EventEmitter<Production>();
  @Output() modalClosed = new EventEmitter<void>();

  isModalOpen = false;
  isSubmitting = false;
  isEditMode = false;
  productionForm!: FormGroup;
  editingProduction?: Production;

  // Sample farmers list (in real app, this would come from a service)
  farmers = [
    { id: 'M001', name: 'Kwame Osei' },
    { id: 'M002', name: 'Anna Boaleng' },
    { id: 'M003', name: 'Yann Mensah' },
    { id: 'M004', name: 'Akosua Darko' }
  ];

  products = [
    'Cocoa Beans',
    'Coffee Beans',
    'Palm Oil',
    'Cotton',
    'Maize',
    'Cassava',
    'Plantains',
    'Rice'
  ];

  units = ['kg', 'L', 'MT', 'bags'];

  qualities = ['Grade A', 'Grade B', 'Grade C', 'Premium'];

  regions = [
    'North West',
    'South West',
    'Littoral',
    'Centre',
    'North',
    'West',
    'Far North',
    'Adamawa',
    'South',
    'East'
  ];

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.productionForm = this.fb.group({
      farmerId: ['', Validators.required],
      farmerName: ['', Validators.required],
      product: ['', Validators.required],
      quantity: ['', [Validators.required, Validators.min(1)]],
      unit: ['kg', Validators.required],
      quality: ['', Validators.required],
      harvestDate: ['', Validators.required],
      location: ['', Validators.required],
      status: ['Pending', Validators.required],
      value: ['', [Validators.required, Validators.min(0)]],
      notes: ['']
    });

    // Update farmer name when farmer is selected
    this.productionForm.get('farmerId')?.valueChanges.subscribe(farmerId => {
      const farmer = this.farmers.find(f => f.id === farmerId);
      if (farmer) {
        this.productionForm.patchValue({ farmerName: farmer.name });
      }
    });
  }

  openModal(production?: Production): void {
    this.isModalOpen = true;
    this.isEditMode = !!production;
    this.editingProduction = production;

    if (production) {
      // Edit mode
      this.productionForm.patchValue({
        farmerId: production.farmerId,
        farmerName: production.farmerName,
        product: production.product,
        quantity: production.quantity,
        unit: production.unit,
        quality: production.quality,
        harvestDate: production.harvestDate,
        location: production.location,
        status: production.status,
        value: production.value,
        notes: ''
      });
    } else {
      // Add mode
      this.productionForm.reset({
        farmerId: '',
        farmerName: '',
        product: '',
        quantity: '',
        unit: 'kg',
        quality: '',
        harvestDate: new Date().toISOString().split('T')[0],
        location: '',
        status: 'Pending',
        value: '',
        notes: ''
      });
    }

    document.body.style.overflow = 'hidden';
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.isSubmitting = false;
    this.isEditMode = false;
    this.editingProduction = undefined;
    this.productionForm.reset();
    document.body.style.overflow = 'auto';
    this.modalClosed.emit();
  }

  onSubmit(): void {
    if (this.productionForm.valid) {
      const formData = this.productionForm.value;

      if (this.isEditMode && this.editingProduction) {
        // Update existing production
        const updatedProduction: Production = {
          ...this.editingProduction,
          ...formData
        };
        this.productionUpdated.emit(updatedProduction);
      } else {
        // Add new production
        this.productionAdded.emit(formData);
      }

      this.closeModal();
    } else {
      this.productionForm.markAllAsTouched();
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const control = this.productionForm.get(fieldName);
    return !!(control && control.invalid && control.touched);
  }

  getErrorMessage(fieldName: string): string {
    const control = this.productionForm.get(fieldName);
    if (control?.hasError('required')) {
      return 'This field is required';
    }
    if (control?.hasError('min')) {
      return 'Value must be greater than 0';
    }
    return '';
  }
}
