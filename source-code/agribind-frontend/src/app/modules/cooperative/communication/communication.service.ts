import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
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
  private apiUrl = `${environment.services.communication}/communications`;

  constructor(private http: HttpClient) {}

  // ==================== SMS Methods ====================

  sendBulkSms(request: {
    targetAudience: 'ALL_MEMBERS' | 'ACTIVE_MEMBERS' | 'DOUALA_ZONE' | 'YAOUNDE_ZONE' | 'OTHER_REGIONS' | 'CUSTOM';
    specificZone?: string;
    content: string;
    priority: 'NORMAL' | 'HIGH' | 'URGENT';
    scheduledAt?: Date;
    templateId?: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/sms/bulk`, request).pipe(
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
    return this.http.post(`${this.apiUrl}/sms/compose`, request).pipe(
      catchError(this.handleError('composeBulkMessage'))
    );
  }

  // ==================== Audio Message Methods ====================

  uploadAudioMessage(formData: FormData): Observable<any> {
    return this.http.post(`${this.apiUrl}/audio/upload`, formData).pipe(
      catchError(this.handleError('uploadAudioMessage'))
    );
  }

  broadcastAudioMessage(request: any, audioFileId: number): Observable<any> {
    const params = new HttpParams().set('audioFileId', audioFileId.toString());
    return this.http.post(`${this.apiUrl}/audio/broadcast`, request, { params }).pipe(
      catchError(this.handleError('broadcastAudioMessage'))
    );
  }

  getSupportedLanguages(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/audio/languages`).pipe(
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
    return this.http.post(`${this.apiUrl}/alerts`, request).pipe(
      catchError(this.handleError('createAlert'))
    );
  }

  getAlertHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/alerts/history`).pipe(
      catchError(this.handleError('getAlertHistory', []))
    );
  }

  getAlertById(alertId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/alerts/${alertId}`).pipe(
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
    return this.http.post(`${this.apiUrl}/resources/requests`, request).pipe(
      catchError(this.handleError('createResourceRequest'))
    );
  }

  getActiveResourceRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/resources/requests`).pipe(
      catchError(this.handleError('getActiveResourceRequests', []))
    );
  }

  getResourceRequestById(requestId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/resources/requests/${requestId}`).pipe(
      catchError(this.handleError('getResourceRequestById'))
    );
  }

  matchSuppliers(requestId: string, supplierIds: number[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/resources/requests/${requestId}/match`, supplierIds).pipe(
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
    return this.http.post<MessageTemplate>(`${this.apiUrl}/templates`, template).pipe(
      catchError(this.handleError('createTemplate'))
    );
  }

  getAllTemplates(): Observable<MessageTemplate[]> {
    return this.http.get<MessageTemplate[]>(`${this.apiUrl}/templates`).pipe(
      catchError(this.handleError('getAllTemplates', []))
    );
  }

  getTemplateById(id: number): Observable<MessageTemplate> {
    return this.http.get<MessageTemplate>(`${this.apiUrl}/templates/${id}`).pipe(
      catchError(this.handleError('getTemplateById'))
    );
  }

  updateTemplate(id: number, template: Partial<MessageTemplate>): Observable<MessageTemplate> {
    return this.http.put<MessageTemplate>(`${this.apiUrl}/templates/${id}`, template).pipe(
      catchError(this.handleError('updateTemplate'))
    );
  }

  deleteTemplate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/templates/${id}`).pipe(
      catchError(this.handleError('deleteTemplate'))
    );
  }

  // ==================== Statistics Methods ====================

  getStatistics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/statistics`).pipe(
      catchError(this.handleError('getStatistics'))
    );
  }

  refreshStatistics(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/statistics/refresh`, {}).pipe(
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
    return of(DEFAULT_DASHBOARD_STATS);
  }

  getDashboardSummary(): Observable<DashboardSummary> {
    const summary: DashboardSummary = {
      totalMessages: 247,
      totalAlerts: 23,
      totalAudioMessages: 87,
      totalListeners: 15420,
      averageDeliveryRate: 94.5,
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
    };
    return of(summary);
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