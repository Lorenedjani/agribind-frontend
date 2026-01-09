import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface AudioMessage {
  id: number;
  title: string;
  language: string;
  duration: string;
  listeners: number;
  icon: string;
  playUrl: string;
  date: Date;
  audioBlob: Blob | null;
  audioFile: File | null;
  isPlaying: boolean;
}

interface AlertMessage {
  id: string;
  type: 'weather' | 'payment' | 'price' | 'security' | 'maintenance' | 'emergency' | 'info';
  priority: 'critical' | 'warning' | 'information' | 'low';
  title: string;
  description: string;
  recipients: number;
  regions: string[];
  channels: string[];
  deliveryRate: number;
  date: Date;
  status: 'active' | 'sent' | 'draft' | 'failed' | 'scheduled';
  createdBy: string;
}

// New Interface for Resource Requests
interface ResourceRequest {
  id: string;
  resource: string;
  type: 'fertilizer' | 'seeds' | 'equipment' | 'chemicals' | 'tools' | 'other';
  quantity: string;
  unit: 'bags' | 'kg' | 'liters' | 'units' | 'packets';
  urgency: 'high' | 'medium' | 'low';
  requestedBy: string;
  suppliersMatched: number;
  date: string;
  status: 'matched' | 'pending' | 'negotiating' | 'delivered' | 'cancelled';
  requiredDate?: string;
  notes?: string;
  budgetAmount?: number;
  currency?: string;
}

// ==================== TEMPLATE INTERFACE ====================
interface Template {
  id: string;
  title: string;
  description: string;
  content: string; // ADD THIS
  type: 'text' | 'audio' | 'delivery' | 'advisory' | 'reminder' | 'meeting' | 'alert';
  category: 'engagement' | 'payment' | 'logistics' | 'technical' | 'meeting' | 'alert' | 'resource';
  language: string;
  createdDate: string;
  usageCount: number;
}


@Component({
  selector: 'app-communication-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './communication-dashboard.component.html',
  styleUrls: ['./communication-dashboard.component.scss']
})
export class CommunicationDashboardComponent implements OnInit {
  // ==================== SMS COMPOSE SECTION ====================
  smsData = {
    message: '',
    audienceSelection: 'all',
    recipientCount: 300,
    selectedTemplate: '',
    messageContent: '',
    scheduleDate: '',
    deliveryType: 'normal',
    priority: 'normal',
    scheduledTime: null as Date | null
  };

  // SMS Modal State
  showSmsModal = false;
  modalSmsData = {
    message: '',
    audienceSelection: 'all',
    recipientCount: 300,
    selectedTemplate: '',
    messageContent: '',
    scheduleDate: '',
    deliveryType: 'normal',
    priority: 'normal',
    scheduledTime: null as Date | null
  };

  // UI State
  isSending = false;
  showSuccessDialog = false;
  showPriorityOptions = true;
  
  // Costs (in XAF)
  readonly BASE_COST_PER_SMS = 50;
  readonly HIGH_PRIORITY_ADDITIONAL_COST = 10;
  readonly URGENT_PRIORITY_ADDITIONAL_COST = 20;

  // ==================== DASHBOARD METRICS ====================
  dashboardMetrics = {
    totalMessages: {
      label: 'Total Messages Sent',
      value: 5247,
      change: '45 this week',
      changeType: 'positive',
      icon: 'fas fa-comment'
    },
    audioMessages: {
      label: 'Audio Messages',
      value: 87,
      info: '5 languages',
      icon: 'fas fa-microphone'
    },
    activeAlerts: {
      label: 'Active Alerts',
      value: 23,
      status: '3 critical',
      statusType: 'critical',
      icon: 'fas fa-bell'
    },
    deliveryRate: {
      label: 'Delivery Rate',
      value: '94.5%',
      change: '+1.2%',
      changeType: 'positive',
      icon: 'fas fa-check'
    }
  };

  // ==================== ACTION BUTTONS ====================
  actionButtons = [
    { 
      name: 'Compose', 
      icon: 'fas fa-paper-plane', 
      active: true 
    },
    { 
      name: 'Audio', 
      icon: 'fas fa-microphone', 
      active: false
    },
    { 
      name: 'Alerts', 
      icon: 'fas fa-bell', 
      active: false 
    },
    { 
      name: 'Resources', 
      icon: 'fas fa-box', 
      active: false 
    },
    { 
      name: 'Templates', 
      icon: 'fas fa-file-alt', 
      active: false 
    }
  ];

  // ==================== LANGUAGE OPTIONS ====================
  languages = [
    { name: 'French', code: 'fr', selected: true, icon: 'fas fa-microphone' },
    { name: 'English', code: 'en', selected: false, icon: 'fas fa-microphone' },
    { name: 'Fulfulde', code: 'ff', selected: false, icon: 'fas fa-microphone' },
    { name: 'Ewondo', code: 'ewo', selected: false, icon: 'fas fa-microphone' },
    { name: 'Duala', code: 'dua', selected: false, icon: 'fas fa-microphone' }
  ];

  // ==================== AUDIO MESSAGES ====================
  recentMessages: AudioMessage[] = [
    {
      id: 1,
      title: 'Fertilizer Delivery Announcement',
      language: 'French',
      duration: '2:45',
      listeners: 245,
      icon: 'fas fa-microphone',
      playUrl: '#',
      date: new Date('2024-01-15'),
      audioBlob: null,
      audioFile: null,
      isPlaying: false
    },
    {
      id: 2,
      title: 'Seasonal Planting Guidelines',
      language: 'English',
      duration: '3:12',
      listeners: 189,
      icon: 'fas fa-microphone',
      playUrl: '#',
      date: new Date('2024-01-14'),
      audioBlob: null,
      audioFile: null,
      isPlaying: false
    },
    {
      id: 3,
      title: 'Weather Alert: Heavy Rainfall',
      language: 'Fulfulde',
      duration: '1:30',
      listeners: 156,
      icon: 'fas fa-microphone',
      playUrl: '#',
      date: new Date('2024-01-13'),
      audioBlob: null,
      audioFile: null,
      isPlaying: false
    }
  ];

  // ==================== ALERTS SECTION ====================
  // Alert data
  newAlert = {
    type: 'weather',
    priority: 'warning',
    title: '',
    message: '',
    audience: 'all',
    requiresAck: false,
    expiryDate: ''
  };

  // Communication channels for alert modal
  communicationChannels = [
    { name: 'SMS', icon: 'fas fa-sms', selected: true },
    { name: 'Push', icon: 'fas fa-mobile-alt', selected: true },
    { name: 'Email', icon: 'fas fa-envelope', selected: false },
    { name: 'Voice', icon: 'fas fa-phone-alt', selected: false }
  ];

  alerts: AlertMessage[] = [
    {
      id: 'ALT-001',
      type: 'weather',
      priority: 'critical',
      title: 'Heavy Rainfall Warning',
      description: 'Severe weather warnings for northern regions',
      recipients: 450,
      regions: ['North Region', 'Central Region'],
      channels: ['sms', 'push', 'voice'],
      deliveryRate: 95.2,
      date: new Date('2024-01-10'),
      status: 'active',
      createdBy: 'admin'
    },
    {
      id: 'ALT-002',
      type: 'payment',
      priority: 'warning',
      title: 'Loan Due Notification',
      description: 'Loan payment due in 3 days',
      recipients: 120,
      regions: ['All Regions'],
      channels: ['sms', 'email'],
      deliveryRate: 89.5,
      date: new Date('2024-01-09'),
      status: 'sent',
      createdBy: 'finance'
    },
    {
      id: 'ALT-003',
      type: 'price',
      priority: 'information',
      title: 'Market Price Alert',
      description: 'Cassava prices dropped by 15%',
      recipients: 320,
      regions: ['South Region', 'East Region'],
      channels: ['sms', 'push'],
      deliveryRate: 92.8,
      date: new Date('2024-01-08'),
      status: 'sent',
      createdBy: 'market'
    },
    {
      id: 'ALT-004',
      type: 'weather',
      priority: 'warning',
      title: 'Drought Warning',
      description: 'Low rainfall expected for next 2 weeks',
      recipients: 280,
      regions: ['West Region'],
      channels: ['sms', 'voice'],
      deliveryRate: 91.3,
      date: new Date('2024-01-07'),
      status: 'sent',
      createdBy: 'admin'
    },
    {
      id: 'ALT-005',
      type: 'payment',
      priority: 'information',
      title: 'Fertilizer Subsidy Reminder',
      description: 'Last day to apply for fertilizer subsidy',
      recipients: 180,
      regions: ['All Regions'],
      channels: ['sms', 'push', 'email'],
      deliveryRate: 94.7,
      date: new Date('2024-01-06'),
      status: 'active',
      createdBy: 'agriculture'
    }
  ];

  // Alert filters
  alertStatusFilter: 'all' | 'active' | 'sent' = 'all';
  alertTypeFilter: 'all' | string = 'all';
  alertPriorityFilter: 'all' | string = 'all';
  alertSearchQuery: string = '';
  
  // Alert types, priorities, statuses for filtering
  alertTypes = ['weather', 'payment', 'price', 'security', 'maintenance', 'emergency', 'info'];
  alertPriorities = ['critical', 'warning', 'information', 'low'];
  alertStatuses: ('active' | 'sent' )[] = ['active', 'sent'];

  // Alert modal state
  showAlertModal: boolean = false;

