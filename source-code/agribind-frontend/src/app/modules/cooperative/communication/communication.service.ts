import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import {
  AudioMessage,
  MessageTemplate,
  Alert,
  DashboardStats,
  DashboardSummary,
  MessageFilter,
  Language,
  ResourceRequest,
  AlertStatus,
  MessageStatus,
  ApiResponse,
  PaginatedResponse,
  DEFAULT_DASHBOARD_STATS,
  DEFAULT_RECENT_MESSAGES
} from './communication.model';

@Injectable({
  providedIn: 'root'
})
export class CommunicationService {
  private apiUrl = environment.services.communication;

  constructor(private http: HttpClient) {}

  private getHeaders(contentType: string = 'application/json'): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    let headers = new HttpHeaders();

    if (contentType) {
      headers = headers.set('Content-Type', contentType);
    }

    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    return headers;
  }

  // ==================== SMS Methods ====================

  sendBulkSms(request: {
    targetAudience: 'ALL_MEMBERS' | 'ACTIVE_MEMBERS' | 'DOUALA_ZONE' | 'YAOUNDE_ZONE' | 'OTHER_REGIONS' | 'CUSTOM';
    specificZone?: string;
    content: string;
    priority: 'NORMAL' | 'HIGH' | 'URGENT';
    scheduledAt?: Date;
    templateId?: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/sms/bulk`, request, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('sendBulkSms'))
    );
  }

  composeBulkMessage(request: {
    recipientAudience: string;
    specificZone?: string;
    messageContent: string;
    priority: string;
    scheduledDelivery?: Date;
    templateId?: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/sms/compose`, request, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('composeBulkMessage'))
    );
  }

  // ==================== Audio Message Methods ====================

  uploadAudioMessage(formData: FormData): Observable<any> {
    // Don't set Content-Type for FormData - browser will set it with boundary
    const token = localStorage.getItem('accessToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.post(`${this.apiUrl}/audio/upload`, formData, { headers }).pipe(
      catchError(this.handleError('uploadAudioMessage'))
    );
  }

  broadcastAudioMessage(request: any, audioFileId: number): Observable<any> {
    const params = new HttpParams().set('audioFileId', audioFileId.toString());
    return this.http.post(`${this.apiUrl}/audio/broadcast`, request, { 
      headers: this.getHeaders(),
      params 
    }).pipe(
      catchError(this.handleError('broadcastAudioMessage'))
    );
  }

  getSupportedLanguages(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/audio/languages`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getSupportedLanguages', []))
    );
  }

  // ==================== Alert Methods ====================

  createAlert(request: {
    title: string;
    description: string;
    type: string;
    priority: string;
    targetAudience: string;
    specificZone?: string;
    channels: string[];
    expiresAt?: Date;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/alerts`, request, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('createAlert'))
    );
  }

  getAlertHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/alerts/history`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getAlertHistory', []))
    );
  }

  getAlertById(alertId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/alerts/${alertId}`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getAlertById'))
    );
  }

  // ==================== Resource Request Methods ====================

  createResourceRequest(request: {
    resourceName: string;
    quantity: number;
    unit: string;
    urgency: 'LOW' | 'MEDIUM' | 'HIGH';
    requestedBy: string;
    requestedByZone?: string;
    requiredDate?: Date;
    notes?: string;
    budgetAmount?: number;
    currency?: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/resources/requests`, request, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('createResourceRequest'))
    );
  }

  getActiveResourceRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/resources/requests`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getActiveResourceRequests', []))
    );
  }

  getResourceRequestById(requestId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/resources/requests/${requestId}`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getResourceRequestById'))
    );
  }

  matchSuppliers(requestId: string, supplierIds: number[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/resources/requests/${requestId}/match`, supplierIds, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('matchSuppliers'))
    );
  }

  // ==================== Template Methods ====================

  createTemplate(template: {
    name: string;
    title: string;
    content: string;
    category: string;
    language: string;
    variables?: string[];
  }): Observable<MessageTemplate> {
    return this.http.post<MessageTemplate>(`${this.apiUrl}/templates`, template, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('createTemplate'))
    );
  }

  getAllTemplates(): Observable<MessageTemplate[]> {
    return this.http.get<MessageTemplate[]>(`${this.apiUrl}/templates`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getAllTemplates', []))
    );
  }

  getTemplateById(id: number): Observable<MessageTemplate> {
    return this.http.get<MessageTemplate>(`${this.apiUrl}/templates/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getTemplateById'))
    );
  }

  updateTemplate(id: number, template: Partial<MessageTemplate>): Observable<MessageTemplate> {
    return this.http.put<MessageTemplate>(`${this.apiUrl}/templates/${id}`, template, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('updateTemplate'))
    );
  }

  deleteTemplate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/templates/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('deleteTemplate'))
    );
  }

  // ==================== Statistics Methods ====================

  getStatistics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/statistics`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getStatistics'))
    );
  }

  refreshStatistics(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/statistics/refresh`, {}, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('refreshStatistics'))
    );
  }

  // ==================== Legacy Methods (for backward compatibility) ====================

  // Audio Messages
  getAudioMessages(filter?: MessageFilter): Observable<AudioMessage[]> {
    // For now, return mock data - will be replaced with actual API call
    return of(DEFAULT_RECENT_MESSAGES);
  }

  createAudioMessage(message: Partial<AudioMessage>): Observable<AudioMessage> {
    // Mock implementation - will be replaced with actual API call
    const newMessage: AudioMessage = {
      id: Math.floor(Math.random() * 10000),
      title: message.title || 'New Audio Message',
      language: message.language || Language.ENGLISH,
      duration: message.duration || '0:00',
      listeners: 0,
      date: new Date().toISOString().split('T')[0],
      status: MessageStatus.SENT,
      targetAudience: message.targetAudience || 'All Members',
      autoPlay: message.autoPlay ?? true,
      createdAt: new Date()
    };
    return of(newMessage);
  }

  // Templates
  getTemplates(): Observable<MessageTemplate[]> {
    return this.getAllTemplates();
  }

  // Dashboard
  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<any>(`${this.apiUrl}/statistics`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getDashboardStats')),
      // Map backend stats to frontend DashboardStats format
      map(stats => ({
        totalMessages: {
          current: stats.totalMessagesSent || 0,
          previous: (stats.totalMessagesSent || 0) - (stats.messagesSentThisWeek || 0),
          trend: stats.deliveryRateChange || 0
        },
        activeAlerts: {
          count: stats.activeAlerts || 0,
          status: AlertStatus.NORMAL // Default mapping
        },
        audioMessages: {
          count: stats.audioMessagesTotal || 0,
          languages: stats.audioLanguagesSupported || 5,
          deliveryRate: stats.deliveryRate || 0,
          deliveryTrend: stats.deliveryRateChange || 0
        }
      }))
    );
  }

  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<any>(`${this.apiUrl}/statistics`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getDashboardSummary')),
      map(stats => ({
        totalMessages: stats.totalMessagesSent || 247,
        totalAlerts: stats.activeAlerts || 23,
        totalAudioMessages: stats.audioMessagesTotal || 87,
        totalListeners: stats.activeMembers || 15420, // Approximate mapping
        averageDeliveryRate: stats.deliveryRate || 94.5,
        recentActivity: [
          {
            id: 1,
            type: 'message',
            action: 'Bulk SMS sent to farmers',
            user: 'Admin',
            timestamp: new Date()
          },
          {
            id: 2,
            type: 'alert',
            action: 'Weather alert created',
            user: 'Admin',
            timestamp: new Date(Date.now() - 3600000)
          }
        ]
      }))
    );
  }

  // Utility methods
  getLanguages(): Language[] {
    return [Language.ENGLISH, Language.FRENCH, Language.FULFULDE, Language.EVONDO, Language.DUALA];
  }

  getCategories(): string[] {
    return ['Payment', 'Meeting', 'Weather', 'General', 'Training', 'Alert', 'Resource'];
  }

  getRegions(): string[] {
    return ['North West', 'South West', 'Littoral', 'Centre', 'West', 'Far North'];
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed:`, error);
      return of(result as T);
    };
  }
}