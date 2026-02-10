// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8084', // ✅ Changed from 8082 to 8084
  googleMapsApiKey: 'AIzaSyB41DRUbKWJHPxaFjMAwdrzWzbVKartNGg',

  // Service URLs (via API Gateway at port 8084)
  services: {
    userManagement: 'http://localhost:8084/api/v1', // Updated to 8084
    auth: 'http://localhost:8084/api/v1/auth', // Updated to 8084
    productionApiUrl: 'http://localhost:8084/api/v1/production', // Updated to 8084
    inventory: 'http://localhost:8084/api/inventory', // Updated to 8084
    communication: 'http://localhost:8084/api/communications', // Updated to 8084
    plantMonitoring: 'http://localhost:8084/api/v1', // Updated to 8084
    notification: 'http://localhost:8084/api/notifications', // Updated to 8084
    microcredit: 'http://localhost:8084/api/microcredit' // Updated to 8084
  }
};