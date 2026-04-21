import React, { useRef, useEffect } from "react";
import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity, Animated, Dimensions, ImageBackground,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSelector } from "react-redux";
import { RootState } from "../store";
import { THEMES } from "../theme";
import { Header } from "../components/Header";
import { StatusBar } from "expo-status-bar";

const { width, height } = Dimensions.get("window");

// Mockup backgrounds matching the UI vibe
const BG_URL = "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?q=80&w=1000";

const QUICK_ACTIONS = [
  { id: "market",   label: "Prix",   icon: "📈", color: "blue" },
  { id: "weather",  label: "Météo",  icon: "☁️", color: "purple" },
  { id: "training", label: "Guide",  icon: "📖", color: "orange" },
  { id: "health",   label: "Profil", icon: "👤", color: "pink" },
  { id: "more",     label: "Plus",   icon: "➕", color: "gray" },
];

const RECENT_UPDATES = [
  { id: "price_1", type: "price", title: "Cacao — Alerte Prix", body: "Hausse de 12% - 2,500 XAF/kg à Yaoundé", time: "16:40", trend: "+12%", icon: "💹", target: "Prices" },
  { id: "weather_1", type: "weather", title: "Alerte Météo", body: "Fortes pluies prévues dans votre région demain matin.", time: "14:15", trend: "⚠️", icon: "🌧", target: "Weather" },
  { id: "voice_1", type: "voice", title: "Nouveau message vocal", body: "Baisse des prix des engrais ce mois par la coopérative.", time: "Hier", trend: "📢", icon: "🎧", target: "Announcements" },
];

