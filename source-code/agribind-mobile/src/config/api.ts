// api.config.ts
import { Platform } from "react-native";

const BACKEND_CANDIDATES = [
  "http://10.203.148.210:8082",  // Try primary first
  "http://10.133.169.206:8082",  // Then secondary
  "http://10.133.169.166:8082",  // Maybe on same machine as Expo?
];

// Try environment variable first
const envBase =
  typeof process !== "undefined" && process.env?.EXPO_PUBLIC_API_BASE_URL
    ? String(process.env.EXPO_PUBLIC_API_BASE_URL).replace(/\/$/, "")
    : "";

// Use environment variable or first candidate
export const API_BASE_URL = envBase || BACKEND_CANDIDATES[0];

// Helper to test if API is working
export async function testApiConnection(): Promise<boolean> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/health`);
    return response.ok;
  } catch (error) {
    console.error("API connection failed:", error);
    return false;
  }
}

// Fallback mechanism to try different servers
export async function getWorkingApiUrl(): Promise<string> {
  if (envBase) return envBase;
  
  for (const candidate of BACKEND_CANDIDATES) {
    try {
      const response = await fetch(`${candidate}/api/v1/health`, { 
        method: 'HEAD',
        timeout: 2000 
      });
      if (response.ok) {
        console.log(`✅ Connected to ${candidate}`);
        return candidate;
      }
    } catch (error) {
      console.log(`❌ Failed to connect to ${candidate}`);
    }
  }
  
  // Fallback to default
  return Platform.OS === "android" ? "http://10.0.2.2:8082" : "http://localhost:8082";
}// api.config.ts
 import { Platform } from "react-native";

 const BACKEND_CANDIDATES = [
   "http://10.203.148.210:8082",  // Try primary first
   "http://10.133.169.206:8082",  // Then secondary
   "http://10.133.169.166:8082",  // Maybe on same machine as Expo?
 ];

 // Try environment variable first
 const envBase =
   typeof process !== "undefined" && process.env?.EXPO_PUBLIC_API_BASE_URL
     ? String(process.env.EXPO_PUBLIC_API_BASE_URL).replace(/\/$/, "")
     : "";

 // Use environment variable or first candidate
 export const API_BASE_URL = envBase || BACKEND_CANDIDATES[0];

 // Helper to test if API is working
 export async function testApiConnection(): Promise<boolean> {
   try {
     const response = await fetch(`${API_BASE_URL}/api/v1/health`);
     return response.ok;
   } catch (error) {
     console.error("API connection failed:", error);
     return false;
   }
 }

 // Fallback mechanism to try different servers
 export async function getWorkingApiUrl(): Promise<string> {
   if (envBase) return envBase;

   for (const candidate of BACKEND_CANDIDATES) {
     try {
       const response = await fetch(`${candidate}/api/v1/health`, {
         method: 'HEAD',
         timeout: 2000
       });
       if (response.ok) {
         console.log(`✅ Connected to ${candidate}`);
         return candidate;
       }
     } catch (error) {
       console.log(`❌ Failed to connect to ${candidate}`);
     }
   }

   // Fallback to default
   return Platform.OS === "android" ? "http://10.0.2.2:8082" : "http://localhost:8082";
 }