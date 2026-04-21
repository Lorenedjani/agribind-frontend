import React, { useState } from "react";
import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSelector } from "react-redux";
import { RootState } from "../store";
import { Header } from "../components/Header";
import { t } from "../utils/i18n";
import { THEMES } from "../theme";

const REGIONS = ["Yaoundé", "Douala", "Garoua", "Bafoussam", "Maroua"];

type Condition = "sunny" | "cloudy" | "rainy" | "stormy";

const DATA: Record<string, {
  high: number; low: number; cond: Condition; humidity: number; wind: number;
  advisory: string;
  forecast: { day: string; cond: Condition; high: number; low: number }[];
}> = {
  "Yaoundé": {
    high: 28, low: 20, cond: "cloudy", humidity: 72, wind: 14,
    advisory: "Bon moment pour planter. Pas de pluie intense. Irriguez tôt le matin.",
    forecast: [
      { day: "Lun", cond: "cloudy", high: 28, low: 20 },
      { day: "Mar", cond: "rainy", high: 25, low: 19 },
      { day: "Mer", cond: "sunny", high: 30, low: 21 },
      { day: "Jeu", cond: "cloudy", high: 27, low: 20 },
      { day: "Ven", cond: "rainy", high: 24, low: 18 },
    ],
  },
  "Douala": {
    high: 31, low: 24, cond: "rainy", humidity: 88, wind: 22,
    advisory: "Forte pluie prévue. Protégez vos récoltes et stockez sous abri.",
    forecast: [
      { day: "Lun", cond: "rainy", high: 31, low: 24 },
      { day: "Mar", cond: "stormy", high: 28, low: 23 },
      { day: "Mer", cond: "rainy", high: 29, low: 23 },
      { day: "Jeu", cond: "cloudy", high: 30, low: 23 },
      { day: "Ven", cond: "sunny", high: 32, low: 24 },
    ],
  },
  "Garoua": {
    high: 38, low: 26, cond: "sunny", humidity: 22, wind: 18,
    advisory: "Forte chaleur. Irriguez tôt le matin. Évitez les travaux entre 11h-15h.",
    forecast: [
      { day: "Lun", cond: "sunny", high: 38, low: 26 },
      { day: "Mar", cond: "sunny", high: 39, low: 27 },
      { day: "Mer", cond: "cloudy", high: 35, low: 25 },
      { day: "Jeu", cond: "sunny", high: 37, low: 26 },
      { day: "Ven", cond: "sunny", high: 40, low: 28 },
    ],
  },
  "Bafoussam": {
    high: 25, low: 17, cond: "cloudy", humidity: 65, wind: 12,
    advisory: "Temps agréable pour le café et le maïs. Vérifiez la santé des cultures.",
    forecast: [
      { day: "Lun", cond: "cloudy", high: 25, low: 17 },
      { day: "Mar", cond: "sunny", high: 27, low: 18 },
      { day: "Mer", cond: "cloudy", high: 24, low: 17 },
      { day: "Jeu", cond: "rainy", high: 22, low: 16 },
      { day: "Ven", cond: "cloudy", high: 25, low: 17 },
    ],
  },
  "Maroua": {
    high: 41, low: 28, cond: "sunny", humidity: 15, wind: 30,
    advisory: "Conditions très sèches. Système d'irrigation essentiel. Alerte canicule.",
    forecast: [
      { day: "Lun", cond: "sunny", high: 41, low: 28 },
      { day: "Mar", cond: "sunny", high: 42, low: 29 },
      { day: "Mer", cond: "sunny", high: 40, low: 27 },
      { day: "Jeu", cond: "cloudy", high: 37, low: 26 },
      { day: "Ven", cond: "sunny", high: 39, low: 27 },
    ],
  },
};

const COND_EMOJI: Record<Condition, string> = { sunny: "☀️", cloudy: "⛅", rainy: "🌧️", stormy: "⛈️" };
const COND_LABEL: Record<Condition, string> = { sunny: "Ensoleillé", cloudy: "Nuageux", rainy: "Pluvieux", stormy: "Orageux" };
const COND_GRAD: Record<Condition, [string, string]> = {
  sunny:  ["#E65100", "#FF8F00"],
  cloudy: ["#1A3520", "#2E5035"],
  rainy:  ["#0D2B3E", "#1565C0"],
  stormy: ["#1A1A2E", "#16213E"],
};