  // Alert stats
  alertStats = {
    total: 0,
    active: 0,
    sent: 0,
    critical: 0,
    byType: {
      weather: 0,
      payment: 0,
      price: 0,
      security: 0,
      maintenance: 0,
      emergency: 0,
      info: 0
    }
  };

  // ==================== RESOURCE REQUEST SECTION ====================
  // Resource request data
  newRequest = {
    type: 'fertilizer' as 'fertilizer' | 'seeds' | 'equipment' | 'chemicals',
    resourceName: '',
    quantity: '',
    unit: 'bags' as 'bags' | 'kg' | 'liters' | 'units' | 'packets',
    urgency: 'high' as 'high' | 'medium' | 'low',
    requestedBy: 'zone3',
    requiredDate: '',
    notes: '',
    hasBudget: false,
    budgetAmount: 0,
    currency: 'XAF'
  };

  
  // Resource requests data
  
   private requestCounter = 1;
// In your ngOnInit or initialization method
resourceRequests: ResourceRequest[] = [
  {
    id: 'REQ-001',
    resource: 'NPK Fertilizer',
    type: 'fertilizer',
    quantity: '500 bags',
    unit: 'bags',
    urgency: 'high',
    requestedBy: 'Zone 3 Farmers',
    suppliersMatched: 3,
    date: '2025-10-01',
    status: 'matched',
    requiredDate: '2025-10-15',
    notes: 'Need urgent delivery for planting season'
  },
  {
    id: 'REQ-002',
    resource: 'Maize Seeds',
    type: 'seeds',
    quantity: '1000 packets',
    unit: 'packets',
    urgency: 'medium',
    requestedBy: 'Cooperative A',
    suppliersMatched: 2,
    date: '2025-09-25',
    status: 'matched',
    requiredDate: '2025-10-10',
    notes: 'Hybrid maize seeds preferred'
  },
  {
    id: 'REQ-003',
    resource: 'Tractor',
    type: 'equipment',
    quantity: '1 unit',
    unit: 'units',
    urgency: 'low',
    requestedBy: 'Individual Farmer',
    suppliersMatched: 1,
    date: '2025-09-20',
    status: 'pending',
    requiredDate: '2025-11-01',
    notes: 'Need for land preparation'
  }
];

// Initialize filtered requests
  generateRequestId(): string {
    const id = `REQ-${this.requestCounter.toString().padStart(3, '0')}`;
    this.requestCounter++;
    return id;
  }

  // Resource request filters
  resourceSearchQuery: string = '';
  currentPage: number = 1;
  pageSize: number = 10;
  
  // Resource modal state
  showResourceModal: boolean = false;

  // ==================== AUDIO MODAL STATE ====================
  showAudioModal: boolean = false;
  
  // Modal form data
  modalData = {
    messageTitle: '',
    selectedLanguage: 'French',
    languageCode: 'fr',
    targetAudience: 'All Members (300)',
    autoPlayEnabled: true,
    includeSmsTranscription: true,
    isRecording: false,
    recordingTime: 0,
    audioFile: null as File | null,
    recordingTimer: null as any
  };

  // Recording state
  mediaRecorder: MediaRecorder | null = null;
  audioChunks: Blob[] = [];
  audioBlob: Blob | null = null;

  // Audio player state
  currentPlayingAudio: HTMLAudioElement | null = null;
  currentPlayingMessageId: number | null = null;

  // Date utility
  today: string = new Date().toISOString().split('T')[0];

  // ==================== TEMPLATES SECTION ====================
  // Templates Data
  templates: Template[] = [
    {
    id: 'TMP001',
    title: 'Payment Reminder',
    description: 'Gentle reminder about upcoming payment deadline for [product]...',
    content: 'Dear (name), this is a reminder that your payment of (amount) for (product) is due on (date). Your balance is (balance).',
    type: 'reminder',
    category: 'payment',
    language: 'French',
    createdDate: '2024-01-15',
    usageCount: 42
  },
    {
    id: 'TMP002',
    title: 'Meeting Invitation',
    description: 'You are invited to a cooperative meeting at [venue] on [date] at [time]...',
    category: 'meeting',
    language: 'fr',
    content: 'Dear (name), you are invited to our meeting at (venue) on (date) at (time). Please bring your ID (id).',
    type: 'text',
    createdDate: '2024-01-20',
    usageCount: 18
  },
    {
    id: 'TMP003',
    title: 'Delivery Confirmation',
    description: 'Your order of [quantity] [product] has been delivered to [location]...',
    category: 'resource',
    language: 'fr',
    content: 'Hello (name), your order of (quantity) (product) has been delivered to (location). Order ID: (id).',
    type: 'text',
    createdDate: '2024-01-25',
    usageCount: 56
  },
    {
      id: 'TMP004',
      title: 'Meeting Announcement',
      description: '📅 IMPORTANT: Cooperative meeting on [Date] at [Time]. Location: [Venue]. Your attendance is required.',
      category: 'meeting',
      language: 'fr',
      content: 'Dear (name), you are invited to our meeting at (venue) on (date) at (time). Please bring your ID (id).',
      type: 'text',
      createdDate: '2024-01-25',
      usageCount: 19
    },
    {
    id: 'TMP005',
    title: 'Product Delivery Notice',
    description: '🚚 DELIVERY UPDATE: Your order of [Product] will be delivered on [Date]. Please be available to receive.',
    content: 'Your order of (product) will be delivered on (date). Please be available at (location) to receive your delivery.',
    type: 'delivery',
    category: 'logistics',
    language: 'French',
    createdDate: '2024-02-05',
    usageCount: 15
  },
    {
    id: 'TMP006',
    title: 'Crop Advisory',
    description: '🌱 CROP ADVISORY: Recommended fertilizer application for [Crop] this season: [Fertilizer Type] at [Rate].',
    content: 'Recommended fertilizer application for (product) this season: (product) at (quantity). Apply to your field at (location).',
    type: 'advisory',
    category: 'technical',
    language: 'Multiple',
    createdDate: '2024-02-10',
    usageCount: 22
  },
    
  ];

  // Template categories for filtering
  templateCategories = [
    { id: 'all', name: 'All Templates', count: 8 },
    { id: 'engagement', name: 'Engagement', count: 1 },
    { id: 'financial', name: 'Financial', count: 1 },
    { id: 'alert', name: 'Alerts', count: 1 },
    { id: 'announcement', name: 'Announcements', count: 1 },
    { id: 'logistics', name: 'Logistics', count: 1 },
    { id: 'technical', name: 'Technical', count: 1 },
    { id: 'information', name: 'Information', count: 1 },
    { id: 'education', name: 'Education', count: 1 }
  ];

  // Template types for filtering
  templateTypes = [
    { id: 'all', name: 'All Types' },
    { id: 'welcome', name: 'Welcome' },
    { id: 'payment', name: 'Payment' },
    { id: 'weather', name: 'Weather' },
    { id: 'meeting', name: 'Meeting' },
    { id: 'delivery', name: 'Delivery' },
    { id: 'advisory', name: 'Advisory' },
    { id: 'market', name: 'Market' },
    { id: 'training', name: 'Training' }
  ];

  // New template form
  newTemplate: Template = {
    id: '',
    title: '',
    description: '',
    content: '',
    type: 'text',
    category: 'engagement',
    language: 'English',
    createdDate: '',
    usageCount: 0
  };

  

  // Template modal states
  showTemplateModal: boolean = false;
  isEditingTemplate: boolean = false;
  editingTemplateId: string = '';

  // Template search and filter
  templateSearchQuery: string = '';
  selectedCategory: string = 'all';
  selectedType: string = 'all';
  selectedLanguage: string = 'all';

  // Languages for templates
  templateLanguages = [
    { code: 'all', name: 'All Languages' },
    { code: 'english', name: 'English' },
    { code: 'french', name: 'French' },
    { code: 'multiple', name: 'Multiple' }
  ];

  constructor() { }

  ngOnInit(): void {
    // Initialize with French selected by default
    this.selectLanguage('French');
    // Calculate alert stats
    this.calculateAlertStats();
    // Initialize template IDs
    this.generateTemplateIds();
  }

  // ==================== TEMPLATE SECTION METHODS ====================
 /**
   * Open template creation modal
   */
  // Add these properties to your component
templateData = {
  name: '',
  category: '',
  language: '',
  content: ''
};
templatePreview = '';

// Add these methods to your component
openTemplateModal(): void {
  this.showTemplateModal = true;
  this.resetTemplateData();
}


resetTemplateData(): void {
  this.templateData = {
    name: '',
    category: '',
    language: '',
    content: ''
  };
  this.templatePreview = '';
}

insertVariable(variable: string): void {
  const textarea = document.querySelector('.message-editor') as HTMLTextAreaElement;
  if (textarea) {
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const text = textarea.value;
    const before = text.substring(0, start);
    const after = text.substring(end);
    
    this.templateData.content = before + variable + ' ' + after;
    this.updateTemplatePreview();
    
    // Set cursor position after inserted variable
    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(start + variable.length + 1, start + variable.length + 1);
    }, 0);
  } else {
    this.templateData.content += variable + ' ';
    this.updateTemplatePreview();
  }
}

