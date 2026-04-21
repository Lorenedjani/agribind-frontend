import React, { useRef, useEffect } from "react";
import {
  View, Text, StyleSheet, TouchableOpacity, Animated, ImageBackground, Dimensions,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSelector, useDispatch } from "react-redux";
import { RootState } from "../store";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { RootStackParamList } from "../navigation/AppNavigator";
import { completeOnboarding } from "../store/slices/appSlice";
import { THEMES } from "../theme";
import { StatusBar } from "expo-status-bar";

const { height } = Dimensions.get("window");

type Props = NativeStackScreenProps<RootStackParamList, "Onboarding">;

const BG_URL = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?q=80&w=1000";

export const OnboardingScreen = ({ navigation }: Props) => {
  const dispatch = useDispatch();
  const insets = useSafeAreaInsets();
  const fadeAnim = useRef(new Animated.Value(0)).current;
  const slideAnim = useRef(new Animated.Value(40)).current;

  // Use themeMode from Redux
  const themeMode = useSelector((state: RootState) => state.app.theme) || "dark";
  const C = THEMES[themeMode].colors;

  useEffect(() => {
    Animated.parallel([
      Animated.timing(fadeAnim, { toValue: 1, duration: 800, useNativeDriver: true }),
      Animated.spring(slideAnim, { toValue: 0, friction: 8, useNativeDriver: true }),
    ]).start();
  }, []);

  return (
    <ImageBackground source={{ uri: BG_URL }} style={styles.container} resizeMode="cover">
      <StatusBar style="light" />
      
      {/* Dark gradient / tint overlay for readability */}
      <View style={[styles.overlay, { backgroundColor: "rgba(12, 24, 16, 0.4)" }]} />

      <Animated.View
        style={[
          styles.content,
          {
            paddingBottom: insets.bottom + 20,
            opacity: fadeAnim,
            transform: [{ translateY: slideAnim }]
          }
        ]}
      >
        {/* Spacer to help center the text vertically */}
        <View style={styles.topSpacer} />

        {/* Centered Text */}
        <View style={styles.textWrap}>
          <Text style={styles.appName}>WELCOME TO{"\n"}AGRIBIND</Text>
          <Text style={styles.tagline}>Connect, grow, and thrive with your agricultural community.</Text>
        </View>

        {/* Spacer that pushes button to bottom */}
        <View style={styles.bottomSpacer} />

        {/* Button at the bottom */}
        <TouchableOpacity
          style={[styles.startBtn, { backgroundColor: "rgba(20, 25, 20, 0.85)", borderColor: "rgba(255,255,255,0.1)" }]}
          onPress={() => {
            dispatch(completeOnboarding());
          }}
          activeOpacity={0.85}
        >
          <View style={styles.btnLeft}>
            <Text style={styles.btnArrow}>‹</Text>
            <View style={[styles.btnIconCircle, { backgroundColor: C.primary }]}>
              <Text style={styles.btnLeaf}>🌿</Text>
            </View>
          </View>

          <Text style={styles.btnText}>Get Started</Text>

          <Text style={styles.btnArrowsRight}>›››</Text>
        </TouchableOpacity>

      </Animated.View>
    </ImageBackground>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    width: "100%",
    height: "100%",
  },
  overlay: {
    ...StyleSheet.absoluteFillObject,
  },
  content: {
    flex: 1,
    justifyContent: "space-between", // This creates space between elements
    alignItems: "center",
    paddingHorizontal: 24,
  },
  topSpacer: {
    flex: 1, // Takes up remaining space above text
  },
  textWrap: {
    alignItems: "center",
    width: "100%",
  },
  bottomSpacer: {
    flex: 1, // Takes up remaining space below text, pushing button down
  },
  appName: {
    fontSize: 34,
    fontWeight: "900",
    color: "#fff",
    textAlign: "center",
    letterSpacing: -0.5,
    marginBottom: 12,
    lineHeight: 40,
  },
  tagline: {
    fontSize: 14,
    color: "rgba(255,255,255,0.8)",
    textAlign: "center",
    lineHeight: 22,
    fontWeight: "500",
  },

  startBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    width: "100%",
    height: 64,
    borderRadius: 32,
    paddingHorizontal: 6,
    borderWidth: 1,
    marginBottom: 20, // Adds some spacing from bottom safe area
  },
  btnLeft: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
  },
  btnArrow: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 22,
    marginLeft: 12,
    marginTop: -2,
  },
  btnIconCircle: {
    width: 52,
    height: 52,
    borderRadius: 26,
    alignItems: "center",
    justifyContent: "center",
  },
  btnLeaf: {
    fontSize: 20,
  },
  btnText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "700",
    marginLeft: -10,
  },
  btnArrowsRight: {
    color: "rgba(255,255,255,0.4)",
    fontSize: 18,
    marginRight: 20,
    letterSpacing: 1,
    marginTop: -2,
  },
});