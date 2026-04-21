// src/environments/environment.prod.ts
export const environment = {
  production: true,
  demoMode: false,
  apiUrl: 'https://api.agribind.cm',
  googleMapsApiKey: 'YOUR_PRODUCTION_GOOGLE_MAPS_API_KEY',

  services: {
    userManagement: 'https://api.agribind.cm/user-management',
    auth: 'https://api.agribind.cm/auth',
    productionApiUrl: 'https://api.agribind.cm/production/api/v1',
    inventory: 'https://api.agribind.cm/inventory/api',
    notification: 'https://api.agribind.cm/notification'
  }
};