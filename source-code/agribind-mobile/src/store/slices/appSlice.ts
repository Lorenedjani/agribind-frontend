import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { AppLanguage } from "../../types/models";

interface AppState {
  isOnline: boolean;
  language: AppLanguage;
  theme: "light" | "dark";
  hasOpenedOnboarding: boolean;
}

const initialState: AppState = {
  isOnline: true,
  language: "en",
  theme: "dark",
  hasOpenedOnboarding: false,
};

const appSlice = createSlice({
  name: "app",
  initialState,
  reducers: {
    setOnlineStatus: (state, action: PayloadAction<boolean>) => {
      state.isOnline = action.payload;
    },
    setLanguage: (state, action: PayloadAction<AppLanguage>) => {
      state.language = action.payload;
    },
    setTheme: (state, action: PayloadAction<"light" | "dark">) => {
      state.theme = action.payload;
    },
    completeOnboarding: (state) => {
      state.hasOpenedOnboarding = true;
    },
    resetOnboarding: (state) => {
      state.hasOpenedOnboarding = false;
    },
  },
});

export const { setOnlineStatus, setLanguage, setTheme, completeOnboarding, resetOnboarding } = appSlice.actions;
export default appSlice.reducer;