export const HomeScreen = ({ navigation }: any) => {
  const insets = useSafeAreaInsets();
  const themeMode = useSelector((state: RootState) => state.app.theme) || "dark";
  const C = THEMES[themeMode].colors;
  
  const balanceAnim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.spring(balanceAnim, {
      toValue: 1,
      friction: 8,
      useNativeDriver: true,
    }).start();
  }, []);

  const handleAction = (id: string) => {
    if (id === "market") navigation.navigate("Prices");
    else if (id === "weather") navigation.navigate("Weather");
    else if (id === "health") navigation.navigate("Profile"); 
    else if (id === "training") navigation.navigate("Announcements");
    else navigation.navigate("Announcements");
  };

  const handleNotificationClick = (target: string) => {
    navigation.navigate(target);
  };

  return (
    <View style={styles.container}>
      <StatusBar style="light" />
      
      {/* Top Immersive Area */}
      <ImageBackground source={{ uri: BG_URL }} style={[styles.topBg, { height: height * 0.45 }]} resizeMode="cover">
        
        {/* Dark overlay for beautiful legibility */}
        <View style={[StyleSheet.absoluteFillObject, { backgroundColor: "rgba(10, 20, 15, 0.4)" }]} />
        
        <View style={{ paddingTop: insets.top }}>
          {/* Header blending with background */}
          <Header transparent={true} />
        </View>

        {/* ── Dashboard Glass Card (replacing old balance layout with identical logic but new UI) ── */}
        <Animated.View style={[
          styles.glassCard, 
          { 
            backgroundColor: "rgba(20, 41, 26, 0.7)", 
            borderColor: "rgba(255, 255, 255, 0.15)",
            opacity: balanceAnim,
            transform: [{ scale: balanceAnim.interpolate({ inputRange: [0, 1], outputRange: [0.95, 1] }) }]
          }
        ]}>
          <Text style={styles.cardGreeting}>Welcome to</Text>
          <Text style={styles.cardTitle}>AgriBind AI</Text>
          
          <View style={styles.statsRow}>
            <View style={[styles.statBox, { backgroundColor: "rgba(0,0,0,0.2)", borderColor: "rgba(255,255,255,0.05)" }]}>
              <Text style={styles.statLabel}>Solde Actuel</Text>
              <Text style={styles.statValue}>450<Text style={styles.statUnit}>K XAF</Text></Text>
            </View>
            <View style={[styles.statBox, { backgroundColor: "rgba(0,0,0,0.2)", borderColor: "rgba(255,255,255,0.05)" }]}>
              <Text style={styles.statLabel}>Rendement</Text>
              <Text style={styles.statValue}>12<Text style={styles.statUnit}> Tons</Text></Text>
            </View>
            <View style={[styles.statBox, { backgroundColor: "rgba(0,0,0,0.2)", borderColor: "rgba(255,255,255,0.05)" }]}>
              <Text style={styles.statLabel}>État</Text>
              <Text style={styles.statValue}><Text style={styles.statUnit}>Opérationnel</Text></Text>
            </View>
          </View>

          {/* Money Actions strictly inline to match layout density */}
          <View style={styles.balanceActions}>
            <TouchableOpacity style={styles.actionBtnDark} onPress={() => navigation.navigate("Prices")}>
              <Text style={styles.actionBtnDarkText}>Comparer Prix</Text>
              <Text style={styles.actionBtnDarkArrow}>↗</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.actionBtnLight, { backgroundColor: C.primary }]} onPress={() => navigation.navigate("Announcements")}>
              <Text style={styles.actionBtnLightText}>Nouveautés</Text>
            </TouchableOpacity>
          </View>
        </Animated.View>

      </ImageBackground>

      {/* Bottom Sheet Area */}
      <View style={[styles.bottomSheet, { backgroundColor: C.bg }]}>
        <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scroll}>
          
          {/* ── Quick Actions ── */}
          <View style={styles.sectionHeader}>
            <Text style={[styles.sectionTitle, { color: C.textPrimary }]}>Actions Rapides</Text>
            <TouchableOpacity onPress={() => navigation.navigate("Profile")}>
              <Text style={[styles.seeAll, { color: C.textMuted }]}>Gérer ›</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.actionsGrid}>
            {QUICK_ACTIONS.map((action) => (
              <TouchableOpacity 
                key={action.id} 
                style={styles.actionItem}
                onPress={() => handleAction(action.id)}
                activeOpacity={0.7}
              >
                <View style={[styles.actionIconContainer, { backgroundColor: C.surface, borderColor: C.glassBorder }]}>
                  <Text style={styles.actionIcon}>{action.icon}</Text>
                </View>
                <Text style={[styles.actionLabel, { color: C.textSecondary }]}>{action.label}</Text>
              </TouchableOpacity>
            ))}
          </View>

          {/* ── Recent Updates (Interactive Redirection) ── */}
          <View style={styles.sectionHeader}>
            <Text style={[styles.sectionTitle, { color: C.textPrimary }]}>Récentes Alertes</Text>
            <TouchableOpacity onPress={() => navigation.navigate("Notifications")}>
              <Text style={[styles.seeAll, { color: C.primaryLight }]}>Tout voir ›</Text>
            </TouchableOpacity>
          </View>

          {RECENT_UPDATES.map((update) => (
            <TouchableOpacity 
              key={update.id} 
              style={[styles.updateCard, { backgroundColor: C.surface, borderColor: C.glassBorder }]}
              onPress={() => handleNotificationClick(update.target)}
              activeOpacity={0.7}
            >
              <View style={[styles.updateIconWrap, { backgroundColor: C.bgMid }]}>
                <Text style={styles.updateIcon}>{update.icon}</Text>
              </View>
              <View style={styles.updateInfo}>
                <Text style={[styles.updateTitle, { color: C.textPrimary }]}>{update.title}</Text>
                <Text style={[styles.updateBody, { color: C.textMuted }]} numberOfLines={2}>{update.body}</Text>
              </View>
              <View style={styles.updateRight}>
                <Text style={[styles.updateTrend, { color: update.type === "price" ? C.success : update.type === "weather" ? C.warning : C.info }]}>
                  {update.trend}
                </Text>
                <Text style={[styles.updateTime, { color: C.textMuted }]}>{update.time}</Text>
              </View>
            </TouchableOpacity>
          ))}

          <View style={{ height: 40 }} />
        </ScrollView>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#000" },
  topBg: { width: "100%", justifyContent: "flex-start" },
  
  glassCard: {
    marginHorizontal: 20,
    marginTop: 10,
    padding: 24,
    borderRadius: 28,
    borderWidth: 1,
    overflow: "hidden",
  },
  cardGreeting: { fontSize: 16, color: "rgba(255,255,255,0.8)", fontWeight: "600", marginBottom: 2 },
  cardTitle: { fontSize: 28, fontWeight: "900", color: "#fff", letterSpacing: -0.5, marginBottom: 20 },
  
  statsRow: { flexDirection: "row", justifyContent: "space-between", gap: 8, marginBottom: 20 },
  statBox: {
    flex: 1,
    borderRadius: 16,
    padding: 12,
    borderWidth: 1,
  },
  statLabel: { fontSize: 11, color: "rgba(255,255,255,0.6)", fontWeight: "700", marginBottom: 6 },
  statValue: { fontSize: 16, color: "#fff", fontWeight: "900" },
  statUnit: { fontSize: 11, fontWeight: "500", color: "rgba(255,255,255,0.6)" },

  balanceActions: { flexDirection: "row", gap: 12 },
  actionBtnDark: {
    flex: 1, flexDirection: "row", backgroundColor: "rgba(0,0,0,0.5)",
    paddingVertical: 14, borderRadius: 16, alignItems: "center", justifyContent: "center", gap: 6,
  },
  actionBtnDarkText: { fontSize: 14, color: "#fff", fontWeight: "700" },
  actionBtnDarkArrow: { fontSize: 16, color: "#fff", fontWeight: "900", marginTop: -2 },
  
  actionBtnLight: {
    flex: 1, paddingVertical: 14, borderRadius: 16, alignItems: "center", justifyContent: "center",
  },
  actionBtnLightText: { fontSize: 14, color: "#fff", fontWeight: "800" },

  bottomSheet: {
    flex: 1,
    marginTop: -24,
    borderTopLeftRadius: 32,
    borderTopRightRadius: 32,
    overflow: "hidden",
  },
  scroll: { paddingHorizontal: 20, paddingTop: 30 },
  
  sectionHeader: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 16 },
  sectionTitle: { fontSize: 18, fontWeight: "900" },
  seeAll: { fontSize: 14, fontWeight: "700" },

  actionsGrid: { flexDirection: "row", flexWrap: "wrap", justifyContent: "space-between", marginBottom: 32, rowGap: 16 },
  actionItem: { width: (width - 40) / 4.4, alignItems: "center", gap: 8 },
  actionIconContainer: { width: 54, height: 54, borderRadius: 16, borderWidth: 1, alignItems: "center", justifyContent: "center" },
  actionIcon: { fontSize: 24 },
  actionLabel: { fontSize: 11, fontWeight: "700", textTransform: "uppercase", letterSpacing: 0.5, textAlign: "center" },

  updateCard: {
    flexDirection: "row", alignItems: "center", padding: 16, borderRadius: 20, borderWidth: 1, marginBottom: 12,
  },
  updateIconWrap: { width: 48, height: 48, borderRadius: 14, alignItems: "center", justifyContent: "center" },
  updateIcon: { fontSize: 22 },
  updateInfo: { flex: 1, marginLeft: 16, marginRight: 8 },
  updateTitle: { fontSize: 15, fontWeight: "800", marginBottom: 4 },
  updateBody: { fontSize: 12, fontWeight: "600", lineHeight: 16 },
  updateRight: { alignItems: "flex-end", gap: 4, width: 60 },
  updateTrend: { fontSize: 13, fontWeight: "900" },
  updateTime: { fontSize: 11, fontWeight: "600" },
});