updateTemplatePreview(): void {
  // Replace variables with example values
  let preview = this.templateData.content;
  
  // Replace all variable placeholders with example values
  const variableExamples = {
    '(name)': 'Jean Dupont',
    '(id)': 'FARM12345',
    '(balance)': '15,000 XAF',
    '(amount)': '5,000 XAF',
    '(date)': '15 Mars 2024',
    '(time)': '14:30',
    '(location)': 'Zone 3',
    '(product)': 'NPK Fertilizer',
    '(quantity)': '5 sacs',
    '(venue)': 'Cooperative Office'
  };
  
  Object.entries(variableExamples).forEach(([variable, example]) => {
    preview = preview.replace(new RegExp(this.escapeRegExp(variable), 'g'), example);
  });
  
  this.templatePreview = preview;
}

escapeRegExp(string: string): string {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

saveTemplate(): void {
  // Validate required fields
  if (!this.templateData.name.trim()) {
    alert('Please enter a template name');
    return;
  }
  
  if (!this.templateData.category) {
    alert('Please select a category');
    return;
  }
  
  if (!this.templateData.language) {
    alert('Please select a primary language');
    return;
  }
  
  if (!this.templateData.content.trim()) {
    alert('Please enter message content');
    return;
  }
  
  // Determine template type based on category
  let templateType: Template['type'] = 'text';
  switch(this.templateData.category) {
    case 'payment':
      templateType = 'reminder';
      break;
    case 'meeting':
      templateType = 'meeting';
      break;
    case 'alert':
      templateType = 'alert';
      break;
    case 'resource':
      templateType = 'delivery';
      break;
    case 'logistics':
      templateType = 'delivery';
      break;
    case 'technical':
      templateType = 'advisory';
      break;
    default:
      templateType = 'text';
  }
  
  // Determine template category
  let templateCategory: Template['category'] = 'engagement';
  switch(this.templateData.category) {
    case 'payment':
      templateCategory = 'payment';
      break;
    case 'meeting':
      templateCategory = 'meeting';
      break;
    case 'alert':
      templateCategory = 'alert';
      break;
    case 'resource':
      templateCategory = 'resource';
      break;
    case 'logistics':
      templateCategory = 'logistics';
      break;
    case 'technical':
      templateCategory = 'technical';
      break;
    default:
      templateCategory = 'engagement';
  }
  
  // Map language display name to value
  let languageValue = this.templateData.language;
  if (this.templateData.language === 'FR Français (French)') {
    languageValue = 'French';
  } else if (this.templateData.language === 'EN English') {
    languageValue = 'English';
  } else if (this.templateData.language === 'ES Español (Spanish)') {
    languageValue = 'Spanish';
  } else if (this.templateData.language === 'PT Português (Portuguese)') {
    languageValue = 'Portuguese';
  }
  
  // Create the new template with ALL required properties
  const newTemplate: Template = {
    id: 'TMP' + Date.now().toString().slice(-6),
    title: this.templateData.name,
    description: this.createTemplateDescription(this.templateData.content),
    content: this.templateData.content,
    type: templateType,
    category: templateCategory,
    language: languageValue,
    createdDate: new Date().toISOString().split('T')[0], // YYYY-MM-DD format
    usageCount: 0
  };
  
  // Add to templates array
  this.templates.unshift(newTemplate);
  
  // Show success message
  this.showSuccessNotification('Template created successfully!');
  
  // Close modal
  this.closeTemplateModal();
  
  // Reset form
  this.resetTemplateData();
}

// Helper method for success notification
showSuccessNotification(message: string): void {
  // Create notification element
  const notification = document.createElement('div');
  notification.className = 'success-notification';
  notification.innerHTML = `
    <i class="fas fa-check-circle"></i>
    <span>${message}</span>
  `;
  
  // Style the notification
  notification.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    background: #10b981;
    color: white;
    padding: 12px 20px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 10px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    z-index: 10000;
    animation: slideIn 0.3s ease;
    font-family: 'Segoe UI', sans-serif;
  `;
  
  // Add to document
  document.body.appendChild(notification);
  
  // Remove after 3 seconds
  setTimeout(() => {
    notification.style.animation = 'fadeOut 0.3s ease';
    setTimeout(() => {
      if (notification.parentNode) {
        notification.parentNode.removeChild(notification);
      }
    }, 300);
  }, 3000);
}

// Update the createTemplateDescription method to be more robust
createTemplateDescription(content: string): string {
  // Create a shortened preview of the template content
  const maxLength = 120;
  
  // Remove multiple spaces and newlines
  let cleanContent = content.replace(/\s+/g, ' ').trim();
  
  // Truncate if needed
  if (cleanContent.length <= maxLength) {
    return cleanContent;
  }
  
  // Find a good truncation point (end of sentence or space)
  let truncationPoint = maxLength;
  for (let i = maxLength; i > maxLength - 20; i--) {
    if (cleanContent[i] === ' ' || cleanContent[i] === '.' || cleanContent[i] === '!' || cleanContent[i] === '?') {
      truncationPoint = i + 1;
      break;
    }
  }
  
  return cleanContent.substring(0, truncationPoint) + '...';
}



  /**
   * Delete a template
   */
  deleteTemplate(templateId: string): void {
    if (confirm('Are you sure you want to delete this template?')) {
      const index = this.templates.findIndex(t => t.id === templateId);
      if (index !== -1) {
        this.templates.splice(index, 1);
        this.showNotification('Template deleted successfully', 'success');
      }
    }
  }

  /**
   * Close template modal
   */
 closeTemplateModal(event?: any): void {
  this.showTemplateModal = false;
  
  // Optional: You can also check if the click was on the overlay
  if (event && event.target.classList.contains('modal-overlay')) {
    console.log('Clicked outside modal to close');
  }
}

editTemplate(templateId: string): void {
  // Find the template
  const template = this.templates.find(t => t.id === templateId);
  
  if (template) {
    // Fill the template modal with the template data
    this.templateData = {
      name: template.title,
      category: template.category,
      language: template.language,
      content: template.content
    };
    
    // Show the template modal
    this.showTemplateModal = true;
    
    // Optional: Mark as editing mode
    this.isEditingTemplate = true;
    this.editingTemplateId = templateId;
    
    // Update the preview
    this.updateTemplatePreview();
    
    console.log('Editing template:', template.title);
  }
}

setActiveSection(section: string): void {
  // Only handle compose section for now
  if (section === 'compose') {
    this.isComposeSectionActive = true;
   
  }
}
loadTemplateToCompose(template: any): void {
  // Just set the message in the SMS form
  this.smsData.message = template.content;
  this.smsData.messageContent = template.content;
  
  // Optional: Show a simple alert
  alert(`Template "${template.title}" loaded!`);
  
  console.log('Template loaded:', template.title);
}
  /**
   * Filter templates based on search query and filters
   */
  get filteredTemplates(): Template[] {
    return this.templates.filter(template => {
      // Search query filter
      const matchesSearch = !this.templateSearchQuery ||
        template.title.toLowerCase().includes(this.templateSearchQuery.toLowerCase()) ||
        template.description.toLowerCase().includes(this.templateSearchQuery.toLowerCase()) ||
        template.id.toLowerCase().includes(this.templateSearchQuery.toLowerCase());

      // Category filter
      const matchesCategory = this.selectedCategory === 'all' || 
        template.category === this.selectedCategory;

      // Type filter
      const matchesType = this.selectedType === 'all' || 
        template.type === this.selectedType;

      // Language filter
      const matchesLanguage = this.selectedLanguage === 'all' || 
        template.language.toLowerCase() === this.selectedLanguage.toLowerCase();

      return matchesSearch && matchesCategory && matchesType && matchesLanguage;
    });
  }

  /**
   * Get template icon based on type
   */
  getTemplateIcon(templateType: string): string {
    const iconMap: { [key: string]: string } = {
      'welcome': 'fas fa-handshake',
      'payment': 'fas fa-money-bill-wave',
      'weather': 'fas fa-cloud-rain',
      'meeting': 'fas fa-calendar-alt',
      'delivery': 'fas fa-truck',
      'advisory': 'fas fa-seedling',
      'market': 'fas fa-chart-line',
      'training': 'fas fa-graduation-cap'
    };
    return iconMap[templateType] || 'fas fa-file-alt';
  }

  /**
   * Get category badge class
   */
  getCategoryClass(category: string): string {
    const classMap: { [key: string]: string } = {
      'engagement': 'badge-engagement',
      'financial': 'badge-financial',
      'alert': 'badge-alert',
      'announcement': 'badge-announcement',
      'logistics': 'badge-logistics',
      'technical': 'badge-technical',
      'information': 'badge-information',
      'education': 'badge-education'
    };
    return classMap[category] || 'badge-default';
  }

  /**
   * Generate a new template ID
   */
  generateTemplateId(): string {
    const lastId = this.templates.length > 0 
      ? parseInt(this.templates[0].id.replace('TMP', '')) 
      : 0;
    return `TMP${(lastId + 1).toString().padStart(3, '0')}`;
  }

  /**
   * Generate IDs for existing templates if missing
   */
  generateTemplateIds(): void {
    this.templates.forEach((template, index) => {
      if (!template.id) {
        template.id = `TMP${(index + 1).toString().padStart(3, '0')}`;
      }
    });
  }

  /**
   * Reset template filters
   */
  resetTemplateFilters(): void {
    this.templateSearchQuery = '';
    this.selectedCategory = 'all';
    this.selectedType = 'all';
    this.selectedLanguage = 'all';
  }

  /**
   * Get category name by ID
   */
  getCategoryName(categoryId: string): string {
    const category = this.templateCategories.find(c => c.id === categoryId);
    return category ? category.name : 'Unknown';
  }

  /**
   * Get type name by ID
   */
  getTypeName(typeId: string): string {
    const type = this.templateTypes.find(t => t.id === typeId);
    return type ? type.name : 'Unknown';
  }

  /**
   * Get language name by code
   */
  getLanguageName(languageCode: string): string {
    const lang = this.templateLanguages.find(l => l.code === languageCode.toLowerCase());
    return lang ? lang.name : languageCode;
  }

  /**
   * Get template statistics
   */
  getTemplateStats(): { total: number, byCategory: { [key: string]: number }, byLanguage: { [key: string]: number } } {
    const stats = {
      total: this.templates.length,
      byCategory: {} as { [key: string]: number },
      byLanguage: {} as { [key: string]: number }
    };

    this.templates.forEach(template => {
      // Count by category
      stats.byCategory[template.category] = (stats.byCategory[template.category] || 0) + 1;
      
      // Count by language
      stats.byLanguage[template.language] = (stats.byLanguage[template.language] || 0) + 1;
    });

    return stats;
  }

  /**
   * Export templates
   */
  exportTemplates(): void {
    const dataStr = JSON.stringify(this.templates, null, 2);
    const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr);
    
    const exportFileDefaultName = `templates-export-${new Date().toISOString().split('T')[0]}.json`;
    
    const linkElement = document.createElement('a');
    linkElement.setAttribute('href', dataUri);
    linkElement.setAttribute('download', exportFileDefaultName);
    linkElement.click();
    
    this.showNotification('Templates exported successfully', 'success');
  }

  /**
   * Import templates from file
   */
  importTemplates(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e: any) => {
      try {
        const importedTemplates = JSON.parse(e.target.result);
        if (Array.isArray(importedTemplates)) {
          // Generate IDs for imported templates
          importedTemplates.forEach((template, index) => {
            if (!template.id) {
              template.id = `IMP${Date.now()}${index}`;
            }
          });
          
          this.templates = [...importedTemplates, ...this.templates];
          this.showNotification('Templates imported successfully', 'success');
        } else {
          this.showNotification('Invalid template file format', 'error');
        }
      } catch (error) {
        this.showNotification('Error importing templates', 'error');
      }
    };
    reader.readAsText(file);
  }

  /**
   * Duplicate a template
   */
  duplicateTemplate(templateId: string): void {
    const template = this.templates.find(t => t.id === templateId);
    if (template) {
      const duplicatedTemplate = {
        ...template,
        id: this.generateTemplateId(),
        title: `${template.title} (Copy)`,
        createdDate: new Date().toISOString().split('T')[0],
        usageCount: 0
      };
      
      this.templates.unshift(duplicatedTemplate);
      this.showNotification('Template duplicated successfully', 'success');
    }
  }

  /**
   * Show notification
   */
  showNotification(message: string, type: 'success' | 'error' | 'info' = 'info'): void {
    alert(`${type.toUpperCase()}: ${message}`);
  }

  // ==================== GETTERS FOR SECTION VISIBILITY ====================
  

  
  get isTemplatesSectionActive(): boolean {
    const templatesBtn = this.actionButtons.find(btn => btn.name === 'Templates');
    return templatesBtn ? templatesBtn.active : false;
  }




  // ==================== RESOURCE REQUEST METHODS ====================
  get isResourceSectionActive(): boolean {
    const resourceBtn = this.actionButtons.find(btn => btn.name === 'Resources');
    return resourceBtn ? resourceBtn.active : false;
  }

  openResourceModal(): void {
    this.showResourceModal = true;
  }

  closeResourceModal(): void {
    this.showResourceModal = false;
    this.resetResourceForm();
  }

  resetResourceForm(): void {
    this.newRequest = {
      type: 'fertilizer',
      resourceName: '',
      quantity: '',
      unit: 'bags',
      urgency: 'high',
      requestedBy: 'zone3',
      requiredDate: '',
      notes: '',
      hasBudget: false,
      budgetAmount: 0,
      currency: 'XAF'
    };
  }

  onBudgetToggle(): void {
    if (!this.newRequest.hasBudget) {
      this.newRequest.budgetAmount = 0;
    }
  }
createResourceRequest(): void {
  // Validation
  if (!this.newRequest.resourceName.trim()) {
    alert('Please enter resource name');
    return;
  }

  if (!this.newRequest.quantity || isNaN(Number(this.newRequest.quantity))) {
    alert('Please enter a valid quantity');
    return;
  }

  if (!this.newRequest.requiredDate) {
    alert('Please select a required date');
    return;
  }

  // Generate new ID in REQ-001 format
  const newId = this.generateNextRequestId();

  // Get requester name
  const requesterName = this.getRequesterName(this.newRequest.requestedBy);

  // Create new request
  const newResourceRequest: ResourceRequest = {
    id: newId,
    resource: this.newRequest.resourceName,
    type: this.newRequest.type,
    quantity: `${this.newRequest.quantity} ${this.newRequest.unit}`,
    unit: this.newRequest.unit,
    urgency: this.newRequest.urgency,
    requestedBy: requesterName,
    suppliersMatched: 0,
    date: new Date().toISOString().split('T')[0],
    status: 'pending',
    requiredDate: this.newRequest.requiredDate,
    notes: this.newRequest.notes,
    ...(this.newRequest.hasBudget && {
      budgetAmount: this.newRequest.budgetAmount,
      currency: this.newRequest.currency
    })
  };

  // Add the new request to the beginning of resourceRequests
  this.resourceRequests.push(newResourceRequest);
  
  // Update filteredResourceRequests WITHOUT reassigning (modify the existing array)
  // Option 1: Clear and repopulate
  this.filteredResourceRequests.length = 0; // Clear the array
  this.filteredResourceRequests.push(...this.resourceRequests); // Add all items back
  
  
 
  // Show success message
  alert(`Resource request ${newId} created successfully!`);

  // Close modal and reset form
  this.closeResourceModal();
  this.resetNewRequestForm();
}

// Helper method to generate the next request ID
private generateNextRequestId(): string {
  if (this.resourceRequests.length === 0) {
    return 'REQ-001';
  }

  // Extract numeric part from existing IDs
  const existingIds = this.resourceRequests
    .map(request => {
      const match = request.id.match(/REQ-(\d+)/);
      return match ? parseInt(match[1]) : 0;
    })
    .filter(id => !isNaN(id));

  // Find the highest number
  const maxId = existingIds.length > 0 ? Math.max(...existingIds) : 0;
  
  // Generate next ID
  const nextNumber = maxId + 1;
  return `REQ-${nextNumber.toString().padStart(3, '0')}`;
}

// Helper method to get requester name
private getRequesterName(requesterKey: string): string {
  const requesterMap: {[key: string]: string} = {
    'zone3': 'Zone 3 Farmers',
    'coop1': 'Cooperative A',
    'coop2': 'Cooperative B',
    'individual': 'Individual Farmer',
    'group': 'Farmer Group'
  };
  
  return requesterMap[requesterKey] || requesterKey;
}

// Method to reset the form
private resetNewRequestForm(): void {
  this.newRequest = {
    type: 'fertilizer',
    resourceName: '',
    quantity: '',
    unit: 'bags',
    urgency: 'medium',
    requestedBy: 'zone3',
    requiredDate: '',
    notes: '',
    hasBudget: false,
    budgetAmount: 0,
    currency: 'XAF'
  };
}
  

  get filteredResourceRequests(): ResourceRequest[] {
    let filtered = [...this.resourceRequests];

    // Apply search filter
    if (this.resourceSearchQuery.trim()) {
      const query = this.resourceSearchQuery.toLowerCase().trim();
      filtered = filtered.filter(request => 
        request.resource.toLowerCase().includes(query) ||
        request.id.toLowerCase().includes(query) ||
        request.requestedBy.toLowerCase().includes(query) ||
        request.status.toLowerCase().includes(query) ||
        request.type.toLowerCase().includes(query)
      );
    }

    return filtered;
  }

  get totalPages(): number {
    return Math.ceil(this.filteredResourceRequests.length / this.pageSize);
  }

  getPaginationInfo(): string {
    const start = (this.currentPage - 1) * this.pageSize + 1;
    const end = Math.min(this.currentPage * this.pageSize, this.filteredResourceRequests.length);
    return `${start}-${end}`;
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

  getResourceIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'fertilizer': 'fas fa-seedling',
      'seeds': 'fas fa-leaf',
      'equipment': 'fas fa-tractor',
      'chemicals': 'fas fa-flask',
      'tools': 'fas fa-wrench',
      'other': 'fas fa-box'
    };
    return icons[type] || 'fas fa-box';
  }
  viewRequest(requestId: string): void {
    const request = this.resourceRequests.find(r => r.id === requestId);
    if (request) {
      const details = `
📦 RESOURCE REQUEST DETAILS

🔸 ID: ${request.id}
🔸 Resource: ${request.resource}
🔸 Type: ${this.capitalizeFirst(request.type)}
🔸 Quantity: ${request.quantity} ${request.unit}
🔸 Urgency: ${this.capitalizeFirst(request.urgency)}
🔸 Requested By: ${request.requestedBy}
🔸 Suppliers Matched: ${request.suppliersMatched}
🔸 Status: ${this.capitalizeFirst(request.status)}
🔸 Date: ${request.date}
${request.requiredDate ? `🔸 Required By: ${request.requiredDate}` : ''}
${request.notes ? `📝 Notes: ${request.notes}` : ''}
${request.budgetAmount ? `💰 Budget: ${request.budgetAmount} ${request.currency}` : ''}
      `;
      alert(details);
    }
  }

  matchSuppliers(requestId: string): void {
    const request = this.resourceRequests.find(r => r.id === requestId);
    if (request) {
      // Simulate matching suppliers
      const newSuppliers = Math.floor(Math.random() * 5) + 1;
      request.suppliersMatched = newSuppliers;
      request.status = 'matched';
      
      alert(`Matched ${newSuppliers} suppliers for request ${requestId}`);
    }
  }

  openResourceFilter(): void {
    // In a real app, this would open a filter dialog
    alert('Filter functionality would open here');
  }

  onResourceSearch(): void {
    // Reset to first page when searching
    this.currentPage = 1;
  }
  

  // ==================== SMS MODAL METHODS ====================
  openSmsModal(): void {
    console.log('=== DEBUG: openSmsModal START ===');
    console.log('1. Button clicked!');
    console.log('2. Form is valid?', this.isFormValid());
    console.log('3. Message content:', this.smsData.messageContent);
    console.log('4. Message length:', this.smsData.messageContent?.length || 0);
    
    if (!this.isFormValid()) {
      console.warn('❌ Form is NOT valid. Cannot open modal.');
      alert('Please enter a message first!');
      return;
    }
    
    console.log('✅ Form is valid. Opening modal...');
    
    // Copy data to modal
    this.modalSmsData = { ...this.smsData };
    this.showSmsModal = true;
    
    console.log('5. Modal should open. showSmsModal =', this.showSmsModal);
    console.log('=== DEBUG: openSmsModal END ===');
  }

  closeSmsModal(): void {
    this.showSmsModal = false;
  }

  sendFromModal(): void {
    if (!this.isModalFormValid()) {
      alert('Please fill all required fields');
      return;
    }
    
    // Copy data back from modal
    this.smsData = { ...this.modalSmsData };
    
    this.isSending = true;
    this.showSmsModal = false;
    
    // Simulate API call
    setTimeout(() => {
      this.isSending = false;
      this.showSuccessDialog = true;
      console.log('SMS sent successfully from modal');
    }, 2000);
  }

  isModalFormValid(): boolean {
    const hasMessage = this.modalSmsData.messageContent.trim().length > 0;
    const messageLengthValid = this.modalSmsData.messageContent.length <= 160;
    
    return hasMessage && messageLengthValid;
  }

  updateModalRecipientCount(): void {
    const audienceCounts: { [key: string]: number } = {
      'all': 300,
      'active': 245,
      'douala': 87,
      'yaounde': 102,
      'garoua': 56,
      'cocoa': 156,
      'coffee': 89,
      'overdue': 18
    };
    
    this.modalSmsData.recipientCount = audienceCounts[this.modalSmsData.audienceSelection] || 300;
  }

  loadModalTemplate(): void {
    const templates: { [key: string]: string } = {
      welcome: 'Welcome to our farming community! We\'re excited to have you onboard.',
      payment: 'Dear farmer, your payment is due soon. Please complete it by the end of the week.',
      weather: 'Weather alert: Heavy rainfall expected tomorrow. Take precautions.',
      meeting: 'Meeting reminder: Cooperative meeting this Friday at 10 AM.',
      delivery: 'Your order has been dispatched and will arrive soon.'
    };
    
    if (this.modalSmsData.selectedTemplate && templates[this.modalSmsData.selectedTemplate]) {
      this.modalSmsData.messageContent = templates[this.modalSmsData.selectedTemplate];
    }
  }

  updateModalMessageLength(): void {
    // Character count logic if needed
  }

  calculateModalCost(): number {
    const costPerSms = this.modalSmsData.priority === 'normal' ? 50 : 
                      this.modalSmsData.priority === 'high' ? 60 : 70;
    return this.modalSmsData.recipientCount * costPerSms;
  }

  

  // ==================== SMS COMPOSE METHODS ====================
  getCostPerSms(): number {
    switch(this.smsData.priority) {
      case 'high':
        return this.BASE_COST_PER_SMS + this.HIGH_PRIORITY_ADDITIONAL_COST;
      case 'urgent':
        return this.BASE_COST_PER_SMS + this.URGENT_PRIORITY_ADDITIONAL_COST;
      default:
        return this.BASE_COST_PER_SMS;
    }
  }

  isFormValid(): boolean {
    const hasMessage = this.smsData.messageContent.trim().length > 0;
    const hasRecipients = this.smsData.recipientCount > 0;
    const messageLengthValid = this.smsData.messageContent.length <= 160;

    console.log('Form validation:', {
      hasMessage,
      hasRecipients,
      messageLengthValid,
      messageContent: this.smsData.messageContent
    });
    
    return hasMessage && hasRecipients && messageLengthValid;
  }

  onPriorityChange(): void {
    console.log('Priority changed to:', this.smsData.priority);
  }

  scheduleSms() {
    // Check if message is empty
    if (!this.smsData.messageContent?.trim()) {
      alert('Please enter a message before scheduling');
      return;
    }
    
    // Check if date is selected
    if (!this.smsData.scheduleDate) {
      alert('Please select a schedule date first');
      return;
    }
    
    // Prepare the scheduled SMS data
    const scheduledSms = {
      audience: this.smsData.audienceSelection,
      recipientCount: this.smsData.recipientCount,
      messageContent: this.smsData.messageContent,
      scheduleDate: this.smsData.scheduleDate,
      deliveryType: this.smsData.deliveryType,
      priority: this.smsData.priority,
      estimatedCost: this.calculateEstimatedCost(),
      status: 'scheduled'
    };
    
    console.log('Scheduling SMS:', scheduledSms);
    
    alert(`SMS scheduled for ${this.formatScheduleDate()} to ${this.smsData.recipientCount} recipients`);
  }

  formatScheduleDate(): string {
    if (!this.smsData.scheduleDate) return '';
    
    const date = new Date(this.smsData.scheduleDate);
    return date.toLocaleDateString('en-US', { 
      weekday: 'long', 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  }

  resetSmsForm() {
    this.smsData = {
      ...this.smsData,
      message: '',
      audienceSelection: 'all',
      recipientCount: 300,
      selectedTemplate: '',
      messageContent: '',
      scheduleDate: '',
      deliveryType: 'normal',
      priority: 'normal',
      scheduledTime: null
    };
  }

  closeSuccessDialog() {
    this.showSuccessDialog = false;
  }

  viewSmsDetails() {
    this.showSuccessDialog = false;
    this.showSmsDetailsModal();
    console.log('Viewing SMS details...');
  }

  private showSmsDetailsModal() {
    console.log('SMS Details:', {
      recipients: this.smsData.recipientCount,
      cost: this.calculateEstimatedCost(),
      priority: this.smsData.priority,
      scheduledFor: this.smsData.scheduledTime
    });
  }

  calculateEstimatedCost(): number {
    return this.smsData.recipientCount * this.getCostPerSms();
  }

  updateRecipientCount() {
    const audienceCounts: { [key: string]: number } = {
      'all': 300,
      'active': 245,
      'douala': 87,
      'yaounde': 102,
      'garoua': 56,
      'cocoa': 156,
      'coffee': 89,
      'overdue': 18
    };
    
    this.smsData.recipientCount = audienceCounts[this.smsData.audienceSelection] || 300;
  }

  updateMessageLength() {
    // Optional validation logic
  }

  loadTemplate() {
    const templates: { [key: string]: string } = {
      welcome: 'Welcome to our farming community! {name}, we\'re excited to have you onboard.',
      payment: 'Dear {name}, your payment is due soon. Please complete it by the end of the week.',
      weather: 'Weather alert for {name}: Heavy rainfall expected tomorrow. Take precautions.',
      meeting: 'Meeting reminder {name}: Cooperative meeting this Friday at 10 AM.'
    };
    
    if (this.smsData.selectedTemplate && templates[this.smsData.selectedTemplate]) {
      this.smsData.messageContent = templates[this.smsData.selectedTemplate];
    }
  }

  getPriorityText(): string {
    if (!this.smsData.priority) return 'Normal';
    
    const priorityTexts: { [key: string]: string } = {
      'high': 'High Priority',
      'normal': 'Normal',
      'low': 'Low Priority'
    };
    
    return priorityTexts[this.smsData.priority] || 'Normal';
  }

  getDeliveryTypeText(): string {
    if (!this.smsData.deliveryType) return 'Normal';
    
    return this.smsData.deliveryType.charAt(0).toUpperCase() + 
           this.smsData.deliveryType.slice(1);
  }

  // ==================== ALERTS SECTION METHODS ====================
  getAudienceCount(): number {
    if (!this.newAlert || !this.newAlert.audience) return 0;
    
    switch (this.newAlert.audience) {
      case 'all': return 300;
      case 'region1': return 150;
      case 'region2': return 120;
      case 'region3': return 80;
      case 'region4': return 100;
      case 'custom': return 0;
      default: return 0;
    }
  }

  toggleChannel(channel: any): void {
    channel.selected = !channel.selected;
  }

  getSelectedChannels(): string[] {
    return this.communicationChannels
      .filter(channel => channel.selected)
      .map(channel => channel.name.toLowerCase());
  }

  private generateAlertId(): string {
    let maxId = 0;
    
    this.alerts.forEach(alert => {
      const idStr = alert.id;
      
      if (idStr.startsWith('ALT-')) {
        const numStr = idStr.substring(4);
        const num = parseInt(numStr, 10);
        if (!isNaN(num) && num > maxId) {
          maxId = num;
        }
      }
    });
    
    const nextId = maxId + 1;
    return `ALT-${nextId.toString().padStart(3, '0')}`;
  }

  sendAlertFromModal(): void {
    if (!this.newAlert.title.trim()) {
      window.alert('Please enter an alert title');
      return;
    }
    
    if (!this.newAlert.message.trim()) {
      window.alert('Please enter an alert message');
      return;
    }
    
    const selectedChannels = this.getSelectedChannels();
    if (selectedChannels.length === 0) {
      window.alert('Please select at least one communication channel');
      return;
    }
    
    const alertId = this.generateAlertId();
    console.log('Generated Alert ID:', alertId);
    
    const newAlert: AlertMessage = {
      id: alertId,
      type: this.newAlert.type as any,
      priority: this.newAlert.priority as any,
      title: this.newAlert.title,
      description: this.newAlert.message,
      recipients: this.getAudienceCount(),
      regions: [this.getRegionName(this.newAlert.audience)],
      channels: selectedChannels,
      deliveryRate: Math.floor(Math.random() * 20) + 80,
      date: new Date(),
      status: 'active',
      createdBy: 'current_user'
    };
    
    this.alerts.push(newAlert);
    console.log('Alerts after adding:', this.alerts.map(a => a.id));
    this.calculateAlertStats();
    this.closeAlertModal();
    
    this.newAlert = {
      type: 'weather',
      priority: 'warning',
      title: '',
      message: '',
      audience: 'all',
      requiresAck: false,
      expiryDate: ''
    };
    
    this.communicationChannels.forEach(channel => {
      channel.selected = channel.name === 'SMS' || channel.name === 'Push';
    });
    
    window.alert(`Alert ${alertId} created successfully!`);
  }

  private getRegionName(audience: string): string {
    switch (audience) {
      case 'all': return 'All Regions';
      case 'region1': return 'North Region';
      case 'region2': return 'South Region';
      case 'region3': return 'East Region';
      case 'region4': return 'West Region';
      default: return 'Custom Region';
    }
  }

  calculateAlertStats(): void {
    this.alertStats.total = this.alerts.length;
    this.alertStats.active = this.alerts.filter(a => a.status === 'active').length;
    this.alertStats.sent = this.alerts.filter(a => a.status === 'sent').length;
    this.alertStats.critical = this.alerts.filter(a => a.priority === 'critical').length;
    
    this.alertTypes.forEach(type => {
      this.alertStats.byType[type as keyof typeof this.alertStats.byType] = 
        this.alerts.filter(a => a.type === type).length;
    });
  }

  onSearch(): void {
    console.log('Searching alerts for:', this.alertSearchQuery);
  }

  get filteredAlerts(): AlertMessage[] {
    let filtered = this.alerts;
    
    if (this.alertStatusFilter !== 'all') {
      filtered = filtered.filter(alert => alert.status === this.alertStatusFilter);
    }
    
    if (this.alertSearchQuery) {
      const query = this.alertSearchQuery.toLowerCase().trim();
      
      filtered = filtered.filter(alert => {
        const searchFields = [
          alert.title.toLowerCase(),
          alert.description.toLowerCase(),
          alert.id.toLowerCase(),
          alert.type.toLowerCase(),
          alert.priority.toLowerCase(),
          ...alert.regions.map(region => region.toLowerCase()),
          ...alert.channels.map(channel => channel.toLowerCase())
        ];
        
        const priorityMap: Record<string, string[]> = {
          'critical': ['urgent', 'important', 'critical', 'high'],
          'warning': ['warning', 'alert', 'caution'],
          'information': ['info', 'information', 'notice', 'update'],
          'low': ['low', 'normal', 'routine']
        };
        
        const typeMap: Record<string, string[]> = {
          'weather': ['weather', 'rain', 'storm', 'drought', 'temperature'],
          'payment': ['payment', 'loan', 'due', 'bill', 'finance'],
          'price': ['price', 'market', 'cost', 'sell', 'buy'],
          'security': ['security', 'safety', 'theft', 'protection'],
          'maintenance': ['maintenance', 'repair', 'service', 'equipment'],
          'emergency': ['emergency', 'urgent', 'crisis', 'disaster'],
          'info': ['info', 'information', 'announcement', 'news']
        };
        
        if (searchFields.some(field => field.includes(query))) {
          return true;
        }
        
        const priorityKeywords = priorityMap[alert.priority] || [];
        if (priorityKeywords.some(keyword => query.includes(keyword))) {
          return true;
        }
        
        const typeKeywords = typeMap[alert.type] || [];
        if (typeKeywords.some(keyword => query.includes(keyword))) {
          return true;
        }
        
        return false;
      });
    }
    
    return filtered;
  }

  openAlertModal(): void {
    this.showAlertModal = true;
  }

  closeAlertModal(): void {
    this.showAlertModal = false;
    this.newAlert = {
      type: 'weather',
      priority: 'warning',
      title: '',
      message: '',
      audience: 'all',
      requiresAck: false,
      expiryDate: ''
    };
    
    this.communicationChannels.forEach(channel => {
      channel.selected = channel.name === 'SMS' || channel.name === 'Push';
    });
  }

  deleteAlert(alertId: string): void {
    if (window.confirm('Are you sure you want to delete this alert?')) {
      this.alerts = this.alerts.filter(alert => alert.id !== alertId);
      this.calculateAlertStats();
      window.alert('Alert deleted successfully!');
    }
  }

  updateAlertStatus(alertId: string, newStatus: AlertMessage['status']): void {
    const alert = this.alerts.find(a => a.id === alertId);
    if (alert) {
      alert.status = newStatus;
      alert.date = new Date();
      this.calculateAlertStats();
      window.alert(`Alert ${alertId} status updated to ${newStatus}`);
    }
  }

  getAlertIcon(type: string): string {
    const icons: Record<string, string> = {
      'weather': 'fas fa-cloud-rain',
      'payment': 'fas fa-money-bill-wave',
      'price': 'fas fa-chart-line',
      'security': 'fas fa-shield-alt',
      'maintenance': 'fas fa-tools',
      'emergency': 'fas fa-exclamation-triangle',
      'info': 'fas fa-info-circle'
    };
    return icons[type] || 'fas fa-bell';
  }

  getPriorityClass(priority: string): string {
    if (priority === 'critical') return 'critical-bg';
    if (priority === 'warning') return 'warning-bg';
    if (priority === 'information') return 'info-bg';
    if (priority === 'low') return 'low-bg';
    return 'low-bg';
  }

  getStatusClass(status: string): string {
    if (status === 'active') return 'active-bg';
    if (status === 'sent') return 'sent-bg';
    if (status === 'draft') return 'draft-bg';
    if (status === 'failed') return 'failed-bg';
    if (status === 'scheduled') return 'scheduled-bg';
    return 'draft-bg';
  }

  formatAlertDate(date: Date): string {
    return new Date(date).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  }

  capitalizeFirst(text: string): string {
    if (!text) return '';
    return text.charAt(0).toUpperCase() + text.slice(1);
  }

  viewAlertDetails(alert: AlertMessage): void {
    const details = `
📢 ALERT DETAILS

🔸 ID: ${alert.id}
🔸 Title: ${alert.title}
🔸 Type: ${this.capitalizeFirst(alert.type)}
🔸 Priority: ${this.capitalizeFirst(alert.priority)}
🔸 Status: ${this.capitalizeFirst(alert.status)}
🔸 Created: ${this.formatAlertDate(alert.date)}

📋 Description:
${alert.description}

👥 Audience:
• ${alert.recipients} recipients
• Regions: ${alert.regions.join(', ')}

📡 Channels: ${alert.channels.map(c => c.toUpperCase()).join(', ')}
✅ Delivery Rate: ${alert.deliveryRate}%
👤 Created By: ${alert.createdBy}
    `;
    
    window.alert(details);
  }

  // ==================== LANGUAGE METHODS ====================
  selectLanguage(languageName: string): void {
    this.languages.forEach(lang => {
      const isSelected = lang.name === languageName;
      lang.selected = isSelected;
      
      if (isSelected) {
        this.modalData.selectedLanguage = lang.name;
        this.modalData.languageCode = lang.code;
      }
    });
    console.log(`Language selected: ${languageName} (${this.modalData.languageCode})`);
  }

  // ==================== AUDIO MODAL METHODS ====================
  openAudioModal(): void {
    this.showAudioModal = true;
    console.log('Audio modal opened');
    this.resetModalData();
  }

  closeAudioModal(): void {
    if (this.modalData.isRecording) {
      this.stopRecording();
    }
    
    this.showAudioModal = false;
    console.log('Audio modal closed');
  }

  resetModalData(): void {
    this.modalData = {
      messageTitle: '',
      selectedLanguage: 'French',
      languageCode: 'fr',
      targetAudience: 'All Members (300)',
      autoPlayEnabled: true,
      includeSmsTranscription: true,
      isRecording: false,
      recordingTime: 0,
      audioFile: null,
      recordingTimer: null
    };
    
    this.languages.forEach(lang => {
      lang.selected = lang.name === 'French';
    });
    
    this.audioChunks = [];
    this.audioBlob = null;
  }

  // ==================== AUDIO RECORDING/UPLOAD METHODS ====================
  async startRecording(): Promise<void> {
    console.log('Starting audio recording...');
    
    if (this.modalData.isRecording) {
      this.stopRecording();
      return;
    }
    
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      
      this.mediaRecorder = new MediaRecorder(stream);
      this.audioChunks = [];
      
      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          this.audioChunks.push(event.data);
        }
      };
      
      this.mediaRecorder.onstop = () => {
        this.audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
        console.log('Recording saved as blob:', this.audioBlob.size + ' bytes');
        stream.getTracks().forEach(track => track.stop());
      };
      
      this.mediaRecorder.start(100);
      this.modalData.isRecording = true;
      this.modalData.recordingTime = 0;
      
      this.modalData.recordingTimer = setInterval(() => {
        this.modalData.recordingTime++;
      }, 1000);
      
      console.log('Recording started');
      
    } catch (error) {
      console.error('Error accessing microphone:', error);
      window.alert('Could not access microphone. Please check permissions and try again.');
    }
  }

  stopRecording(): void {
    if (!this.modalData.isRecording || !this.mediaRecorder) return;
    
    this.mediaRecorder.stop();
    this.modalData.isRecording = false;
    clearInterval(this.modalData.recordingTimer);
    
    console.log(`Recording stopped. Duration: ${this.formatTime(this.modalData.recordingTime)}`);
  }

  uploadFile(): void {
    console.log('Opening file upload dialog...');
    
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'audio/*,.mp3,.wav,.m4a,.aac,.webm';
    
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        this.modalData.audioFile = file;
        console.log('Audio file selected:', file.name);
        
        if (file.size > 50 * 1024 * 1024) {
          window.alert('File too large. Maximum size is 50MB.');
          this.modalData.audioFile = null;
          return;
        }
        
        if (!file.type.startsWith('audio/')) {
          window.alert('Please select an audio file.');
          this.modalData.audioFile = null;
          return;
        }
        
        if (this.audioBlob) {
          this.audioBlob = null;
          this.audioChunks = [];
        }
        
        console.log(`File "${file.name}" selected for upload`);
      }
    };
    
    input.click();
  }

  private formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  getRecordingStatus(): string {
    if (this.modalData.isRecording) {
      return `Recording... ${this.formatTime(this.modalData.recordingTime)}`;
    }
    if (this.modalData.audioFile) {
      return `File ready: ${this.modalData.audioFile.name}`;
    }
    if (this.audioBlob) {
      return `Recording ready (${this.formatTime(this.modalData.recordingTime)})`;
    }
    return 'No audio selected';
  }

  // ==================== MESSAGE BROADCAST METHODS ====================
  broadcastMessage(): void {
    if (!this.modalData.messageTitle.trim()) {
      window.alert('Please enter a message title');
      return;
    }

    const hasAudio = this.modalData.audioFile || this.audioBlob || this.modalData.recordingTime > 0;
    
    if (!hasAudio) {
      const confirmNoAudio = window.confirm('No audio file or recording detected. Broadcast text-only message?');
      if (!confirmNoAudio) {
        return;
      }
    }

    const randomListeners = Math.floor(Math.random() * 250) + 50;
    
    let duration = '0:00';
    if (this.modalData.recordingTime > 0) {
      duration = this.formatTime(this.modalData.recordingTime);
    } else if (this.modalData.audioFile) {
      const randomMinutes = Math.floor(Math.random() * 3) + 1;
      const randomSeconds = Math.floor(Math.random() * 60);
      duration = `${randomMinutes}:${randomSeconds.toString().padStart(2, '0')}`;
    }

    const newMessage: AudioMessage = {
      id: Date.now(),
      title: this.modalData.messageTitle,
      language: this.modalData.selectedLanguage,
      duration: duration,
      listeners: randomListeners,
      icon: 'fas fa-microphone',
      playUrl: '#',
      date: new Date(),
      audioBlob: this.audioBlob,
      audioFile: this.modalData.audioFile,
      isPlaying: false
    };

    console.log('Broadcasting message:', newMessage);
    
    this.recentMessages.unshift(newMessage);
    
    if (this.recentMessages.length > 5) {
      this.recentMessages = this.recentMessages.slice(0, 5);
    }

    this.dashboardMetrics.audioMessages.value += 1;
    this.dashboardMetrics.totalMessages.value += 1;
    
    this.dashboardMetrics.totalMessages.change = 
      `${parseInt(this.dashboardMetrics.totalMessages.change.split(' ')[0]) + 1} this week`;

    const successMessage = `Message "${this.modalData.messageTitle}" broadcast successfully to ${this.modalData.targetAudience}!`;
    
    this.closeAudioModal();
    
    window.alert(successMessage);
    
    console.log('New message added to recent messages with audio:', newMessage);
  }

  // ==================== AUDIO PLAYBACK METHODS ====================
  playMessage(message: AudioMessage): void {
    console.log('Attempting to play message:', message.title);
    
    this.stopCurrentPlayback();
    
    if (message.audioBlob || message.audioFile) {
      let audioUrl: string;
      
      if (message.audioBlob) {
        audioUrl = URL.createObjectURL(message.audioBlob);
      } else if (message.audioFile) {
        audioUrl = URL.createObjectURL(message.audioFile);
      } else {
        this.showDemoPlayback(message);
        return;
      }
      
      this.currentPlayingAudio = new Audio(audioUrl);
      this.currentPlayingMessageId = message.id;
      message.isPlaying = true;
      
      this.currentPlayingAudio.onplay = () => {
        console.log('Audio started playing');
        this.showAudioPlayer(message, audioUrl);
      };
      
      this.currentPlayingAudio.onended = () => {
        console.log('Audio finished playing');
        message.isPlaying = false;
        this.currentPlayingMessageId = null;
        URL.revokeObjectURL(audioUrl);
      };
      
      this.currentPlayingAudio.onerror = (error) => {
        console.error('Error playing audio:', error);
        message.isPlaying = false;
        this.currentPlayingMessageId = null;
        window.alert('Error playing audio. Please try again.');
      };
      
      this.currentPlayingAudio.play().catch(error => {
        console.error('Playback failed:', error);
        message.isPlaying = false;
        this.currentPlayingMessageId = null;
        window.alert('Could not play audio. Please check your speakers.');
      });
      
    } else {
      this.showDemoPlayback(message);
    }
  }

  stopCurrentPlayback(): void {
    if (this.currentPlayingAudio) {
      this.currentPlayingAudio.pause();
      this.currentPlayingAudio = null;
    }
    
    if (this.currentPlayingMessageId) {
      const playingMessage = this.recentMessages.find(m => m.id === this.currentPlayingMessageId);
      if (playingMessage) {
        playingMessage.isPlaying = false;
      }
      this.currentPlayingMessageId = null;
    }
  }

  private showDemoPlayback(message: AudioMessage): void {
    console.log('Showing demo playback for message without audio');
    message.isPlaying = true;
    
    const notification = document.createElement('div');
    notification.style.cssText = `
      position: fixed;
      bottom: 20px;
      right: 20px;
      background: #34c759;
      color: white;
      padding: 15px 20px;
      border-radius: 10px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.2);
      z-index: 10000;
      min-width: 300px;
      animation: slideIn 0.3s ease;
    `;
    
    notification.innerHTML = `
      <div style="display: flex; align-items: center; gap: 15px;">
        <i class="fas fa-play-circle" style="font-size: 24px;"></i>
        <div>
          <div style="font-weight: bold;">Demo Playback</div>
          <div style="font-size: 13px; opacity: 0.9;">"${message.title}"</div>
          <div style="margin-top: 5px; width: 100%; height: 4px; background: rgba(255,255,255,0.3); border-radius: 2px; overflow: hidden;">
            <div class="demo-progress" style="height: 100%; background: white; width: 0%; transition: width 0.3s;"></div>
          </div>
        </div>
        <button class="demo-stop" style="background: none; border: none; color: white; cursor: pointer; margin-left: auto;">
          <i class="fas fa-times"></i>
        </button>
      </div>
    `;
    
    document.body.appendChild(notification);
    
    let progress = 0;
    const progressBar = notification.querySelector('.demo-progress') as HTMLElement;
    const interval = setInterval(() => {
      progress += 2;
      progressBar.style.width = `${progress}%`;
      
      if (progress >= 100) {
        clearInterval(interval);
        setTimeout(() => {
          notification.remove();
          message.isPlaying = false;
        }, 500);
      }
    }, 100);
    
    const stopBtn = notification.querySelector('.demo-stop')!;
    stopBtn.addEventListener('click', () => {
      clearInterval(interval);
      notification.remove();
      message.isPlaying = false;
    });
  }

  private showAudioPlayer(message: AudioMessage, audioUrl: string): void {
    const player = document.createElement('div');
    player.style.cssText = `
      position: fixed;
      bottom: 20px;
      right: 20px;
      background: #34c759;
      color: white;
      padding: 15px 20px;
      border-radius: 10px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.2);
      z-index: 10000;
      min-width: 350px;
      animation: slideIn 0.3s ease;
    `;
    
    player.innerHTML = `
      <div style="display: flex; align-items: center; gap: 15px;">
        <button class="player-pause" style="background: none; border: none; color: white; cursor: pointer; font-size: 20px;">
          <i class="fas fa-pause"></i>
        </button>
        <div style="flex: 1;">
          <div style="font-weight: bold; font-size: 14px;">Now Playing</div>
          <div style="font-size: 13px; opacity: 0.9; margin-top: 2px;">"${message.title}"</div>
          <div style="margin-top: 8px; display: flex; align-items: center; gap: 10px;">
            <div class="player-time" style="font-size: 12px;">0:00</div>
            <div style="flex: 1; height: 4px; background: rgba(255,255,255,0.3); border-radius: 2px; overflow: hidden;">
              <div class="player-progress" style="height: 100%; background: white; width: 0%;"></div>
            </div>
            <div class="player-duration" style="font-size: 12px;">${message.duration}</div>
          </div>
        </div>
        <button class="player-stop" style="background: none; border: none; color: white; cursor: pointer;">
          <i class="fas fa-times"></i>
        </button>
      </div>
    `;
    
    document.body.appendChild(player);
    
    const pauseBtn = player.querySelector('.player-pause')!;
    const stopBtn = player.querySelector('.player-stop')!;
    const progressBar = player.querySelector('.player-progress') as HTMLElement;
    const timeDisplay = player.querySelector('.player-time') as HTMLElement;
    const durationDisplay = player.querySelector('.player-duration') as HTMLElement;
    
    const updateProgress = () => {
      if (this.currentPlayingAudio && !this.currentPlayingAudio.paused) {
        const progress = (this.currentPlayingAudio.currentTime / this.currentPlayingAudio.duration) * 100;
        progressBar.style.width = `${progress}%`;
        timeDisplay.textContent = this.formatAudioTime(this.currentPlayingAudio.currentTime);
        
        if (this.currentPlayingAudio.duration && this.currentPlayingAudio.duration > 0) {
          durationDisplay.textContent = this.formatAudioTime(this.currentPlayingAudio.duration);
        }
      }
    };
    
    const progressInterval = setInterval(updateProgress, 100);
    
    pauseBtn.addEventListener('click', () => {
      if (this.currentPlayingAudio) {
        if (this.currentPlayingAudio.paused) {
          this.currentPlayingAudio.play();
          pauseBtn.innerHTML = '<i class="fas fa-pause"></i>';
        } else {
          this.currentPlayingAudio.pause();
          pauseBtn.innerHTML = '<i class="fas fa-play"></i>';
        }
      }
    });
    
    stopBtn.addEventListener('click', () => {
      this.stopCurrentPlayback();
      player.remove();
      clearInterval(progressInterval);
      URL.revokeObjectURL(audioUrl);
    });
    
    if (this.currentPlayingAudio) {
      this.currentPlayingAudio.onended = () => {
        player.remove();
        clearInterval(progressInterval);
        URL.revokeObjectURL(audioUrl);
        message.isPlaying = false;
        this.currentPlayingMessageId = null;
      };
    }
  }

  private formatAudioTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  // ==================== MESSAGE INTERACTION METHODS ====================
  viewMessageStats(message: AudioMessage): void {
    console.log('Viewing stats for message:', message.title);
    
    const completionRate = Math.floor(Math.random() * 30) + 70;
    const peakTime = ['Morning (8-10 AM)', 'Afternoon (2-4 PM)', 'Evening (6-8 PM)'][Math.floor(Math.random() * 3)];
    const regions = [
      { name: 'North Region', percentage: Math.floor(Math.random() * 40) + 30 },
      { name: 'South Region', percentage: Math.floor(Math.random() * 30) + 20 },
      { name: 'East Region', percentage: Math.floor(Math.random() * 20) + 10 },
      { name: 'West Region', percentage: Math.floor(Math.random() * 10) + 5 }
    ];
    
    const statsMessage = `
