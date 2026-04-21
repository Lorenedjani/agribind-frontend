import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

// ─────────────────────────────────────────────────────────────────────────────
// Interfaces
// ─────────────────────────────────────────────────────────────────────────────

export interface TrainingAnnouncement {
  id: string;
  title: string;
  languages: string[];
  targetAudience: string;
  regions: string[];
  crops: string[];
  scheduleDate: Date | null;
  status: 'Draft' | 'Scheduled' | 'Sent';
  reachCount?: number;
  engagementRate?: number;
  audioUrl?: string | null;
}

export interface QuickTemplate {
  title: string;
  description: string;
  icon: string;
}

export interface AudioLibraryItem {
  id: number;
  fileName: string;
  filePath: string;
  title: string;
  language: string;
}

type SubmitState = 'idle' | 'uploading' | 'success' | 'error';

// ─────────────────────────────────────────────────────────────────────────────
// Component
// ─────────────────────────────────────────────────────────────────────────────

@Component({
  selector: 'app-training-announcements',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './training-announcements.component.html',
  styleUrls: ['./training-announcements.component.scss']
})
export class TrainingAnnouncementsComponent implements OnInit {

  // ── API base ──────────────────────────────────────────────────────────────
  /**
   * Government agents POST to /government/announcements.
   * The mobile endpoint GET /mobile/announcements serves both cooperative
   * and government broadcasts, so farmers receive everything automatically.
   */
  private readonly govApi = `${environment.services.communication}/government`;

  // ── Form state ─────────────────────────────────────────────────────────────
  title             = '';
  isRecording       = false;
  recordingLanguage = 'French';
  targetAudience    = 'All Farmers';
  selectedRegions:  string[] = [];
  selectedCrops:    string[] = [];
  selectedLanguages: string[] = ['French'];
  scheduleDateStr   = '';

  // Edit mode
  editingId: string | null = null;

  // Recording state
  private mediaRecorder: MediaRecorder | null = null;
  private audioChunks:   Blob[]               = [];
  recordedAudioUrl: string | null = null;
  private recordedAudioBlob: Blob | null = null;

  // Submit state
  submitState: SubmitState = 'idle';
  submitError  = '';

  // ── Static reference data ─────────────────────────────────────────────────
  readonly languages: string[] = ['French', 'English', 'Fulfulde', 'Ewondo', 'Duala'];

  readonly regions: string[] = [
    'Centre', 'Littoral', 'West', 'South-West', 'North-West',
    'Adamawa', 'North', 'Far North', 'East', 'South'
  ];

  readonly crops: string[] = [
    'Cocoa', 'Coffee', 'Palm Oil', 'Cotton',
    'Cassava', 'Maize', 'Plantain', 'Rice'
  ];

  readonly quickTemplates: QuickTemplate[] = [
    { title: 'Seasonal Planting Guide',    description: 'Pre-configured for current season',   icon: '🌱' },
    { title: 'Emergency Weather Alert',    description: 'Rapid deployment for weather warnings', icon: '⛈️' },
    { title: 'Market Price Update',        description: 'Linked to latest price changes',        icon: '💰' },
    { title: 'Disease Outbreak Warning',   description: 'Emergency health alerts',               icon: '🦠' },
  ];

  // ── Announcement list — populated from server only ────────────────────────
  announcements: TrainingAnnouncement[] = [];
  announcementsLoading = false;
  audioLibrary: AudioLibraryItem[] = [];

  constructor(private http: HttpClient) {}

