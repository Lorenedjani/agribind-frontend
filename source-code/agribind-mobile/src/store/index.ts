import AsyncStorage from "@react-native-async-storage/async-storage";
import { combineReducers, configureStore } from "@reduxjs/toolkit";
import {
  persistReducer,
  persistStore,
  FLUSH,
  REHYDRATE,
  PAUSE,
  PERSIST,
  PURGE,
  REGISTER,
} from "redux-persist";
import appReducer from "./slices/appSlice";
import authReducer from "./slices/authSlice";
import notificationsReducer from "./slices/notificationsSlice";
import marketReducer from "./slices/marketSlice";
import announcementReducer from "./slices/announcementSlice";

const appPersistConfig = {
  key: "agribind-app",
  storage: AsyncStorage,
  // Persist hasOpenedOnboarding so onboarding shows once per install, then Auth/Main.
};

const persistedAppReducer = persistReducer(appPersistConfig, appReducer);

const rootReducer = combineReducers({
  app: persistedAppReducer,
  auth: authReducer,
  notifications: notificationsReducer,
  market: marketReducer,
  announcements: announcementReducer,
});

const persistConfig = {
  key: "agribind-mobile",
  storage: AsyncStorage,
  whitelist: ["app", "auth", "notifications", "market", "announcements"],
};

const persistedReducer = persistReducer(persistConfig, rootReducer);

export const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
      },
    }),
});

export const persistor = persistStore(store);

export type RootState = ReturnType<typeof rootReducer>;
export type AppDispatch = typeof store.dispatch;
