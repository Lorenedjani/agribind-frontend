// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8082', // ✅ API Gateway URL
  googleMapsApiKey: 'AIzaSyB41DRUbKWJHPxaFjMAwdrzWzbVKartNGg',

  // Service URLs (via API Gateway)
  services: {
    userManagement: 'http://localhost:8080/user-management',
    auth: 'http://localhost:8080/auth',
    productionApiUrl: 'http://localhost:8085/api/v1', // Production monitoring service
    inventory: 'http://localhost:8086/api', // Inventory service
    communication: 'http://localhost:8087/api', // Communication service
    plantMonitoring: 'http://localhost:8088/api', // Plant monitoring service
    notification: 'http://localhost:8080/notification'
  }
};