  // ── Lifecycle ──────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.loadAudioLibrary();
  }

  // ── Auth header ───────────────────────────────────────────────────────────
  private get authHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    return token
      ? new HttpHeaders({ Authorization: `Bearer ${token}` })
      : new HttpHeaders();
  }

  // ── Computed helpers ──────────────────────────────────────────────────────
  get todayStr(): string {
    return new Date().toISOString().split('T')[0];
  }

  get scheduleDate(): Date | undefined {
    return this.scheduleDateStr ? new Date(this.scheduleDateStr) : undefined;
  }

  // ── Load list from server ─────────────────────────────────────────────────
  loadAudioLibrary(): void {
    const libUrl = `${environment.services.communication}/audio/library`;
    this.http.get<AudioLibraryItem[]>(libUrl, { headers: this.authHeaders })
      .subscribe({
        next: (items) => {
          this.audioLibrary = items;
          this.loadAnnouncementsFromServer();
        },
        error: (err) => {
          console.error('Failed to load audio library:', err);
          this.loadAnnouncementsFromServer(); // Still try to load announcements
        }
      });
  }

  loadAnnouncementsFromServer(): void {
    this.announcementsLoading = true;
    this.http.get<any[]>(`${this.govApi}/announcements`, { headers: this.authHeaders })
      .subscribe({
        next: (items) => {
          this.announcementsLoading = false;

          // Build a map of audio library items to get IDs by filename
          const audioMap = new Map<string, number>();
          this.audioLibrary.forEach(item => {
            const fileName = (item.fileName || '').trim();
            const filePath = (item.filePath || '').trim();
            if (fileName) audioMap.set(fileName, item.id);
            if (filePath) {
              audioMap.set(filePath, item.id);
              audioMap.set(filePath.replace(/^\.\/?/, ''), item.id);
              audioMap.set(filePath.split(/[/\\]/).pop() || '', item.id);
            }
          });

          this.announcements = items.map(item => {
            // Get the audio file path from the first variant
            const filePath = item.variants?.[0]?.filePath || null;
            let audioUrl = filePath;

            // If we have a filePath, try to find the audio library ID
            if (filePath) {
              const normalizedFilePath = String(filePath).replace(/^\.\/?/, '');
              const bareName = normalizedFilePath.split(/[/\\]/).pop() || '';
              if (audioMap.has(filePath)) {
                const audioId = audioMap.get(filePath);
                audioUrl = `/api/communications/audio/download/${audioId}`;
              } else if (audioMap.has(normalizedFilePath)) {
                const audioId = audioMap.get(normalizedFilePath);
                audioUrl = `/api/communications/audio/download/${audioId}`;
              } else if (audioMap.has(bareName)) {
                const audioId = audioMap.get(bareName);
                audioUrl = `/api/communications/audio/download/${audioId}`;
              } else {
                // Try to find by filename pattern (stripping any path)
                const filename = filePath.split(/[/\\]/).pop() || '';
                const matchingItem = this.audioLibrary.find(a =>
                  a.fileName === filename || a.filePath?.endsWith(filename)
                );
                if (matchingItem) {
                  audioUrl = `/api/communications/audio/download/${matchingItem.id}`;
                }
              }
            }

            return {
              id:             String(item.id),
              title:          (item.title ?? '').replace(/^\[GOV\]\s*/, ''),
              languages:      item.variants?.map((v: any) => v.language).filter(Boolean) ?? [],
              targetAudience: item.targetAudience ?? 'All Farmers',
              regions:        [],
              crops:          [],
              scheduleDate:   item.date ? new Date(item.date) : null,
              status:         this.mapServerStatus(item.status),
              reachCount:     item.listeners ?? item.reachCount ?? 0,
              engagementRate: item.engagementRate ?? 0,
              audioUrl:       audioUrl,
            };
          });
        },
        error: (err) => {
          this.announcementsLoading = false;
          console.warn('Could not load announcements from server:', err.message);
        }
      });
  }

  private mapServerStatus(status: string): 'Draft' | 'Scheduled' | 'Sent' {
    const s = (status ?? '').toLowerCase();
    if (s === 'scheduled') return 'Scheduled';
    if (s === 'sent')      return 'Sent';
    return 'Draft';
  }

  // ── Recording ─────────────────────────────────────────────────────────────
  async handleRecord(): Promise<void> {
    if (!this.isRecording) {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        this.mediaRecorder = new MediaRecorder(stream);
        this.audioChunks   = [];

        this.mediaRecorder.ondataavailable = (event) => {
          if (event.data.size > 0) this.audioChunks.push(event.data);
        };

        this.mediaRecorder.onstop = () => {
          this.recordedAudioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
          if (this.recordedAudioUrl) URL.revokeObjectURL(this.recordedAudioUrl);
          this.recordedAudioUrl = URL.createObjectURL(this.recordedAudioBlob);
          stream.getTracks().forEach(track => track.stop());
          this.isRecording = false;
        };

        this.mediaRecorder.start();
        this.isRecording = true;
      } catch (err) {
        console.error('Microphone access denied or error:', err);
        alert('Unable to access microphone. Please allow microphone access and try again.');
      }
    } else {
      this.isRecording = false;
      this.mediaRecorder?.stop();
      this.mediaRecorder = null;
    }
  }

  // ── Multi-select toggles ──────────────────────────────────────────────────
  toggleLanguage(lang: string): void {
    this.selectedLanguages = this.selectedLanguages.includes(lang)
      ? this.selectedLanguages.filter(l => l !== lang)
      : [...this.selectedLanguages, lang];
  }

  toggleRegion(region: string): void {
    this.selectedRegions = this.selectedRegions.includes(region)
      ? this.selectedRegions.filter(r => r !== region)
      : [...this.selectedRegions, region];
  }

  toggleCrop(crop: string): void {
    this.selectedCrops = this.selectedCrops.includes(crop)
      ? this.selectedCrops.filter(c => c !== crop)
      : [...this.selectedCrops, crop];
  }

  // ── Estimated reach ───────────────────────────────────────────────────────
  getEstimatedReach(): string {
    switch (this.targetAudience) {
      case 'All Farmers':      return '127,543';
      case 'Specific Regions': return (this.selectedRegions.length * 12000).toLocaleString();
      case 'Crop Specific':    return (this.selectedCrops.length * 8000).toLocaleString();
      default:                 return '0';
    }
  }

  // ── Map UI audience → backend enum ───────────────────────────────────────
  private mapAudience(uiValue: string): string {
    const map: Record<string, string> = {
      'All Farmers':      'ALL_MEMBERS',
      'Specific Regions': 'ACTIVE_MEMBERS',
      'Crop Specific':    'ACTIVE_MEMBERS',
    };
    return map[uiValue] ?? 'ALL_MEMBERS';
  }

  // ── Create or update announcement ─────────────────────────────────────────
  /**
   * If audio has been recorded → POST to backend as multipart.
   * If no audio (text-only / editing metadata) → update local list only.
   */
  createOrUpdateAnnouncement(): void {
    if (!this.title.trim()) return;

    if (this.recordedAudioBlob) {
      this.publishToBackend();
    } else {
      this.saveLocally();
    }
  }

  // ── Publish to backend ────────────────────────────────────────────────────
  private publishToBackend(): void {
    if (!this.recordedAudioBlob) return;

    const formData = new FormData();
    formData.append('file',           this.recordedAudioBlob, `announcement_${Date.now()}.webm`);
    formData.append('title',          this.title);
    formData.append('language',       this.recordingLanguage);
    formData.append('targetAudience', this.mapAudience(this.targetAudience));
    formData.append('source',         'Government Training Unit');

    this.submitState = 'uploading';
    this.submitError = '';

    this.http.post<any>(`${this.govApi}/announcements`, formData, { headers: this.authHeaders })
      .subscribe({
        next: (result) => {
          this.submitState = 'success';

          // Add to local list immediately
          const newItem: TrainingAnnouncement = {
            id:             String(result.id ?? Date.now()),
            title:          this.title,
            languages:      [this.recordingLanguage],
            targetAudience: this.targetAudience,
            regions:        this.targetAudience === 'Specific Regions' ? [...this.selectedRegions] : [],
            crops:          this.targetAudience === 'Crop Specific'    ? [...this.selectedCrops]   : [],
            scheduleDate:   this.scheduleDate ?? null,
            status:         'Sent',
            reachCount:     0,
            engagementRate: 0,
            audioUrl:       result.audioUrl ?? this.recordedAudioUrl,
          };
          this.announcements = [newItem, ...this.announcements];

          setTimeout(() => {
            this.submitState = 'idle';
            this.resetForm();
          }, 2500);
        },
        error: (err) => {
          this.submitState = 'error';
          this.submitError = err?.error?.error ?? err?.message ?? 'Upload failed. Please try again.';
          console.error('Publish error:', err);
        }
      });
  }

  // ── Save locally (no audio / edit mode) ──────────────────────────────────
  private saveLocally(): void {
    if (this.editingId) {
      this.announcements = this.announcements.map(item =>
        item.id === this.editingId
          ? {
              ...item,
              title:          this.title,
              languages:      [...this.selectedLanguages],
              targetAudience: this.targetAudience,
              regions:        this.targetAudience === 'Specific Regions' ? [...this.selectedRegions] : [],
              crops:          this.targetAudience === 'Crop Specific'    ? [...this.selectedCrops]   : [],
              scheduleDate:   this.scheduleDate ?? null,
              audioUrl:       this.recordedAudioUrl || item.audioUrl,
            }
          : item
      );
    } else {
      const newAnnouncement: TrainingAnnouncement = {
        id:             Date.now().toString(),
        title:          this.title,
        languages:      [...this.selectedLanguages],
        targetAudience: this.targetAudience,
        regions:        this.targetAudience === 'Specific Regions' ? [...this.selectedRegions] : [],
        crops:          this.targetAudience === 'Crop Specific'    ? [...this.selectedCrops]   : [],
        scheduleDate:   this.scheduleDate ?? null,
        status:         'Draft',
        reachCount:     0,
        engagementRate: 0,
        audioUrl:       this.recordedAudioUrl,
      };
      this.announcements = [newAnnouncement, ...this.announcements];
    }
    this.resetForm();
  }

  // ── Send a draft ──────────────────────────────────────────────────────────
  sendAnnouncement(id: string): void {
    this.announcements = this.announcements.map(item =>
      item.id === id
        ? {
            ...item,
            status:         'Sent' as const,
            reachCount:     Math.floor(Math.random() * 20000) + 10000,
            engagementRate: Math.floor(Math.random() * 30) + 60,
          }
        : item
    );
  }

  // ── Apply quick template ──────────────────────────────────────────────────
  applyTemplate(template: QuickTemplate): void {
    this.title = template.title;
  }

  // ── Edit ──────────────────────────────────────────────────────────────────
  editAnnouncement(id: string): void {
    const announcement = this.announcements.find(a => a.id === id);
    if (!announcement) return;

    this.editingId         = id;
    this.title             = announcement.title;
    this.selectedLanguages = [...announcement.languages];
    this.targetAudience    = announcement.targetAudience;
    this.selectedRegions   = [...announcement.regions];
    this.selectedCrops     = [...announcement.crops];
    this.scheduleDateStr   = announcement.scheduleDate
      ? announcement.scheduleDate.toISOString().split('T')[0]
      : '';

    if (this.recordedAudioUrl) {
      URL.revokeObjectURL(this.recordedAudioUrl);
      this.recordedAudioUrl  = null;
      this.recordedAudioBlob = null;
    }

    const form = document.querySelector('.creation-grid');
    if (form) form.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  // ── Play audio ────────────────────────────────────────────────────────────
  private currentAudio: HTMLAudioElement | null = null;

  private resolveAudioUrl(audioRef: string): string {
    if (!audioRef) return '';
    if (audioRef.startsWith('http://') || audioRef.startsWith('https://')) return audioRef;

    const envBase = environment.services.communication.replace(/\/+$/, '');
    const cleanedRef = audioRef.trim();

    if (cleanedRef.startsWith('/api/communications/')) return cleanedRef;
    if (cleanedRef.startsWith('/audio/download/') || cleanedRef.startsWith('/mobile/broadcast-audio')) {
      return `${envBase}${cleanedRef}`;
    }
    if (cleanedRef.startsWith('audio/download/') || cleanedRef.startsWith('mobile/broadcast-audio')) {
      return `${envBase}/${cleanedRef}`;
    }

    // Filename-only refs must be translated to download endpoint using audio library ID.
    const filename = cleanedRef.replace(/^\.\/?/, '').split(/[/\\]/).pop() || '';
    const filenamePattern = /^[a-f0-9-]+_[A-Za-z]+_\d+\.(webm|mp3|wav|m4a|aac)$/i;
    if (filenamePattern.test(filename)) {
      const match = this.audioLibrary.find(item =>
        item.fileName === filename || item.filePath?.endsWith(filename)
      );
      if (match?.id) {
        return `${envBase}/audio/download/${match.id}`;
      }
    }

    return '';
  }

  playAudio(id: string): void {
    const announcement = this.announcements.find(a => a.id === id);
    if (!announcement?.audioUrl) {
      alert('No audio recorded for this announcement.');
      return;
    }

    // Stop any currently playing audio
    if (this.currentAudio) {
      this.currentAudio.pause();
      this.currentAudio.currentTime = 0;
      this.currentAudio = null;
    }

    const url = this.resolveAudioUrl(announcement.audioUrl);
    console.log('[playAudio] resolved URL:', url);

    if (!url) {
      alert('Invalid audio reference. Please re-upload the announcement audio.');
      return;
    }

    const token = localStorage.getItem('accessToken');

    // Always use fetch with Authorization header to ensure authenticated access
    fetch(url, {
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
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
        this.currentAudio = audio;
        audio.onended = () => { URL.revokeObjectURL(objectUrl); this.currentAudio = null; };
        audio.onerror = (e) => {
          console.error('[playAudio] Audio decoding/playback error:', e);
          URL.revokeObjectURL(objectUrl);
          this.currentAudio = null;
          alert('Audio decoding failed or format unsupported.');
        };
        return audio.play();
      })
      .catch(err => {
        console.error('[playAudio] failed:', err);
        alert(`Could not play audio: ${err.message}`);
      });
  }

  // ── Reset form ────────────────────────────────────────────────────────────
  private resetForm(): void {
    this.title             = '';
    this.selectedLanguages = ['French'];
    this.targetAudience    = 'All Farmers';
    this.selectedRegions   = [];
    this.selectedCrops     = [];
    this.scheduleDateStr   = '';
    this.editingId         = null;
    this.submitState       = 'idle';
    this.submitError       = '';

    if (this.recordedAudioUrl) {
      URL.revokeObjectURL(this.recordedAudioUrl);
      this.recordedAudioUrl  = null;
      this.recordedAudioBlob = null;
    }
    if (this.mediaRecorder) {
      if (this.isRecording) { this.mediaRecorder.stop(); this.isRecording = false; }
      this.mediaRecorder = null;
    }
    this.audioChunks = [];
  }

  // ── Status helpers ────────────────────────────────────────────────────────
  getStatusClass(status: string): Record<string, boolean> {
    return {
      'status-badge--sent':      status === 'Sent',
      'status-badge--scheduled': status === 'Scheduled',
      'status-badge--draft':     status === 'Draft',
    };
  }

  getStatusIcon(status: string): string {
    const icons: Record<string, string> = { Sent: '✅', Scheduled: '🕐', Draft: '⚠️' };
    return icons[status] ?? '🕐';
  }
}