export const WeatherScreen = () => {
  const insets = useSafeAreaInsets();
  const lang = useSelector((state: RootState) => state.app.language);
  const themeMode = useSelector((state: RootState) => state.app.theme) || "dark";
  const C = THEMES[themeMode].colors;
  
  const [region, setRegion] = useState("Yaoundé");
  const weather = DATA[region];
  const grad = COND_GRAD[weather.cond];

  return (
    <View style={[styles.container, { backgroundColor: grad[0], paddingTop: insets.top }]}>
      <Header />
      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingBottom: 40 }}>

        {/* Region Selector */}
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.regionScroll}>
          {REGIONS.map((r) => (
            <TouchableOpacity
              key={r} onPress={() => setRegion(r)} activeOpacity={0.8}
              style={[styles.regionChip, region === r && { backgroundColor: C.primary, borderColor: C.primary }]}
            >
              <Text style={[styles.regionText, region === r && { color: "#fff" }]}>{r}</Text>
            </TouchableOpacity>
          ))}
        </ScrollView>

        {/* Hero */}
        <View style={styles.hero}>
          <View style={[styles.heroBg, { backgroundColor: grad[1], opacity: 0.3 }]} />
          <View style={styles.heroContent}>
            <View style={styles.heroTopRow}>
              <View>
                <Text style={styles.heroLabel}>📍 {region}</Text>
                <Text style={styles.heroTitle}>{t("weather_title", lang)}</Text>
              </View>
            </View>

            {/* Giant condition */}
            <Text style={styles.heroEmoji}>{COND_EMOJI[weather.cond]}</Text>
            <Text style={styles.heroTemp}>{weather.high}°C</Text>
            <Text style={styles.heroCondLabel}>{COND_LABEL[weather.cond]}</Text>

            {/* Quick stats */}
            <View style={styles.quickStats}>
              <View style={styles.quickStat}><Text style={styles.qsVal}>💧 {weather.humidity}%</Text><Text style={styles.qsLbl}>Humidité</Text></View>
              <View style={styles.qsDivider} />
              <View style={styles.quickStat}><Text style={styles.qsVal}>🌡 {weather.low}°</Text><Text style={styles.qsLbl}>Min</Text></View>
              <View style={styles.qsDivider} />
              <View style={styles.quickStat}><Text style={styles.qsVal}>💨 {weather.wind}km/h</Text><Text style={styles.qsLbl}>Vent</Text></View>
            </View>
          </View>
        </View>

        {/* 5-Day Forecast */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Prévisions 5 jours</Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.forecastScroll}>
            {weather.forecast.map((f, i) => (
              <View key={i} style={styles.forecastCard}>
                <Text style={styles.forecastDay}>{f.day}</Text>
                <Text style={styles.forecastEmoji}>{COND_EMOJI[f.cond]}</Text>
                <Text style={styles.forecastHigh}>{f.high}°</Text>
                <Text style={styles.forecastLow}>{f.low}°</Text>
              </View>
            ))}
          </ScrollView>
        </View>

        {/* Agricultural Advisory */}
        <View style={[styles.advisoryCard, { backgroundColor: `${C.primary}1A`, borderColor: `${C.primary}40` }]}>
          <View style={styles.advisoryHeader}>
            <Text style={styles.advisoryIcon}>🌾</Text>
            <Text style={[styles.advisoryTitle, { color: C.primaryLight }]}>Conseil Agricole</Text>
          </View>
          <Text style={styles.advisoryText}>{weather.advisory}</Text>
        </View>
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  hero: { paddingHorizontal: 16, paddingBottom: 24, overflow: "hidden" },
  heroBg: { ...StyleSheet.absoluteFillObject, borderRadius: 24, margin: 16 },
  heroContent: { padding: 32 },
  heroTopRow: { flexDirection: "row", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 20 },
  heroLabel: { fontSize: 13, color: "rgba(255,255,255,0.6)", fontWeight: "700", marginBottom: 4 },
  heroTitle: { fontSize: 26, fontWeight: "900", color: "#fff" },
  heroEmoji: { fontSize: 80, textAlign: "center", marginBottom: 8 },
  heroTemp: { fontSize: 64, fontWeight: "900", color: "#fff", textAlign: "center", letterSpacing: -3 },
  heroCondLabel: { fontSize: 18, color: "rgba(255,255,255,0.7)", textAlign: "center", fontWeight: "700", marginBottom: 20 },

  quickStats: {
    flexDirection: "row", backgroundColor: "rgba(255,255,255,0.1)", borderRadius: 20,
    padding: 16, borderWidth: 1, borderColor: "rgba(255,255,255,0.15)", justifyContent: "space-around",
  },
  quickStat: { alignItems: "center" },
  qsVal: { fontSize: 14, fontWeight: "800", color: "#fff", marginBottom: 4 },
  qsLbl: { fontSize: 11, color: "rgba(255,255,255,0.5)", fontWeight: "600" },
  qsDivider: { width: 1, backgroundColor: "rgba(255,255,255,0.15)" },

  regionScroll: { paddingHorizontal: 16, paddingVertical: 16, gap: 10 },
  regionChip: {
    paddingHorizontal: 18, paddingVertical: 9, borderRadius: 20,
    backgroundColor: "rgba(255,255,255,0.12)", borderWidth: 1, borderColor: "rgba(255,255,255,0.2)",
  },
  regionText: { fontSize: 14, fontWeight: "700", color: "rgba(255,255,255,0.7)" },

  section: { paddingHorizontal: 16, marginBottom: 16 },
  sectionTitle: { fontSize: 14, fontWeight: "800", color: "rgba(255,255,255,0.8)", marginBottom: 12, textTransform: "uppercase", letterSpacing: 0.5 },
  forecastScroll: { gap: 10 },
  forecastCard: {
    backgroundColor: "rgba(255,255,255,0.1)", borderRadius: 18, padding: 16,
    alignItems: "center", minWidth: 70, borderWidth: 1, borderColor: "rgba(255,255,255,0.15)",
  },
  forecastDay: { fontSize: 12, fontWeight: "700", color: "rgba(255,255,255,0.6)", marginBottom: 8 },
  forecastEmoji: { fontSize: 26, marginBottom: 6 },
  forecastHigh: { fontSize: 15, fontWeight: "900", color: "#fff" },
  forecastLow: { fontSize: 12, color: "rgba(255,255,255,0.4)", fontWeight: "600" },

  advisoryCard: {
    marginHorizontal: 16, borderRadius: 22,
    padding: 18, borderWidth: 1,
  },
  advisoryHeader: { flexDirection: "row", alignItems: "center", gap: 10, marginBottom: 10 },
  advisoryIcon: { fontSize: 24 },
  advisoryTitle: { fontSize: 15, fontWeight: "800" },
  advisoryText: { fontSize: 14, color: "rgba(255,255,255,0.85)", lineHeight: 22 },
});
