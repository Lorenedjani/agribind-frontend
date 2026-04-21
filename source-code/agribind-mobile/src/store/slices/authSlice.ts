import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { REHYDRATE } from "redux-persist";
import { FarmerProfile } from "../../types/models";
import { ENDPOINTS } from "../../config/api";

// Fetch with a 30-second timeout — prevents premature Aborted errors on slow starts
const fetchWithTimeout = (url: string, options: RequestInit, ms = 30000): Promise<Response> => {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), ms);
  return fetch(url, { ...options, signal: controller.signal }).finally(() => clearTimeout(timer));
};





interface AuthState {
  token: string | null;
  profile: FarmerProfile | null;
  loading: boolean;
  error: string | null;
  otpSent: boolean;
}

const initialState: AuthState = {
  token: null,
  profile: null,
  loading: false,
  error: null,
  otpSent: false,
};

export const sendPhoneOtp = createAsyncThunk(
  "auth/sendOtp",
  async (phoneNumber: string, { rejectWithValue }) => {
    try {
      const response = await fetchWithTimeout(`${ENDPOINTS.AUTH_BASE}/phone/send-otp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phoneNumber }),
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Failed to send OTP");
      return data;
    } catch (e: any) {
      return rejectWithValue(e.message);
    }
  }
);

export const verifyPhoneOtp = createAsyncThunk(
  "auth/verifyOtp",
  async ({ phoneNumber, otp }: { phoneNumber: string; otp: string }, { rejectWithValue }) => {
    try {
      const response = await fetchWithTimeout(`${ENDPOINTS.AUTH_BASE}/phone/verify-otp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phoneNumber, otpCode: otp }), // 👈 FIX: backend expects otpCode
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Verification failed");

      // Map backend UserInfo to mobile FarmerProfile
      const userInfo = data.data.userInfo;
      const profile: FarmerProfile = {
          id: userInfo.userId,
          name: userInfo.username || userInfo.email || phoneNumber,
          phoneNumber: phoneNumber,
          preferredLanguage: (userInfo.preferredLanguage || "fr") as any,
          cooperativeId: userInfo.cooperativeId,
      };

      return { token: data.data.accessToken, profile };
    } catch (e: any) {
      return rejectWithValue(e.message);
    }
  }
);

/**
 * QR Login: Scanned data is "USER:FXXXXX:LOGIN:..."
 * We parse the second part as the registrationNumber.
 */
export const loginWithQr = createAsyncThunk(
  "auth/loginWithQr",
  async (qrData: string, { rejectWithValue }) => {
    try {
      console.log(`Loging in with QR Data: ${qrData.substring(0, 15)}...`);

      const response = await fetchWithTimeout(`${ENDPOINTS.AUTH_BASE}/qr-login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ qrData }),
      });

      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "QR Login failed");

      const parts = qrData.split(":");
      const userId = parts.length > 1 ? parts[1] : qrData;

      const userInfo = data.data.userInfo;
      const profile: FarmerProfile = {
          id: userInfo.userId,
          name: userInfo.username || userInfo.email || userId,
          phoneNumber: "", // Not provided in QR login
          preferredLanguage: (userInfo.preferredLanguage || "fr") as any,
          cooperativeId: userInfo.cooperativeId,
      };

      return { token: data.data.accessToken, profile };
    } catch (e: any) {
      return rejectWithValue(e.message);
    }
  }
);

export const updateFarmerProfile = createAsyncThunk(
    "auth/updateProfile",
    async ({ userId, profile }: { userId: string; profile: Partial<FarmerProfile> }, { getState, rejectWithValue }) => {
      try {
        const state: any = getState();
        const response = await fetchWithTimeout(`${ENDPOINTS.USERS_BASE}/${userId}`, {
          method: "PUT",
          headers: {
              "Content-Type": "application/json",
              "Authorization": `Bearer ${state.auth.token}`
          },
          body: JSON.stringify(profile),
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || "Profile update failed");
        return profile;
      } catch (e: any) {
        return rejectWithValue(e.message);
      }
    }
);

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setSession: (
      state,
      action: PayloadAction<{ token: string; profile: FarmerProfile }>
    ) => {
      state.token = action.payload.token;
      state.profile = action.payload.profile;
    },
    clearSession: (state) => {
      state.token = null;
      state.profile = null;
      state.otpSent = false;
      state.loading = false;
      state.error = null;
    },
    resetError: (state) => {
      state.error = null;
      state.loading = false;
    },
    // Explicitly unstick any persisted loading state on app boot
    resetLoading: (state) => {
      state.loading = false;
      state.error = null;
      state.otpSent = false;
    },
  },
  extraReducers: (builder) => {
    builder
      // Always reset transient UI state on redux-persist rehydration
      .addCase(REHYDRATE, (state, action: any) => {
        if (action.payload?.auth) {
          state.loading = false;
          state.error = null;
          state.otpSent = false;
          state.token = action.payload.auth.token ?? null;
          state.profile = action.payload.auth.profile ?? null;
        }
      })
      // Send OTP
      .addCase(sendPhoneOtp.pending, (state) => { state.loading = true; state.error = null; })
      .addCase(sendPhoneOtp.fulfilled, (state) => { state.loading = false; state.otpSent = true; })
      .addCase(sendPhoneOtp.rejected, (state, action) => { state.loading = false; state.error = action.payload as string; })

      // Verify OTP
      .addCase(verifyPhoneOtp.pending, (state) => { state.loading = true; state.error = null; })
      .addCase(verifyPhoneOtp.fulfilled, (state, action) => {
        state.loading = false;
        state.token = action.payload.token;
        state.profile = action.payload.profile;
      })
      .addCase(verifyPhoneOtp.rejected, (state, action) => { state.loading = false; state.error = action.payload as string; })

      // QR Login
      .addCase(loginWithQr.pending, (state) => { state.loading = true; state.error = null; })
      .addCase(loginWithQr.fulfilled, (state, action) => {
          state.loading = false;
          state.token = action.payload.token;
          state.profile = action.payload.profile;
      })
      .addCase(loginWithQr.rejected, (state, action) => { state.loading = false; state.error = action.payload as string; })

      // Update Profile
      .addCase(updateFarmerProfile.fulfilled, (state, action) => {
          if (state.profile) {
              state.profile = { ...state.profile, ...action.payload };
          }
      });
  },
});

export const { setSession, clearSession, resetError, resetLoading } = authSlice.actions;
export default authSlice.reducer;