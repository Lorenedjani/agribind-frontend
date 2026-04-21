import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import {
  AudioMessage, AudioLibraryItem, MessageTemplate, AlertItem,
  DashboardStats, DashboardSummary, MessageFilter,
  Language, AlertStatus, MessageStatus,
  ApiResponse, PaginatedResponse,
  DEFAULT_DASHBOARD_STATS, DEFAULT_RECENT_MESSAGES
} from './communication.model';

@Injectable({ providedIn: 'root' })
export class CommunicationService {
  private apiUrl = environment.services.communication;

  constructor(private http: HttpClient) {}

  private getHeaders(contentType = 'application/json'): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    let h = new HttpHeaders();
    if (contentType) h = h.set('Content-Type', contentType);
    if (token)       h = h.set('Authorization', `Bearer ${token}`);
    return h;
  }

  // ══════════════════════════════════════════════════════════
  // AUDIO URL HELPER
  // ══════════════════════════════════════════════════════════

  /**
   * Converts a server-side file path (e.g. "./uploads/audio/file.webm")
   * to a full URL served by the backend.
   */
  getAudioUrl(filePath: string): string {
    if (!filePath) return '';
    if (filePath.startsWith('http://') || filePath.startsWith('https://')) return filePath;

    // Normalise: strip leading "./" then leading "/"
    const clean = filePath.replace(/^\.\/?/, '').replace(/^\//, '');

    const envBase: string = this.apiUrl ?? '';

    // Already a valid communication endpoint reference
    if (clean.startsWith('api/communications/')) {
      return `/${clean}`;
    }
    if (clean.startsWith('audio/download/') || clean.startsWith('mobile/broadcast-audio')) {
      const base = envBase.replace(/\/+$/, '');
      return `${base}/${clean}`;
    }

    if (envBase.startsWith('http')) {
      // Production: absolute URL — go direct to backend origin
      const backendBase = envBase.replace(/\/api\/communications.*$/, '');
      return `${backendBase}/${clean}`;
    }

    // Development: route through the Angular proxy
    const proxyBase = envBase.replace(/\/+$/, '');
    return `${proxyBase}/${clean}`;
  }

  // ══════════════════════════════════════════════════════════
  // AUDIO PLAYBACK
  // ══════════════════════════════════════════════════════════

  private _currentAudio: HTMLAudioElement | null = null;

  /**
   * Resolves a server filePath to a playable absolute URL and plays it.
   * Handles auth-protected endpoints by fetching as a Blob first.
   * Stops any previously playing audio automatically.
   */
  playAudioFile(filePath: string): void {
    if (!filePath) { console.warn('playAudioFile: no filePath provided'); return; }

    // Stop current audio
    if (this._currentAudio) {
      this._currentAudio.pause();
      this._currentAudio.currentTime = 0;
      this._currentAudio = null;
    }

    const url   = this.getAudioUrl(filePath);
    const token = localStorage.getItem('accessToken');

    console.log('[CommunicationService] playing audio:', url);

    // Always attempt fetch with auth if possible, or direct if not 
    // (though backend usually requires it)
    fetch(url, { 
      headers: { 
        ...(token ? { Authorization: `Bearer ${token}` } : {}) 
      } 
    })
      .then(async res => {
        if (!res.ok) {
          const errorText = await res.text().catch(() => 'Unknown error');
          throw new Error(`HTTP ${res.status} — ${errorText || res.statusText}`);
        }
        return res.blob();
      })
      .then(blob => {
        if (blob.size === 0) throw new Error('Audio file is empty (0 bytes)');
        const objectUrl = URL.createObjectURL(blob);
        const audio = new Audio(objectUrl);
        this._currentAudio = audio;
        
        audio.onended = () => { 
          URL.revokeObjectURL(objectUrl); 
          if (this._currentAudio === audio) this._currentAudio = null; 
        };
        
        audio.onerror = (e) => {
          console.error('[CommunicationService] Audio decoding error:', e);
          URL.revokeObjectURL(objectUrl);
          if (this._currentAudio === audio) this._currentAudio = null;
          alert('Audio decoding failed or format unsupported.');
        };

        return audio.play();
      })
      .catch(err => {
        console.error('[CommunicationService] Playback failed:', err);
        alert(`Could not play audio: ${err.message}`);
      });
  }

  stopAudio(): void {
    if (this._currentAudio) {
      this._currentAudio.pause();
      this._currentAudio.currentTime = 0;
      this._currentAudio = null;
    }
  }
  // ══════════════════════════════════════════════════════════

  /** Return the full audio library for this cooperative. */
  getAudioLibrary(): Observable<AudioLibraryItem[]> {
    return this.http.get<AudioLibraryItem[]>(
      `${this.apiUrl}/audio/library`,
      { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError('getAudioLibrary', [])));
  }

  /**
   * Upload a new audio asset to the library.
   * FormData fields: file (Blob), title (string), language (string)
   */
  uploadToAudioLibrary(formData: FormData): Observable<AudioLibraryItem> {
    const token = localStorage.getItem('accessToken');
    let h = new HttpHeaders();
    if (token) h = h.set('Authorization', `Bearer ${token}`);
    return this.http.post<AudioLibraryItem>(
      `${this.apiUrl}/audio/library`,
      formData,
      { headers: h }
    ).pipe(catchError(this.handleError<AudioLibraryItem>('uploadToAudioLibrary')));
  }

  /** Rename an audio library item. */
  renameAudioLibraryItem(id: number, title: string): Observable<AudioLibraryItem> {
    return this.http.patch<AudioLibraryItem>(
      `${this.apiUrl}/audio/library/${id}`,
      { title },
      { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError<AudioLibraryItem>('renameAudioLibraryItem')));
  }

  /** Delete an audio library item. */
  deleteAudioLibraryItem(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/audio/library/${id}`,
      { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError<void>('deleteAudioLibraryItem')));
  }

  // ══════════════════════════════════════════════════════════
  // BROADCASTS / ANNOUNCEMENTS
  // ══════════════════════════════════════════════════════════

  /**
   * Upload a single audio file for a broadcast variant.
   * (Used when the manager records inline during broadcast creation
   *  and hasn't saved to the library yet.)
   */
  uploadAudioMessage(formData: FormData): Observable<any> {
    const token = localStorage.getItem('accessToken');
    let h = new HttpHeaders();
    if (token) h = h.set('Authorization', `Bearer ${token}`);
    return this.http.post(`${this.apiUrl}/audio/upload`, formData, { headers: h })
      .pipe(catchError(this.handleError('uploadAudioMessage')));
  }

  broadcastAudioMessage(request: any, audioFileId: number): Observable<any> {
    const params = new HttpParams().set('audioFileId', audioFileId.toString());
    return this.http.post(`${this.apiUrl}/audio/broadcast`, request, {
      headers: this.getHeaders(), params
    }).pipe(catchError(this.handleError('broadcastAudioMessage')));
  }

  // ══════════════════════════════════════════════════════════
  // BROADCASTS (dashboard)
  // ══════════════════════════════════════════════════════════

  getBroadcasts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/broadcasts`, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('getBroadcasts', [])));
  }

  createBroadcast(request: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/broadcasts`, request, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('createBroadcast')));
  }

  renameBroadcast(broadcastId: number, title: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/broadcasts/${broadcastId}`, { title }, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('renameBroadcast')));
  }

  deleteBroadcast(broadcastId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/broadcasts/${broadcastId}`, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError<void>('deleteBroadcast')));
  }

  getSupportedLanguages(): Observable<string[]> {
    return this.http.get<string[]>(
      `${this.apiUrl}/audio/languages`,
      { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError('getSupportedLanguages', [])));
  }

  // ══════════════════════════════════════════════════════════
  // ALERTS
  // ══════════════════════════════════════════════════════════

  createAlert(request: {
    title:          string;
    type:           string;
    priority:       string;
    targetAudience: string;
    specificZone?:  string;
    content:        string;
    channels:       string[];
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/alerts`, request, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('createAlert')));
  }

  getAlertHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/alerts/history`, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('getAlertHistory', [])));
  }

  updateAlert(alertId: string, request: {
    title:          string;
    type:           string;
    priority:       string;
    targetAudience: string;
    specificZone?:  string;
    content:        string;
    channels:       string[];
  }): Observable<any> {
    return this.http.put(`${this.apiUrl}/alerts/${alertId}`, request, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('updateAlert')));
  }

  deleteAlert(alertId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/alerts/${alertId}`, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError<void>('deleteAlert')));
  }

  markAlertSent(alertId: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/alerts/${alertId}/send`, null, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('markAlertSent')));
  }

  getAlertById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/alerts/${id}`, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('getAlertById')));
  }

  // ══════════════════════════════════════════════════════════
  // TEMPLATES
  // ══════════════════════════════════════════════════════════

  createTemplate(template: {
    name: string; title: string; content: string;
    category: string; language: string; variables?: string[];
  }): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}/templates`, template, { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError<any>('createTemplate')));
  }

  getAllTemplates(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiUrl}/templates`, { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError('getAllTemplates', [])));
  }

  getTemplateById(id: number): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/templates/${id}`, { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError<any>('getTemplateById')));
  }

  updateTemplate(id: number, template: Partial<{ name: string; content: string; variables?: string[] }>): Observable<any> {
    return this.http.put<any>(
      `${this.apiUrl}/templates/${id}`, template, { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError<any>('updateTemplate')));
  }

  deleteTemplate(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/templates/${id}`, { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError<void>('deleteTemplate')));
  }

  // ══════════════════════════════════════════════════════════
  // SMS
  // ══════════════════════════════════════════════════════════

  sendBulkSms(request: {
    targetAudience: string;
    specificZone?:  string;
    content:        string;
    priority:       'NORMAL' | 'HIGH' | 'URGENT';
    scheduledAt?:   Date;
    templateId?:    string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/sms/bulk`, request, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('sendBulkSms')));
  }

  // ══════════════════════════════════════════════════════════
  // RESOURCE REQUESTS
  // ══════════════════════════════════════════════════════════

  createResourceRequest(request: {
    resourceName:    string; quantity: number; unit: string;
    urgency:         'LOW' | 'MEDIUM' | 'HIGH';
    requestedBy:     string; requestedByZone?: string;
    requiredDate?:   Date;   notes?: string;
    budgetAmount?:   number; currency?: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/resources/requests`, request, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('createResourceRequest')));
  }

  getActiveResourceRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/resources/requests`, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('getActiveResourceRequests', [])));
  }

  matchSuppliers(requestId: string, supplierIds: number[]): Observable<void> {
    return this.http.put<void>(
      `${this.apiUrl}/resources/requests/${requestId}/match`, supplierIds, { headers: this.getHeaders() }
    ).pipe(catchError(this.handleError<void>('matchSuppliers')));
  }

  // ══════════════════════════════════════════════════════════
  // STATISTICS
  // ══════════════════════════════════════════════════════════

  getStatistics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/statistics`, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError('getStatistics', null)));
  }

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<any>(`${this.apiUrl}/statistics`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getDashboardStats')),
      map(stats => ({
        totalMessages: {
          current:  stats?.totalMessagesSent   ?? 0,
          previous: (stats?.totalMessagesSent  ?? 0) - (stats?.messagesSentThisWeek ?? 0),
          trend:    stats?.deliveryRateChange  ?? 0
        },
        activeAlerts:  { count: stats?.activeAlerts ?? 0, status: AlertStatus.NORMAL },
        audioMessages: {
          count:         stats?.audioMessagesTotal    ?? 0,
          languages:     stats?.audioLanguagesSupported ?? 5,
          deliveryRate:  stats?.deliveryRate          ?? 0,
          deliveryTrend: stats?.deliveryRateChange    ?? 0
        }
      }))
    );
  }

  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<any>(`${this.apiUrl}/statistics`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError('getDashboardSummary')),
      map(stats => ({
        totalMessages:       stats?.totalMessagesSent   ?? 247,
        totalAlerts:         stats?.activeAlerts        ?? 23,
        totalAudioMessages:  stats?.audioMessagesTotal  ?? 87,
        totalListeners:      stats?.activeMembers       ?? 15420,
        averageDeliveryRate: stats?.deliveryRate        ?? 94.5,
        recentActivity: [
          { id: 1, type: 'message' as const, action: 'Bulk SMS sent to farmers',  user: 'Admin', timestamp: new Date() },
          { id: 2, type: 'alert'   as const, action: 'Weather alert created',     user: 'Admin', timestamp: new Date(Date.now() - 3_600_000) }
        ]
      }))
    );
  }

  // ══════════════════════════════════════════════════════════
  // LEGACY helpers (kept for backward compatibility)
  // ══════════════════════════════════════════════════════════

  getAudioMessages(_filter?: MessageFilter): Observable<AudioMessage[]> {
    return of(DEFAULT_RECENT_MESSAGES);
  }

  getTemplates(): Observable<MessageTemplate[]> { return this.getAllTemplates(); }

  getLanguages():  Language[] { return Object.values(Language) as Language[]; }
  getCategories(): string[]   { return ['Payment','Meeting','Weather','General','Training','Alert','Resource']; }
  getRegions():    string[]   { return ['North West','South West','Littoral','Centre','West','Far North']; }

  // ══════════════════════════════════════════════════════════
  // ERROR HANDLER
  // ══════════════════════════════════════════════════════════

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed:`, error);
      return of(result as unknown as T);
    };
  }
}
