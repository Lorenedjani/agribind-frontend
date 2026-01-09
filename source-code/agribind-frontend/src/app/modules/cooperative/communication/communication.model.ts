// Enum for alert status
export enum AlertStatus {
  CRITICAL = 'critical',
  WARNING = 'warning',
  NORMAL = 'normal'
}

// Enum for message status
export enum MessageStatus {
  SENT = 'sent',
  SCHEDULED = 'scheduled',
  DRAFT = 'draft',
  FAILED = 'failed'
}

// Enum for language codes
export enum Language {
  FRENCH = 'French',
  ENGLISH = 'English',
  FULFULDE = 'Fulfulde',
  EVONDO = 'Evondo',
  DUALA = 'Duala'
}

// Interface for dashboard statistics
export interface DashboardStats {
  totalMessages: {
    current: number;
    previous: number;
    trend: number;
  };
  activeAlerts: {
    count: number;
    status: AlertStatus;
  };
  audioMessages: {
    count: number;
    languages: number;
    deliveryRate: number;
    deliveryTrend: number;
  };
}

// Interface for audio message
export interface AudioMessage {
  id: number;
  title: string;
  language: Language;
  duration: string; // Format: "2:45 min"
  listeners: number;
  date: string; // ISO format or "YYYY-MM-DD"
  status: MessageStatus;
  fileUrl?: string;
  targetAudience: string;
  autoPlay: boolean;
  createdAt: Date;
  scheduledFor?: Date;
}

// Interface for message statistics
export interface MessageStats {
  messageId: number;
  totalListeners: number;
  completionRate: number; // Percentage
  peakListenTime: string; // Time of day
  regions: RegionStat[];
  devices: DeviceStat[];
  replayCount: number;
}

// Interface for region statistics
export interface RegionStat {
  name: string;
  listeners: number;
  percentage: number;
}

// Interface for device statistics
export interface DeviceStat {
  type: 'mobile' | 'desktop' | 'tablet';
  count: number;
  percentage: number;
}

// Interface for alert
export interface Alert {
  id: number;
  title: string;
  priority: 'critical' | 'warning' | 'information' | 'low';
  message: string;
  severity: AlertStatus;
  timestamp: Date;
  acknowledged: boolean;
  targetGroups: string[];
  expiresAt?: Date;
}

// Interface for broadcast form data
export interface BroadcastFormData {
  title: string;
  language: Language;
  targetAudience: string;
  autoPlay: boolean;
  audioFile?: File;
  duration?: string;
  scheduleFor?: Date;
}

// Interface for user/audience
export interface AudienceMember {
  id: number;
  name: string;
  phoneNumber: string;
  region: string;
  language: Language;
  groups: string[];
  lastActive: Date;
}

// Interface for template
export interface MessageTemplate {
  id: number;
  name: string;
  title: string;
  body: string;
  language: Language;
  category: string;
  createdBy: string;
  createdAt: Date;
  lastUsed: Date;
  usageCount: number;
}

// Interface for resource request
export interface ResourceRequest {
  id: number;
  type: string;
  quantity: number;
  requester: string;
  status: 'pending' | 'approved' | 'rejected' | 'fulfilled';
  requestedAt: Date;
  completedAt?: Date;
  notes?: string;
}

// Interface for API response wrapper
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  timestamp: Date;
}

// Interface for paginated response
export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

// Interface for filter options
export interface MessageFilter {
  language?: Language;
  status?: MessageStatus;
  startDate?: Date;
  endDate?: Date;
  searchTerm?: string;
  page?: number;
  limit?: number;
}

// Interface for dashboard summary
export interface DashboardSummary {
  totalMessages: number;
  totalAlerts: number;
  totalAudioMessages: number;
  totalListeners: number;
  averageDeliveryRate: number;
  recentActivity: RecentActivity[];
}

// Interface for recent activity
export interface RecentActivity {
  id: number;
  type: 'message' | 'alert' | 'resource';
  action: string;
  user: string;
  timestamp: Date;
  details?: any;
}

// Class implementations for creating instances
export class AudioMessageModel implements AudioMessage {
  constructor(
    public id: number,
    public title: string,
    public language: Language,
    public duration: string,
    public listeners: number,
    public date: string,
    public status: MessageStatus,
    public targetAudience: string,
    public autoPlay: boolean,
    public createdAt: Date = new Date(),
    public fileUrl?: string,
    public scheduledFor?: Date
  ) {}
}

export class BroadcastFormModel implements BroadcastFormData {
  constructor(
    public title: string,
    public language: Language = Language.ENGLISH,
    public targetAudience: string = 'All Members',
    public autoPlay: boolean = true,
    public audioFile?: File,
    public duration?: string,
    public scheduleFor?: Date
  ) {}
}

export class AlertModel implements Alert {
  constructor(
    public id: number,
    public title: string,
    public message: string,
    public priority: 'critical' | 'warning' | 'information' | 'low',
    public severity: AlertStatus = AlertStatus.NORMAL,
    public timestamp: Date = new Date(),
    public acknowledged: boolean = false,
    public targetGroups: string[] = ['All Members'],
    public expiresAt?: Date
  ) {}
}

// Default/initial values
export const DEFAULT_DASHBOARD_STATS: DashboardStats = {
  totalMessages: {
    current: 247,
    previous: 0,
    trend: 45
  },
  activeAlerts: {
    count: 23,
    status: AlertStatus.CRITICAL
  },
  audioMessages: {
    count: 87,
    languages: 5,
    deliveryRate: 94.5,
    deliveryTrend: 1.2
  }
};

export const DEFAULT_RECENT_MESSAGES: AudioMessage[] = [
  {
    id: 1,
    title: 'Fertilizer Delivery Announcement',
    language: Language.FRENCH,
    duration: '2:45 min',
    listeners: 245,
    date: new Date().toISOString().split('T')[0],
    status: MessageStatus.SENT,
    targetAudience: 'All Members',
    autoPlay: true,
    createdAt: new Date()
  }
];

// Utility functions
export function formatDuration(minutes: number, seconds: number = 0): string {
  const totalSeconds = minutes * 60 + seconds;
  const mins = Math.floor(totalSeconds / 60);
  const secs = totalSeconds % 60;
  return `${mins}:${secs.toString().padStart(2, '0')} min`;
}

export function formatNumber(num: number): string {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M';
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K';
  }
  return num.toString();
}

export function getLanguageColor(language: Language): string {
  const colors: Record<Language, string> = {
    [Language.FRENCH]: '#4285f4',
    [Language.ENGLISH]: '#34a853',
    [Language.FULFULDE]: '#fbbc05',
    [Language.EVONDO]: '#ea4335',
    [Language.DUALA]: '#9c27b0'
  };
  return colors[language] || '#666';
}