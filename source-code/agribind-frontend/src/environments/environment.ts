// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080', // ✅ API Gateway URL
  googleMapsApiKey: 'AIzaSyB41DRUbKWJHPxaFjMAwdrzWzbVKartNGg',
  
  // Service URLs (via API Gateway)
  services: {
    userManagement: 'http://localhost:8080/user-management',
    auth: 'http://localhost:8080/auth',
    production: 'http://localhost:8080/production',
    notification: 'http://localhost:8080/notification'
  }
};