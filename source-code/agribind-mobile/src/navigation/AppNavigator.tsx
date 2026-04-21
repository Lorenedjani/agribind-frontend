import React, { useRef, useEffect } from "react";
import {
  View, Text, TouchableOpacity, StyleSheet, Platform, Animated, Dimensions,
} from "react-native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSelector } from "react-redux";
import { RootState } from "../store";
import { t } from "../utils/i18n";

import { OnboardingScreen } from "../screens/OnboardingScreen";
import { AuthScreen } from "../screens/AuthScreen";
import { MarketPricesScreen } from "../screens/MarketPricesScreen";
import { AnnouncementsScreen } from "../screens/AnnouncementsScreen";
import { WeatherScreen } from "../screens/WeatherScreen";
import { CreditsScreen } from "../screens/CreditsScreen";
import { ProfileScreen } from "../screens/ProfileScreen";
import { NotificationsScreen } from "../screens/NotificationsScreen";

export type RootStackParamList = {
  Onboarding: undefined;
  Auth: undefined;
  Main: undefined;
  Notifications: undefined;
};

export type MainTabParamList = {
  Home: undefined;
  Prices: undefined;
  Announcements: undefined;
  Weather: undefined;
  Credits: undefined;
  Profile: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<MainTabParamList>();
const { width } = Dimensions.get("window");

import { HomeScreen } from "../screens/HomeScreen";
import { THEMES } from "../theme";

const TABS = [
  { name: "Home",          labelKey: "nav_home",           icon: "🏠", activeIcon: "🏠" },
  { name: "Prices",        labelKey: "nav_prices",         icon: "📊", activeIcon: "📊" },
  { name: "Announcements", labelKey: "nav_announcements",  icon: "📢", activeIcon: "📢" },
  { name: "Weather",       labelKey: "nav_weather",        icon: "🌦", activeIcon: "🌦" },
  { name: "Credits",       labelKey: "nav_credits",        icon: "💳", activeIcon: "💳" },
  { name: "Profile",       labelKey: "nav_profile",        icon: "👤", activeIcon: "👤" },
];

// ─── Single animated tab item ──────
const TabItem = ({ tabItem, focused, onPress, lang, theme }: {
  tabItem: typeof TABS[0]; focused: boolean; onPress: () => void; lang: string; theme: any;
}) => {
  const C = theme.colors;
  const scale = useRef(new Animated.Value(focused ? 1 : 0.85)).current;
  useEffect(() => {
    Animated.spring(scale, { toValue: focused ? 1 : 0.85, friction: 6, useNativeDriver: true }).start();
  }, [focused]);

  return (
    <TouchableOpacity style={styles.tabItem} onPress={onPress} activeOpacity={0.7}>
      <Animated.View style={[styles.tabIconWrap, focused && { backgroundColor: `${C.primary}26` }, { transform: [{ scale }] }]}>
        <Text style={styles.tabIcon}>{tabItem.icon}</Text>
      </Animated.View>
      <Text style={[styles.tabLabel, { color: focused ? C.primaryLight : C.textMuted }]}>
        {t(tabItem.labelKey, lang as any)}
      </Text>
    </TouchableOpacity>
  );
};

// ─── Smart Garden AI-style Tab Bar ────────────────────────────────────────
const SmartTabBar = ({ state, navigation }: any) => {
  const insets = useSafeAreaInsets();
  const lang = useSelector((st: RootState) => st.app.language);
  const themeMode = useSelector((st: RootState) => st.app.theme) || "dark";
  const theme = THEMES[themeMode];
  const C = theme.colors;
  
  const indicatorX = useRef(new Animated.Value(0)).current;
  const TAB_W = width / TABS.length;

  useEffect(() => {
    Animated.spring(indicatorX, {
      toValue: state.index * TAB_W + TAB_W / 2 - 20,
      friction: 7, tension: 50, useNativeDriver: false,
    }).start();
  }, [state.index]);

  const pb = Platform.OS === "ios" ? (insets.bottom || 20) : 8;

  return (
    <View style={[styles.tabBarWrapper, { paddingBottom: pb, backgroundColor: C.surface, borderTopColor: C.glassBorder }]}>
      <Animated.View style={[styles.activeDot, { left: indicatorX, backgroundColor: C.primary }]} />
      {TABS.map((tabItem, idx) => (
        <TabItem
          key={tabItem.name}
          tabItem={tabItem}
          focused={state.index === idx}
          onPress={() => navigation.navigate(tabItem.name)}
          lang={lang}
          theme={theme}
        />
      ))}
    </View>
  );
};

// ─── Main Tabs ─────────────────────────────────────────────────────────────
const MainTabs = () => {
  const themeMode = useSelector((st: RootState) => st.app.theme) || "dark";
  const C = THEMES[themeMode].colors;
  
  return (
    <Tab.Navigator
      tabBar={(props) => <SmartTabBar {...props} />}
      screenOptions={{
        headerShown: false,
        sceneStyle: { backgroundColor: C.bg },
      }}
    >
      <Tab.Screen name="Home"          component={HomeScreen} />
      <Tab.Screen name="Prices"        component={MarketPricesScreen} />
      <Tab.Screen name="Announcements" component={AnnouncementsScreen} />
      <Tab.Screen name="Weather"       component={WeatherScreen} />
      <Tab.Screen name="Credits"       component={CreditsScreen} />
      <Tab.Screen name="Profile"       component={ProfileScreen} />
    </Tab.Navigator>
  );
};

// ─── Root Nav ──────────────────────────────────────────────────────────────
// Using conditional rendering instead of key/initialRouteName to avoid
// race conditions where the stack remounts before navigation completes.
export const AppNavigator = () => {
  const { token } = useSelector((state: RootState) => state.auth);
  const { hasOpenedOnboarding } = useSelector((state: RootState) => state.app);

  // Step 1: Show onboarding on first ever launch
  if (!hasOpenedOnboarding) {
    return (
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="Onboarding" component={OnboardingScreen} />
      </Stack.Navigator>
    );
  }

  // Step 2: Not authenticated → show login
  if (!token) {
    return (
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="Auth" component={AuthScreen} />
      </Stack.Navigator>
    );
  }

  // Step 3: Authenticated → main app
  // Notifications is here so it can be pushed from any tab screen
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Main" component={MainTabs} />
      <Stack.Screen name="Notifications" component={NotificationsScreen} />
    </Stack.Navigator>
  );
};

const styles = StyleSheet.create({
  tabBarWrapper: {
    flexDirection: "row",
    borderTopWidth: 1,
    paddingTop: 8,
    position: "relative",
  },
  activeDot: {
    position: "absolute",
    top: 0,
    width: 40,
    height: 3,
    borderRadius: 2,
  },
  tabItem: {
    flex: 1,
    alignItems: "center",
    paddingVertical: 4,
    gap: 4,
  },
  tabIconWrap: {
    width: 44,
    height: 44,
    borderRadius: 14,
    alignItems: "center",
    justifyContent: "center",
  },
  tabIcon: { fontSize: 22 },
  tabLabel: {
    fontSize: 10,
    fontWeight: "700",
    textTransform: "uppercase",
    letterSpacing: 0.3,
  },
});