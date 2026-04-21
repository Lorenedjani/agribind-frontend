import { createSlice, createAsyncThunk, PayloadAction } from "@reduxjs/toolkit";
import { AppNotification } from "../../types/models";
import { ENDPOINTS } from "../../config/api";
import { RootState } from "../index";

interface NotificationsState {
  items: AppNotification[];
  loading: boolean;
  error: string | null;
}

const initialState: NotificationsState = {
  items: [],
  loading: false,
  error: null,
};

export const fetchNotifications = createAsyncThunk(
  "notifications/fetch",
  async (_, { getState, rejectWithValue }) => {
    try {
      const state = getState() as RootState;
      const userId = state.auth.profile?.id || state.auth.profile?.phoneNumber; // Fallback to phone if ID isn't set yet
      if (!userId) {
        throw new Error("User ID not available");
      }

      const response = await fetch(ENDPOINTS.NOTIFICATIONS(userId));
      if (!response.ok) throw new Error("Failed to fetch notifications");
      
      const data = await response.json();
      return data as AppNotification[];
    } catch (e: any) {
      return rejectWithValue(e.message);
    }
  }
);

export const markNotificationRead = createAsyncThunk(
  "notifications/markRead",
  async (id: string, { rejectWithValue }) => {
    try {
      await fetch(ENDPOINTS.MARK_READ(id), { method: "PUT" });
      return id;
    } catch (e: any) {
      return rejectWithValue(e.message);
    }
  }
);

export const markAllNotificationsRead = createAsyncThunk(
  "notifications/markAllRead",
  async (_, { getState, rejectWithValue }) => {
    try {
      const state = getState() as RootState;
      const userId = state.auth.profile?.id || state.auth.profile?.phoneNumber;
      if (userId) {
         await fetch(ENDPOINTS.MARK_ALL_READ(userId), { method: "PUT" });
      }
      return true;
    } catch (e: any) {
      return rejectWithValue(e.message);
    }
  }
);

const notificationsSlice = createSlice({
  name: "notifications",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      // Fetch
      .addCase(fetchNotifications.pending, (state) => { state.loading = true; })
      .addCase(fetchNotifications.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchNotifications.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      // Mark single read
      .addCase(markNotificationRead.fulfilled, (state, action) => {
        const n = state.items.find(i => i.id === action.payload);
        if (n) n.read = true;
      })
      // Mark all read
      .addCase(markAllNotificationsRead.fulfilled, (state) => {
        state.items.forEach(i => { i.read = true; });
      });
  },
});

export default notificationsSlice.reducer;
