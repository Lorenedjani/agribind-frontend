import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

interface Loan {
  id: string;
  borrower: string;
  amount: string;
  interestRate: string;
  duration: string;
  status: string;
  startDate: string;
  dueDate: string;
  amountPaid: string;
  amountRemaining: string;
}

interface LoanStat {
  title: string;
  value: string;
  change: string;
  icon: string;
  color: string;
}

@Component({
  selector: 'app-microcredit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './microcredit.component.html',
  styleUrls: ['./microcredit.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class MicrocreditComponent implements OnInit {
  user = {
    name: '',
    role: '',
    initials: '',
    cooperativeId: ''
  };

  searchQuery: string = '';
  selectedStatus: string = 'all';

  // Modal states
  showNewLoanModal = false;
  showViewLoanModal = false;
  showEditLoanModal = false;
  selectedLoan: Loan | null = null;

  // Form data for new loan
  loanForm = {
    borrower: '',
    amount: '',
    interestRate: '5',
    duration: '12',
    startDate: new Date().toISOString().split('T')[0],
    dueDate: '',
    purpose: '',
    guarantor: '',
    collateral: ''
  };

  loanStats: LoanStat[] = [
    {
      title: 'Total Loans',
      value: '156',
      change: '+12 this month',
      icon: '💵',
      color: 'bg-primary'
    },
    {
      title: 'Total Amount Lent',
      value: '45.8M XAF',
      change: '+8.5%',
      icon: '💰',
      color: 'bg-success'
    },
    {
      title: 'Active Loans',
      value: '89',
      change: '57% active',
      icon: '📋',
      color: 'bg-info'
    },
    {
      title: 'Default Rate',
      value: '3.2%',
      change: '-0.5%',
      icon: '⚠️',
      color: 'bg-warning'
    }
  ];

  loans: Loan[] = [
    {
      id: 'LOAN-001',
      borrower: 'Jean Baptiste',
      amount: '500,000 XAF',
      interestRate: '5%',
      duration: '6 months',
      status: 'active',
      startDate: '2025-07-01',
      dueDate: '2025-12-31',
      amountPaid: '250,000 XAF',
      amountRemaining: '250,000 XAF'
    },
    {
      id: 'LOAN-002',
      borrower: 'Marie Kouam',
      amount: '750,000 XAF',
      interestRate: '5%',
      duration: '12 months',
      status: 'active',
      startDate: '2025-06-01',
      dueDate: '2026-05-31',
      amountPaid: '375,000 XAF',
      amountRemaining: '375,000 XAF'
    },
    {
      id: 'LOAN-003',
      borrower: 'Paul Mbarga',
      amount: '1,000,000 XAF',
      interestRate: '5%',
      duration: '6 months',
      status: 'completed',
      startDate: '2025-01-01',
      dueDate: '2025-06-30',
      amountPaid: '1,000,000 XAF',
      amountRemaining: '0 XAF'
    }
  ];

  filteredLoans: Loan[] = [];
  
  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalPages: number = 1;

  statusOptions = [
    { value: 'all', label: 'All Status' },
    { value: 'active', label: 'Active' },
    { value: 'completed', label: 'Completed' },
    { value: 'defaulted', label: 'Defaulted' }
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.applyFilters();
  }

  loadCurrentUser(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      let cooperativeId = currentUser.cooperativeId;
      if (!cooperativeId && currentUser.role === 'COOPERATIVE') {
        cooperativeId = currentUser.userId;
      }
      this.user = {
        name: currentUser.username || currentUser.email || 'User',
        role: this.formatRole(currentUser.role),
        initials: this.getInitials(currentUser.username || currentUser.email || 'User'),
        cooperativeId: cooperativeId || ''
      };
    }
  }

  formatRole(role: string): string {
    const roleMap: { [key: string]: string } = {
      'COOPERATIVE': 'Cooperative Manager',
      'FARMER': 'Farmer',
      'GOVERNMENT': 'Government Official'
    };
    return roleMap[role] || role;
  }

  getInitials(name: string): string {
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  get allFilteredLoans(): Loan[] {
    let filtered = [...this.loans];
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(loan =>
        loan.id.toLowerCase().includes(query) ||
        loan.borrower.toLowerCase().includes(query)
      );
    }
    if (this.selectedStatus !== 'all') {
      filtered = filtered.filter(loan => loan.status === this.selectedStatus);
    }
    return filtered;
  }
  
  get paginatedLoans(): Loan[] {
    const filtered = this.allFilteredLoans;
    this.totalPages = Math.ceil(filtered.length / this.itemsPerPage);
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return filtered.slice(startIndex, endIndex);
  }

  applyFilters(): void {
    this.currentPage = 1;
  }
  
  get Math() {
    return Math;
  }

  onSearchChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }

  onStatusChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }
  
  // Pagination methods
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(1, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages, start + maxVisible - 1);
    
    if (end - start < maxVisible - 1) {
      start = Math.max(1, end - maxVisible + 1);
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  // Helper methods for template
  getPaginationStart(): number {
    return (this.currentPage - 1) * this.itemsPerPage + 1;
  }

  getPaginationEnd(): number {
    return Math.min(this.currentPage * this.itemsPerPage, this.allFilteredLoans.length);
  }

  getTotalItems(): number {
    return this.allFilteredLoans.length;
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'active':
        return 'status-success';
      case 'completed':
        return 'status-info';
      case 'defaulted':
        return 'status-danger';
      default:
        return 'status-default';
    }
  }

  newLoan(): void {
    this.resetLoanForm();
    this.showNewLoanModal = true;
  }

  viewLoan(loan: Loan): void {
    this.selectedLoan = loan;
    this.showViewLoanModal = true;
  }

  editLoan(loan: Loan): void {
    this.selectedLoan = loan;
    this.populateEditForm(loan);
    this.showEditLoanModal = true;
  }

  deleteLoan(loan: Loan): void {
    if (confirm(`Are you sure you want to delete the loan for ${loan.borrower}?`)) {
      const index = this.loans.findIndex(l => l.id === loan.id);
      if (index > -1) {
        this.loans.splice(index, 1);
        this.applyFilters();
        alert('Loan deleted successfully!');
      }
    }
  }

  // Modal management methods
  closeNewLoanModal(): void {
    this.showNewLoanModal = false;
    this.resetLoanForm();
  }

  closeViewLoanModal(): void {
    this.showViewLoanModal = false;
    this.selectedLoan = null;
  }

  closeEditLoanModal(): void {
    this.showEditLoanModal = false;
    this.selectedLoan = null;
    this.resetLoanForm();
  }

  // Form management methods
  resetLoanForm(): void {
    const today = new Date();
    const dueDate = new Date(today);
    dueDate.setMonth(today.getMonth() + 12); // Default 12 months

    this.loanForm = {
      borrower: '',
      amount: '',
      interestRate: '5',
      duration: '12',
      startDate: today.toISOString().split('T')[0],
      dueDate: dueDate.toISOString().split('T')[0],
      purpose: '',
      guarantor: '',
      collateral: ''
    };
  }

  populateEditForm(loan: Loan): void {
    // Parse the loan data to populate the form
    const amount = loan.amount.replace(/[XAF\s,]/g, '');
    const interestRate = loan.interestRate.replace(/%/g, '');
    const duration = loan.duration.replace(/\smonths/g, '');

    this.loanForm = {
      borrower: loan.borrower,
      amount: amount,
      interestRate: interestRate,
      duration: duration,
      startDate: loan.startDate,
      dueDate: loan.dueDate,
      purpose: 'Working Capital', // Default, could be enhanced
      guarantor: '',
      collateral: ''
    };
  }

  // Form submission methods
  submitLoan(): void {
    if (!this.validateLoanForm()) {
      return;
    }

    console.log('Submitting loan:', this.loanForm);

    // Calculate amount remaining (initially full amount)
    const amountRemaining = parseFloat(this.loanForm.amount);

    // Create new loan
    const newLoan: Loan = {
      id: `LOAN-${String(this.loans.length + 1).padStart(3, '0')}`,
      borrower: this.loanForm.borrower,
      amount: `${this.formatAmount(this.loanForm.amount)} XAF`,
      interestRate: `${this.loanForm.interestRate}%`,
      duration: `${this.loanForm.duration} months`,
      status: 'Active',
      startDate: this.loanForm.startDate,
      dueDate: this.loanForm.dueDate,
      amountPaid: '0 XAF',
      amountRemaining: `${this.formatAmount(amountRemaining.toString())} XAF`
    };

    this.loans.unshift(newLoan);
    this.applyFilters();
    this.closeNewLoanModal();

    alert('Loan application submitted successfully!');
  }

  updateLoan(): void {
    if (!this.validateLoanForm() || !this.selectedLoan) {
      return;
    }

    console.log('Updating loan:', this.selectedLoan.id, this.loanForm);

    // Find and update the loan
    const index = this.loans.findIndex(l => l.id === this.selectedLoan!.id);
    if (index > -1) {
      this.loans[index] = {
        ...this.loans[index],
        borrower: this.loanForm.borrower,
        amount: `${this.formatAmount(this.loanForm.amount)} XAF`,
        interestRate: `${this.loanForm.interestRate}%`,
        duration: `${this.loanForm.duration} months`,
        startDate: this.loanForm.startDate,
        dueDate: this.loanForm.dueDate
      };

      this.applyFilters();
      this.closeEditLoanModal();

      alert('Loan updated successfully!');
    }
  }

  // Validation methods
  validateLoanForm(): boolean {
    if (!this.loanForm.borrower || !this.loanForm.amount || !this.loanForm.startDate || !this.loanForm.dueDate) {
      alert('Please fill in all required fields.');
      return false;
    }

    const amount = parseFloat(this.loanForm.amount);
    if (isNaN(amount) || amount <= 0) {
      alert('Please enter a valid loan amount.');
      return false;
    }

    const interestRate = parseFloat(this.loanForm.interestRate);
    if (isNaN(interestRate) || interestRate < 0) {
      alert('Please enter a valid interest rate.');
      return false;
    }

    const duration = parseInt(this.loanForm.duration);
    if (isNaN(duration) || duration <= 0) {
      alert('Please enter a valid loan duration.');
      return false;
    }

    return true;
  }

  // Helper methods
  formatAmount(amount: string): string {
    const num = parseFloat(amount);
    return num.toLocaleString('fr-FR');
  }
}
