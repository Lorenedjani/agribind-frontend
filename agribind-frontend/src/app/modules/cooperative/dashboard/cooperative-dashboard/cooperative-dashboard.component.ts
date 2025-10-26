import { Component, OnInit, HostListener } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-cooperative-dashboard',
  templateUrl: './cooperative-dashboard.component.html',
  styleUrls: ['./cooperative-dashboard.component.scss'],
  standalone: true,
  imports: [RouterOutlet]
})
export class CooperativeDashboardComponent implements OnInit {
  pageTitle = 'Members';
  currentLanguage = 'EN';
  showLanguageDropdown = false;
  showProfileDropdown = false;

  constructor(private router: Router) {}

  ngOnInit() {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updatePageTitle();
      });
  }

  private updatePageTitle() {
    const url = this.router.url;
    if (url.includes('members')) {
      this.pageTitle = 'Members';
    } else if (url.includes('inventory')) {
      this.pageTitle = 'Inventory';
    } else if (url.includes('credits')) {
      this.pageTitle = 'Microcredits';
    } else if (url.includes('reports')) {
      this.pageTitle = 'Reports';
    }
  }

  toggleLanguageDropdown() {
    this.showLanguageDropdown = !this.showLanguageDropdown;
    this.showProfileDropdown = false;
  }

  toggleProfileDropdown() {
    this.showProfileDropdown = !this.showProfileDropdown;
    this.showLanguageDropdown = false;
  }

  changeLanguage(lang: string) {
    if (lang === 'fr') {
      this.currentLanguage = 'FR';
    } else {
      this.currentLanguage = 'EN';
    }
    this.showLanguageDropdown = false;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.language-selector') && !target.closest('.user-profile')) {
      this.showLanguageDropdown = false;
      this.showProfileDropdown = false;
    }
  }
}
