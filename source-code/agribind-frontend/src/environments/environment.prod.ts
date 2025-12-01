// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://api.agribind.cm',
  googleMapsApiKey: 'YOUR_PRODUCTION_GOOGLE_MAPS_API_KEY',

  services: {
    userManagement: 'https://api.agribind.cm/user-management',
    auth: 'https://api.agribind.cm/auth',
    production: 'https://api.agribind.cm/production',
    notification: 'https://api.agribind.cm/notification'
  }
};