import { useEffect } from "react";
import { Provider, useSelector } from "react-redux";
import { PersistGate } from "redux-persist/integration/react";
import { NavigationContainer } from "@react-navigation/native";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { ActivityIndicator, View } from "react-native";
import { StatusBar } from "expo-status-bar";
import { store, persistor, RootState } from "./src/store";
import { initializeDatabase } from "./src/db/database";
import { AppNavigator } from "./src/navigation/AppNavigator";
import { OfflineBanner } from "./src/components/OfflineBanner";
import { attachConnectivityListener } from "./src/services/sync/netInfoSync";
import { synchronizeOfflineData } from "./src/services/sync/outboxSyncService";

const AppShell = () => {
  const isOnline = useSelector((state: RootState) => state.app.isOnline);

  useEffect(() => {
    initializeDatabase();
    const unsubscribe = attachConnectivityListener();
    return () => unsubscribe();
  }, []);

  useEffect(() => {
    if (isOnline) {
      synchronizeOfflineData().catch(() => {});
    }
  }, [isOnline]);

  return (
    // Use a plain View with flex:1 — let each screen handle its own SafeArea + background
    <View style={{ flex: 1 }}>
      <OfflineBanner isOnline={isOnline} />
      <NavigationContainer>
        <StatusBar style="light" translucent backgroundColor="transparent" />
        <AppNavigator />
      </NavigationContainer>
    </View>
  );
};

export default function App() {
  return (
    <Provider store={store}>
      <SafeAreaProvider>
        <PersistGate loading={<ActivityIndicator style={{ flex: 1 }} />} persistor={persistor}>
          <AppShell />
        </PersistGate>
      </SafeAreaProvider>
    </Provider>
  );
}