import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { Communication } from './communication.model';

@Injectable({
  providedIn: 'root'
})
export class CommunicationService {
  private communications: Communication[] = [
    {
      id: '1',
      title: 'Maize Market Prices Update',
      content: 'Current maize prices are favorable. Sell now for best returns.',
      language: 'English',
      category: 'market_prices',
      targetRegions: ['North West', 'South West'],
      targetCrops: ['Maize'],
      status: 'published',
      createdBy: 'Admin',
      createdAt: new Date('2024-01-15'),
      publishedAt: new Date('2024-01-15'),
      reachCount: 150,
      broadcastType: 'mobile_app'
    },
    {
      id: '2',
      title: 'Coffee Planting Season Alert',
      content: 'Coffee planting season starts next week. Prepare your fields.',
      language: 'French',
      category: 'seasonal_alerts',
      targetRegions: ['West', 'Littoral'],
      targetCrops: ['Coffee'],
      status: 'scheduled',
      scheduledTime: new Date('2024-01-20'),
      createdBy: 'Admin',
      createdAt: new Date('2024-01-10'),
      broadcastType: 'mobile_app'
    },
    {
      id: '3',
      title: 'Pest Warning for Cocoa Farmers',
      content: 'Black pod disease detected in cocoa farms. Apply fungicide immediately.',
      language: 'Pidgin',
      category: 'pest_warning',
      targetRegions: ['South West', 'Centre'],
      targetCrops: ['Cocoa'],
      status: 'published',
      createdBy: 'Admin',
      createdAt: new Date('2024-01-05'),
      publishedAt: new Date('2024-01-05'),
      reachCount: 89,
      broadcastType: 'sms'
    },
    {
      id: '4',
      title: 'Rainfall Forecast Alert',
      content: 'Heavy rainfall expected next 3 days. Harvest ripe crops.',
      language: 'English',
      category: 'weather_alerts',
      targetRegions: ['North West', 'Far North'],
      targetCrops: ['Maize', 'Rice'],
      status: 'published',
      createdBy: 'Admin',
      createdAt: new Date('2024-01-03'),
      publishedAt: new Date('2024-01-03'),
      reachCount: 203,
      broadcastType: 'voice_call'
    },
    {
      id: '5',
      title: 'New Fertilizer Subsidy',
      content: 'Government announces 30% subsidy on organic fertilizers.',
      language: 'French',
      category: 'general_info',
      targetRegions: ['Littoral', 'Centre', 'West'],
      targetCrops: [],
      status: 'draft',
      createdBy: 'Admin',
      createdAt: new Date('2024-01-02')
    }
  ];

  private communicationsSubject = new BehaviorSubject<Communication[]>(this.communications);

  getCommunications(): Observable<Communication[]> {
    return this.communicationsSubject.asObservable();
  }

  getCommunicationById(id: string): Observable<Communication | null> {
    const communication = this.communications.find(c => c.id === id);
    return of(communication || null);
  }

  createCommunication(communication: Omit<Communication, 'id' | 'createdAt'>): Observable<Communication> {
    const newCommunication: Communication = {
      ...communication,
      id: this.generateId(),
      createdAt: new Date()
    };
    
    this.communications.push(newCommunication);
    this.communicationsSubject.next([...this.communications]);
    return of(newCommunication);
  }

  updateCommunication(id: string, updates: Partial<Communication>): Observable<Communication> {
    const index = this.communications.findIndex(c => c.id === id);
    if (index !== -1) {
      this.communications[index] = { ...this.communications[index], ...updates };
      this.communicationsSubject.next([...this.communications]);
      return of(this.communications[index]);
    }
    throw new Error('Communication not found');
  }

  deleteCommunication(id: string): Observable<boolean> {
    const index = this.communications.findIndex(c => c.id === id);
    if (index !== -1) {
      this.communications.splice(index, 1);
      this.communicationsSubject.next([...this.communications]);
      return of(true);
    }
    return of(false);
  }

  getLanguages(): string[] {
    return ['English', 'French', 'Pidgin', 'Local Dialect'];
  }

  getCategories(): string[] {
    return ['market_prices', 'weather_alerts', 'training', 'emergency', 'general_info', 'seasonal_alerts', 'pest_warning', 'success_story'];
  }

  getRegions(): string[] {
    return ['North West', 'South West', 'Littoral', 'Centre', 'West', 'Far North'];
  }

  getCrops(): string[] {
    return ['Cocoa', 'Coffee', 'Maize', 'Plantains', 'Cotton', 'Palm Oil', 'Cassava', 'Rice'];
  }

  getBroadcastTypes(): string[] {
    return ['mobile_app', 'sms', 'voice_call'];
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9);
  }
}