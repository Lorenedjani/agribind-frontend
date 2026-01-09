// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8082', // ✅ API Gateway URL
  googleMapsApiKey: 'AIzaSyB41DRUbKWJHPxaFjMAwdrzWzbVKartNGg',

  // Service URLs (via API Gateway at port 8082)
  services: {
    userManagement: 'http://localhost:8082/api/v1', // Via API Gateway
    auth: 'http://localhost:8082/api/v1/auth', // Via API Gateway
    productionApiUrl: 'http://localhost:8082/api/v1/production', // Production monitoring via Gateway
    inventory: 'http://localhost:8082/api/inventory', // Inventory via Gateway
    communication: 'http://localhost:8082/api/communications', // Communication via Gateway
    plantMonitoring: 'http://localhost:8082/api/v1', // Plant monitoring via Gateway
    notification: 'http://localhost:8082/api/notifications', // Notification via Gateway
    microcredit: 'http://localhost:8082/api/microcredit' // Microcredit via Gateway
  }
};