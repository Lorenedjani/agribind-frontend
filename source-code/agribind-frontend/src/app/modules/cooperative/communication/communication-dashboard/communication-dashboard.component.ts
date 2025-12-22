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
  today: string = new Date().toISOString().split('T')[0];
  
  // Dashboard metrics data
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

  // Action buttons
  actionButtons = [
    { 
      name: 'Compose', 
      icon: 'fas fa-paper-plane', 
      active: false 
    },
    { 
      name: 'Audio', 
      icon: 'fas fa-microphone', 
      active: true 
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

  // Language options for modal
  languages = [
    { name: 'French', code: 'fr', selected: true, icon: 'fas fa-microphone' },
    { name: 'English', code: 'en', selected: false, icon: 'fas fa-microphone' },
    { name: 'Fulfulde', code: 'ff', selected: false, icon: 'fas fa-microphone' },
    { name: 'Ewondo', code: 'ewo', selected: false, icon: 'fas fa-microphone' },
    { name: 'Duala', code: 'dua', selected: false, icon: 'fas fa-microphone' }
  ];

  // Recent audio messages
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

  constructor() { }

  ngOnInit(): void {
    // Initialize with French selected by default
    this.selectLanguage('French');
    // Calculate alert stats
    this.calculateAlertStats();
  }

  // ==================== ALERTS SECTION METHODS ====================

  // Get audience count for modal display
  getAudienceCount(): number {
    if (!this.newAlert || !this.newAlert.audience) return 0;
    
    switch (this.newAlert.audience) {
      case 'all': return 300;
      case 'region1': return 150;
      case 'region2': return 120;
      case 'region3': return 80;
      case 'region4': return 100;
      case 'custom': return 0; // Custom selection
      default: return 0;
    }
  }

  // Toggle channel selection
  toggleChannel(channel: any): void {
    channel.selected = !channel.selected;
  }

  // Get selected channels
  getSelectedChannels(): string[] {
    return this.communicationChannels
      .filter(channel => channel.selected)
      .map(channel => channel.name.toLowerCase());
  }

  // Send alert from modal
 // Add a helper method to generate sequential alert IDs
// Add this helper method to generate sequential alert IDs
private generateAlertId(): string {
  let maxId = 0;
  
  // Go through all alerts and find the highest ID number
  this.alerts.forEach(alert => {
    const idStr = alert.id;
    
    // Check for ALT-001 format
    if (idStr.startsWith('ALT-')) {
      const numStr = idStr.substring(4); // Remove "ALT-"
      const num = parseInt(numStr, 10);
      if (!isNaN(num) && num > maxId) {
        maxId = num;
      }
    }
  });
  
  const nextId = maxId + 1;
  return `ALT-${nextId.toString().padStart(3, '0')}`;
}

// Update the sendAlertFromModal method to use this generator
sendAlertFromModal(): void {
  if (!this.newAlert.title.trim()) {
    window.alert('Please enter an alert title');
    return;
  }
  
  if (!this.newAlert.message.trim()) {
    window.alert('Please enter an alert message');
    return;
  }
  
  // Get selected channels
  const selectedChannels = this.getSelectedChannels();
  if (selectedChannels.length === 0) {
    window.alert('Please select at least one communication channel');
    return;
  }
  
  // Generate sequential ID
  const alertId = this.generateAlertId();
  console.log('Generated Alert ID:', alertId);
  console.log('Existing alerts:', this.alerts.map(a => a.id));
  
  // Create new alert
  const newAlert: AlertMessage = {
    id: alertId, // Use the generated ID
    type: this.newAlert.type as any,
    priority: this.newAlert.priority as any,
    title: this.newAlert.title,
    description: this.newAlert.message,
    recipients: this.getAudienceCount(),
    regions: [this.getRegionName(this.newAlert.audience)],
    channels: selectedChannels,
    deliveryRate: Math.floor(Math.random() * 20) + 80, // Random 80-100%
    date: new Date(),
    status: 'active', // Changed from 'draft' to 'active'
    createdBy: 'current_user'
  };
  
  this.alerts.push(newAlert);
  console.log('Alerts after adding:', this.alerts.map(a => a.id));
  this.calculateAlertStats();
  this.closeAlertModal();
  
  // Reset newAlert object
  this.newAlert = {
    type: 'weather',
    priority: 'warning',
    title: '',
    message: '',
    audience: 'all',
    requiresAck: false,
    expiryDate: ''
  };
  
  // Reset channels
  this.communicationChannels.forEach(channel => {
    channel.selected = channel.name === 'SMS' || channel.name === 'Push';
  });
  
  window.alert(`Alert ${alertId} created successfully!`);
}

  // Helper method to get region name
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

  // Calculate alert statistics
  calculateAlertStats(): void {
    this.alertStats.total = this.alerts.length;
    this.alertStats.active = this.alerts.filter(a => a.status === 'active').length;
    this.alertStats.sent = this.alerts.filter(a => a.status === 'sent').length;
    this.alertStats.critical = this.alerts.filter(a => a.priority === 'critical').length;
    
    // Count by type
    this.alertTypes.forEach(type => {
      this.alertStats.byType[type as keyof typeof this.alertStats.byType] = 
        this.alerts.filter(a => a.type === type).length;
    });
  }

  onSearch(): void {
    // This method is triggered when the search input changes
    // The filtering happens automatically through the filteredAlerts getter
    console.log('Searching alerts for:', this.alertSearchQuery);
  }

  // Get filtered alerts
 // Get filtered alerts
// Get filtered alerts with enhanced search
get filteredAlerts(): AlertMessage[] {
  let filtered = this.alerts;
  
  // Apply status filter
  if (this.alertStatusFilter !== 'all') {
    filtered = filtered.filter(alert => alert.status === this.alertStatusFilter);
  }
  
  // Apply search filter with enhanced capabilities
  if (this.alertSearchQuery) {
    const query = this.alertSearchQuery.toLowerCase().trim();
    
    filtered = filtered.filter(alert => {
      // Search in multiple fields
      const searchFields = [
        alert.title.toLowerCase(),
        alert.description.toLowerCase(),
        alert.id.toLowerCase(),
        alert.type.toLowerCase(),
        alert.priority.toLowerCase(),
        ...alert.regions.map(region => region.toLowerCase()),
        ...alert.channels.map(channel => channel.toLowerCase())
      ];
      
      // Also search for partial matches in priority and type
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
      
      // Check if query matches any search field
      if (searchFields.some(field => field.includes(query))) {
        return true;
      }
      
      // Check if query matches priority keywords
      const priorityKeywords = priorityMap[alert.priority] || [];
      if (priorityKeywords.some(keyword => query.includes(keyword))) {
        return true;
      }
      
      // Check if query matches type keywords
      const typeKeywords = typeMap[alert.type] || [];
      if (typeKeywords.some(keyword => query.includes(keyword))) {
        return true;
      }
      
      return false;
    });
  }
  
  return filtered;
}

  // Open alert creation modal
  openAlertModal(): void {
    this.showAlertModal = true;
  }

  // Close alert modal
  closeAlertModal(): void {
    this.showAlertModal = false;
    // Reset newAlert object
    this.newAlert = {
      type: 'weather',
      priority: 'warning',
      title: '',
      message: '',
      audience: 'all',
      requiresAck: false,
      expiryDate: ''
    };
    
    // Reset channels
    this.communicationChannels.forEach(channel => {
      channel.selected = channel.name === 'SMS' || channel.name === 'Push';
    });
  }

  // Delete alert
  deleteAlert(alertId: string): void {
    if (window.confirm('Are you sure you want to delete this alert?')) {
      this.alerts = this.alerts.filter(alert => alert.id !== alertId);
      this.calculateAlertStats();
      window.alert('Alert deleted successfully!');
    }
  }

  // Update alert status
  updateAlertStatus(alertId: string, newStatus: AlertMessage['status']): void {
    const alert = this.alerts.find(a => a.id === alertId);
    if (alert) {
      alert.status = newStatus;
      alert.date = new Date(); // Update timestamp
      this.calculateAlertStats();
      window.alert(`Alert ${alertId} status updated to ${newStatus}`);
    }
  }

  // Alert type icon
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

  // Priority class helper - NO ERRORS
  getPriorityClass(priority: string): string {
    if (priority === 'critical') return 'critical-bg';
    if (priority === 'warning') return 'warning-bg';
    if (priority === 'information') return 'info-bg';
    if (priority === 'low') return 'low-bg';
    return 'low-bg';
  }

  // Status class helper
  getStatusClass(status: string): string {
    if (status === 'active') return 'active-bg';
    if (status === 'sent') return 'sent-bg';
    if (status === 'draft') return 'draft-bg';
    if (status === 'failed') return 'failed-bg';
    if (status === 'scheduled') return 'scheduled-bg';
    return 'draft-bg';
  }

  // Format alert date
  formatAlertDate(date: Date): string {
    return new Date(date).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  }

  // Capitalize first letter
  capitalizeFirst(text: string): string {
    if (!text) return '';
    return text.charAt(0).toUpperCase() + text.slice(1);
  }

  // View alert details
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

  // Language selection in modal
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

  // Open audio recording modal
  openAudioModal(): void {
    this.showAudioModal = true;
    console.log('Audio modal opened');
    
    // Reset modal data when opening
    this.resetModalData();
  }

  // Close audio recording modal
  closeAudioModal(): void {
    // Stop recording if active
    if (this.modalData.isRecording) {
      this.stopRecording();
    }
    
    this.showAudioModal = false;
    console.log('Audio modal closed');
  }

  // Reset modal form data
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
    
    // Reset languages selection
    this.languages.forEach(lang => {
      lang.selected = lang.name === 'French';
    });
    
    // Clear audio recording
    this.audioChunks = [];
    this.audioBlob = null;
  }

  // ==================== AUDIO RECORDING/UPLOAD METHODS ====================

  // Start recording in modal
  async startRecording(): Promise<void> {
    console.log('Starting audio recording...');
    
    if (this.modalData.isRecording) {
      this.stopRecording();
      return;
    }
    
    try {
      // Request microphone permission
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      
      // Initialize MediaRecorder
      this.mediaRecorder = new MediaRecorder(stream);
      this.audioChunks = [];
      
      // Handle data available event
      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          this.audioChunks.push(event.data);
        }
      };
      
      // Handle recording stop
      this.mediaRecorder.onstop = () => {
        // Create blob from chunks
        this.audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
        console.log('Recording saved as blob:', this.audioBlob.size + ' bytes');
        
        // Stop all tracks in stream
        stream.getTracks().forEach(track => track.stop());
      };
      
      // Start recording
      this.mediaRecorder.start(100); // Collect data every 100ms
      this.modalData.isRecording = true;
      this.modalData.recordingTime = 0;
      
      // Start timer
      this.modalData.recordingTimer = setInterval(() => {
        this.modalData.recordingTime++;
      }, 1000);
      
      console.log('Recording started');
      
    } catch (error) {
      console.error('Error accessing microphone:', error);
      window.alert('Could not access microphone. Please check permissions and try again.');
    }
  }

  // Stop recording in modal
  stopRecording(): void {
    if (!this.modalData.isRecording || !this.mediaRecorder) return;
    
    // Stop recording
    this.mediaRecorder.stop();
    this.modalData.isRecording = false;
    clearInterval(this.modalData.recordingTimer);
    
    console.log(`Recording stopped. Duration: ${this.formatTime(this.modalData.recordingTime)}`);
  }

  // Upload file in modal
  uploadFile(): void {
    console.log('Opening file upload dialog...');
    
    // Create file input element
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'audio/*,.mp3,.wav,.m4a,.aac,.webm';
    
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        this.modalData.audioFile = file;
        console.log('Audio file selected:', file.name);
        
        // Validate file
        if (file.size > 50 * 1024 * 1024) { // 50MB limit
          window.alert('File too large. Maximum size is 50MB.');
          this.modalData.audioFile = null;
          return;
        }
        
        if (!file.type.startsWith('audio/')) {
          window.alert('Please select an audio file.');
          this.modalData.audioFile = null;
          return;
        }
        
        // If there was a recording, replace it with the uploaded file
        if (this.audioBlob) {
          this.audioBlob = null;
          this.audioChunks = [];
        }
        
        console.log(`File "${file.name}" selected for upload`);
      }
    };
    
    input.click();
  }

  // Format time in MM:SS
  private formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  // Get recording status text for modal
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

  // Broadcast message from modal
  broadcastMessage(): void {
    // Validate form
    if (!this.modalData.messageTitle.trim()) {
      window.alert('Please enter a message title');
      return;
    }

    // Check if we have audio
    const hasAudio = this.modalData.audioFile || this.audioBlob || this.modalData.recordingTime > 0;
    
    if (!hasAudio) {
      const confirmNoAudio = window.confirm('No audio file or recording detected. Broadcast text-only message?');
      if (!confirmNoAudio) {
        return;
      }
    }

    // Generate random listeners count (between 50-300 for new messages)
    const randomListeners = Math.floor(Math.random() * 250) + 50;
    
    // Format duration based on recording time or file
    let duration = '0:00';
    if (this.modalData.recordingTime > 0) {
      duration = this.formatTime(this.modalData.recordingTime);
    } else if (this.modalData.audioFile) {
      // For uploaded files, generate a random duration (1-4 minutes)
      const randomMinutes = Math.floor(Math.random() * 3) + 1;
      const randomSeconds = Math.floor(Math.random() * 60);
      duration = `${randomMinutes}:${randomSeconds.toString().padStart(2, '0')}`;
    }

    // Create new message WITH AUDIO DATA
    const newMessage: AudioMessage = {
      id: Date.now(),
      title: this.modalData.messageTitle,
      language: this.modalData.selectedLanguage,
      duration: duration,
      listeners: randomListeners,
      icon: 'fas fa-microphone',
      playUrl: '#',
      date: new Date(),
      // Store the audio data for playback
      audioBlob: this.audioBlob,
      audioFile: this.modalData.audioFile,
      isPlaying: false
    };

    console.log('Broadcasting message:', newMessage);
    
    // Add to beginning of recent messages
    this.recentMessages.unshift(newMessage);
    
    // Keep only last 5 messages
    if (this.recentMessages.length > 5) {
      this.recentMessages = this.recentMessages.slice(0, 5);
    }

    // Update dashboard metrics
    this.dashboardMetrics.audioMessages.value += 1;
    this.dashboardMetrics.totalMessages.value += 1;
    
    // Update weekly trend
    this.dashboardMetrics.totalMessages.change = 
      `${parseInt(this.dashboardMetrics.totalMessages.change.split(' ')[0]) + 1} this week`;

    // Show success message
    const successMessage = `Message "${this.modalData.messageTitle}" broadcast successfully to ${this.modalData.targetAudience}!`;
    
    // Close modal and reset
    this.closeAudioModal();
    
    // Show success message
    window.alert(successMessage);
    
    console.log('New message added to recent messages with audio:', newMessage);
  }

  // ==================== AUDIO PLAYBACK METHODS ====================

  // Play message with actual audio
  playMessage(message: AudioMessage): void {
    console.log('Attempting to play message:', message.title);
    
    // Stop any currently playing audio
    this.stopCurrentPlayback();
    
    // Check if message has audio data
    if (message.audioBlob || message.audioFile) {
      // Create audio URL from blob or file
      let audioUrl: string;
      
      if (message.audioBlob) {
        audioUrl = URL.createObjectURL(message.audioBlob);
      } else if (message.audioFile) {
        audioUrl = URL.createObjectURL(message.audioFile);
      } else {
        this.showDemoPlayback(message);
        return;
      }
      
      // Create audio element
      this.currentPlayingAudio = new Audio(audioUrl);
      this.currentPlayingMessageId = message.id;
      message.isPlaying = true;
      
      // Set up audio events
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
      
      // Start playback
      this.currentPlayingAudio.play().catch(error => {
        console.error('Playback failed:', error);
        message.isPlaying = false;
        this.currentPlayingMessageId = null;
        window.alert('Could not play audio. Please check your speakers.');
      });
      
    } else {
      // For demo messages without audio data
      this.showDemoPlayback(message);
    }
  }

  // Stop current audio playback
  stopCurrentPlayback(): void {
    if (this.currentPlayingAudio) {
      this.currentPlayingAudio.pause();
      this.currentPlayingAudio = null;
    }
    
    // Update UI for any playing message
    if (this.currentPlayingMessageId) {
      const playingMessage = this.recentMessages.find(m => m.id === this.currentPlayingMessageId);
      if (playingMessage) {
        playingMessage.isPlaying = false;
      }
      this.currentPlayingMessageId = null;
    }
  }

  // Show demo playback for messages without audio
  private showDemoPlayback(message: AudioMessage): void {
    console.log('Showing demo playback for message without audio');
    message.isPlaying = true;
    
    // Show play notification
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
    
    // Simulate playback progress
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
    
    // Stop button
    const stopBtn = notification.querySelector('.demo-stop')!;
    stopBtn.addEventListener('click', () => {
      clearInterval(interval);
      notification.remove();
      message.isPlaying = false;
    });
  }

  // Show audio player notification
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
    
    // Get elements
    const pauseBtn = player.querySelector('.player-pause')!;
    const stopBtn = player.querySelector('.player-stop')!;
    const progressBar = player.querySelector('.player-progress') as HTMLElement;
    const timeDisplay = player.querySelector('.player-time') as HTMLElement;
    const durationDisplay = player.querySelector('.player-duration') as HTMLElement;
    
    // Update progress
    const updateProgress = () => {
      if (this.currentPlayingAudio && !this.currentPlayingAudio.paused) {
        const progress = (this.currentPlayingAudio.currentTime / this.currentPlayingAudio.duration) * 100;
        progressBar.style.width = `${progress}%`;
        timeDisplay.textContent = this.formatAudioTime(this.currentPlayingAudio.currentTime);
        
        // Update duration if available
        if (this.currentPlayingAudio.duration && this.currentPlayingAudio.duration > 0) {
          durationDisplay.textContent = this.formatAudioTime(this.currentPlayingAudio.duration);
        }
      }
    };
    
    // Update progress every 100ms
    const progressInterval = setInterval(updateProgress, 100);
    
    // Pause/Play button
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
    
    // Stop button
    stopBtn.addEventListener('click', () => {
      this.stopCurrentPlayback();
      player.remove();
      clearInterval(progressInterval);
      URL.revokeObjectURL(audioUrl);
    });
    
    // Clean up when audio ends
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

  // Format audio time
  private formatAudioTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  // ==================== MESSAGE INTERACTION METHODS ====================

  // View stats for message
  viewMessageStats(message: AudioMessage): void {
    console.log('Viewing stats for message:', message.title);
    
    // Generate realistic stats
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

  // Filter history
  filterHistory(): void {
    console.log('Filter history clicked');
    window.alert('Filter history functionality - would open filter options');
  }

  // Export data
  exportData(): void {
    console.log('Export clicked');
    this.createCSVExport();
  }

  private createCSVExport(): void {
    // Prepare data for CSV
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
    
    // Create blob and download
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

  // Action button click handler
  onActionButtonClick(buttonName: string): void {
    // Update active state
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
    // Calculate fresh stats when switching to alerts
    this.calculateAlertStats();
  }

  private onResourcesClick(): void {
    console.log('Resources functionality - managing resources');
  }

  private onTemplatesClick(): void {
    console.log('Templates functionality - managing templates');
  }

  // ==================== UTILITY METHODS ====================

  // Format date for display
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

  // Format number with commas
  formatNumber(num: number): string {
    return num.toLocaleString();
  }

  // Get current date formatted
  getCurrentDate(): string {
    return new Date().toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  // Add these getters
  get isAudioSectionActive(): boolean {
    const audioBtn = this.actionButtons.find(btn => btn.name === 'Audio');
    return audioBtn ? audioBtn.active : false;
  }

  get isAlertsSectionActive(): boolean {
    const alertsBtn = this.actionButtons.find(btn => btn.name === 'Alerts');
    return alertsBtn ? alertsBtn.active : false;
  }
  // Add this method to your CommunicationDashboardComponent class:

updateFilteredStats(): void {
  const filtered = this.filteredAlerts;
  
  this.alertStats.total = filtered.length;
  this.alertStats.active = filtered.filter(a => a.status === 'active').length;
  this.alertStats.sent = filtered.filter(a => a.status === 'sent').length;
  this.alertStats.critical = filtered.filter(a => a.priority === 'critical').length;
  
  // Count by type for filtered alerts
  this.alertTypes.forEach(type => {
    this.alertStats.byType[type as keyof typeof this.alertStats.byType] = 
      filtered.filter(a => a.type === type).length;
  });
}
setStatusFilter(status: 'all' | 'active' | 'sent'): void {
  this.alertStatusFilter = status;
  this.updateFilteredStats();
}
// Method to show search help
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
}