import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-simple-communication',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="simple-communication">
      <h1>Communications Center</h1>
      <p>This is a simple communication component for testing.</p>
    </div>
  `,
  styles: [`
    .simple-communication {
      padding: 20px;
      text-align: center;
    }
    h1 {
      color: #328048;
    }
  `]
})
export class SimpleCommunicationComponent {}