🎵 Audio Message Statistics

📋 Title: "${message.title}"
🌐 Language: ${message.language}
⏱️ Duration: ${message.duration} minutes
👥 Total Listeners: ${message.listeners}

📊 Performance Metrics:
   ✅ Completion Rate: ${completionRate}%
   ⏰ Peak Listening Time: ${peakTime}
   📍 Audience Distribution:
${regions.map(r => `     • ${r.name}: ${r.percentage}%`).join('\n')}
    `;
    
    window.alert(statsMessage);
  }

  // ==================== HEADER ACTION METHODS ====================
  filterHistory(): void {
    console.log('Filter history clicked');
    window.alert('Filter history functionality - would open filter options');
  }

  exportData(): void {
    console.log('Export clicked');
    this.createCSVExport();
  }

  private createCSVExport(): void {
    const csvData = [
      ['Message Title', 'Language', 'Duration', 'Listeners', 'Date'],
      ...this.recentMessages.map(msg => [
        msg.title,
        msg.language,
        msg.duration,
        msg.listeners.toString(),
        msg.date.toLocaleDateString()
      ])
    ];
    
    const csvContent = csvData.map(row => row.join(',')).join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    link.setAttribute('href', url);
    link.setAttribute('download', `communications-export-${new Date().toISOString().slice(0,10)}.csv`);
    link.style.visibility = 'hidden';
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    window.alert('Export completed! File downloaded as CSV.');
  }

  // ==================== ACTION BUTTON METHODS ====================
  onActionButtonClick(buttonName: string): void {
    this.actionButtons.forEach(btn => {
      btn.active = btn.name === buttonName;
    });

    console.log(`Action button clicked: ${buttonName}`);
    
    switch(buttonName) {
      case 'Compose':
        this.onComposeClick();
        break;
      case 'Audio':
        this.onAudioClick();
        break;
      case 'Alerts':
        this.onAlertsClick();
        break;
      case 'Resources':
        this.onResourcesClick();
        break;
      case 'Templates':
        this.onTemplatesClick();
        break;
    }
  }

  private onComposeClick(): void {
    console.log('Compose functionality - opening message composer');
  }

  private onAudioClick(): void {
    console.log('Audio functionality - managing audio messages');
  }

  private onAlertsClick(): void {
    console.log('Alerts functionality - viewing alerts');
    this.calculateAlertStats();
  }

  private onResourcesClick(): void {
    console.log('Resources functionality - managing resources');
  }

  private onTemplatesClick(): void {
    console.log('Templates functionality - managing templates');
  }

  // ==================== UTILITY METHODS ====================
  formatMessageDate(date: Date): string {
    const now = new Date();
    const messageDate = new Date(date);
    const diffInHours = (now.getTime() - messageDate.getTime()) / (1000 * 60 * 60);
    
    if (diffInHours < 24) {
      if (diffInHours < 1) {
        return 'Just now';
      } else if (diffInHours < 2) {
        return '1 hour ago';
      } else {
        return `${Math.floor(diffInHours)} hours ago`;
      }
    } else if (diffInHours < 48) {
      return 'Yesterday';
    } else {
      return messageDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }
  }

  formatNumber(num: number): string {
    return num.toLocaleString();
  }

  getCurrentDate(): string {
    return new Date().toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  // ==================== GETTERS FOR SECTION VISIBILITY ====================
  get isAudioSectionActive(): boolean {
    const audioBtn = this.actionButtons.find(btn => btn.name === 'Audio');
    return audioBtn ? audioBtn.active : false;
  }

  get isAlertsSectionActive(): boolean {
    const alertsBtn = this.actionButtons.find(btn => btn.name === 'Alerts');
    return alertsBtn ? alertsBtn.active : false;
  }

  private _isComposeSectionActive: boolean = false;

get isComposeSectionActive(): boolean {
  const composeBtn = this.actionButtons.find(btn => btn.name === 'Compose');
  return composeBtn ? composeBtn.active : this._isComposeSectionActive;
}

set isComposeSectionActive(value: boolean) {
  this._isComposeSectionActive = value;
  // Also update the action button if it exists
  const composeBtn = this.actionButtons.find(btn => btn.name === 'Compose');
  if (composeBtn) {
    composeBtn.active = value;
  }
}

  // ==================== ALERT FILTER METHODS ====================
  updateFilteredStats(): void {
    const filtered = this.filteredAlerts;
    
    this.alertStats.total = filtered.length;
    this.alertStats.active = filtered.filter(a => a.status === 'active').length;
    this.alertStats.sent = filtered.filter(a => a.status === 'sent').length;
    this.alertStats.critical = filtered.filter(a => a.priority === 'critical').length;
    
    this.alertTypes.forEach(type => {
      this.alertStats.byType[type as keyof typeof this.alertStats.byType] = 
        filtered.filter(a => a.type === type).length;
    });
  }

  setStatusFilter(status: 'all' | 'active' | 'sent'): void {
    this.alertStatusFilter = status;
    this.updateFilteredStats();
  }

  showSearchHelp(): void {
    const helpMessage = `
🔍 SEARCH HELP

You can search alerts by:

📋 **ID**: ART-001, ART-002, etc.
📝 **Title**: Heavy Rainfall, Loan Payment, etc.
📄 **Description**: weather, payment, price, etc.
📍 **Region**: North, South, East, West
📡 **Channels**: sms, push, email, voice
⚠️ **Priority**: critical, warning, information, low
📊 **Type**: weather, payment, price, security, etc.

Examples:
• "weather" - Find all weather alerts
• "critical" - Find critical priority alerts
• "sms" - Find alerts sent via SMS
• "north" - Find alerts for north region
• "ART-001" - Find specific alert by ID
  `;
  
    window.alert(helpMessage);
  }

  // ==================== TAB MANAGEMENT ====================
  activeTab: string = 'resources';

  switchTab(tab: string) {
    this.activeTab = tab;
  }

  // ==================== DIAGNOSTIC METHOD ====================
  diagnoseButtonIssue() {
    console.log('=== BUTTON DIAGNOSTIC ===');
    console.log('1. isSending value:', this.isSending);
    console.log('2. Type of isSending:', typeof this.isSending);
    console.log('3. Form valid:', this.isFormValid());
    console.log('4. Button should be disabled:', !this.isFormValid() || this.isSending);
    console.log('5. Has btn-loading class:', this.isSending);
    
    setTimeout(() => {
      console.log('6. After timeout - isSending:', this.isSending);
    }, 100);
  }
}