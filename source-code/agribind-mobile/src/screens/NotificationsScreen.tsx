import React from "react";
import {
  View, Text, StyleSheet, FlatList, TouchableOpacity,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../store";
import { fetchNotifications, markNotificationRead, markAllNotificationsRead } from "../store/slices/notificationsSlice";
import { AppNotification, NotificationCategory } from "../types/models";
import { useNetInfo } from "@react-native-community/netinfo";
import { Header } from "../components/Header";
import { THEMES } from "../theme";

const CATEGORY_STYLE_BASE: Record<NotificationCategory, { icon: string; color: string; route: string }> = {
  PRICE_ALERT:  { icon: "📈", color: "#a3d727", route: "Prices" },
  WEATHER:      { icon: "⛈️", color: "#FFB300", route: "Weather" },
  COOPERATIVE:  { icon: "🤝", color: "#66BB6A", route: "Announcements" },
  PAYMENT:      { icon: "💸", color: "#AB47BC", route: "Profile" },
  PLANT_HEALTH: { icon: "🐛", color: "#EF5350", route: "Home" },
  GENERAL:      { icon: "📢", color: "#78909C", route: "Announcements" },
};

export const NotificationsScreen = ({ navigation }: any) => {
  const insets = useSafeAreaInsets();
  const dispatch = useDispatch();
  const themeMode = useSelector((state: RootState) => state.app.theme) || "dark";
  const C = THEMES[themeMode].colors;
  
  const notifications = useSelector((state: RootState) => state.notifications.items);
  const unread = notifications.filter(n => !n.read).length;
  const netInfo = useNetInfo();

  React.useEffect(() => {
    if (netInfo.isConnected) {
        dispatch(fetchNotifications() as any);
    }
  }, [netInfo.isConnected, dispatch]);

  const handlePress = (item: AppNotification) => {
    dispatch(markNotificationRead(item.id) as any);
    const base = CATEGORY_STYLE_BASE[item.category] ?? CATEGORY_STYLE_BASE.GENERAL;
    navigation.navigate(base.route);
  };

  const renderItem = ({ item }: { item: AppNotification }) => {
    const base = CATEGORY_STYLE_BASE[item.category] ?? CATEGORY_STYLE_BASE.GENERAL;
    
    // Use theme-aware background for the icon box to make it look premium
    const bg = themeMode === "dark" ? "rgba(255,255,255,0.05)" : "rgba(0,0,0,0.03)";
    
    return (
      <TouchableOpacity
        style={[styles.card, { backgroundColor: C.surface, borderColor: C.glassBorder }, item.read && styles.cardRead]}
        onPress={() => handlePress(item)}
        activeOpacity={0.8}
      >
        <View style={[styles.iconBox, { backgroundColor: bg, borderColor: C.glassBorder, borderWidth: 1 }]}>
          <Text style={styles.iconEmoji}>{base.icon}</Text>
        </View>
        <View style={styles.content}>
          <Text style={[styles.title, { color: C.textPrimary }, item.read && { color: C.textSecondary }]}>{item.title}</Text>
          <Text style={[styles.body, { color: C.textMuted }]} numberOfLines={2}>{item.body}</Text>
          <Text style={[styles.timestamp, { color: C.textMuted }]}>
            {new Date(item.createdAt).toLocaleString("fr-CM", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" })}
          </Text>
        </View>
        {!item.read && <View style={[styles.unreadDot, { backgroundColor: base.color }]} />}
      </TouchableOpacity>
    );
  };

  return (
    <View style={[styles.container, { backgroundColor: C.bg, paddingTop: insets.top }]}>
      <Header />
      
      {/* Sub Header */}
      <View style={styles.headerRow}>
        <View style={styles.headerTop}>
          <TouchableOpacity style={[styles.backBtn, { backgroundColor: C.glass, borderColor: C.glassBorder }]} onPress={() => navigation.goBack()}>
            <Text style={[styles.backBtnText, { color: C.textPrimary }]}>←</Text>
          </TouchableOpacity>
          <Text style={[styles.headerTitle, { color: C.textPrimary }]}>Notifications</Text>
          {unread > 0 && (
            <TouchableOpacity onPress={() => dispatch(markAllNotificationsRead() as any)} style={[styles.markAllBtn, { backgroundColor: `${C.primary}26`, borderColor: `${C.primaryDark}4D` }]}>
              <Text style={[styles.markAllText, { color: C.primaryLight }]}>Tout lire</Text>
            </TouchableOpacity>
          )}
        </View>

        {netInfo.isConnected === false && (
          <View style={{ backgroundColor: '#FF9800', padding: 8, borderRadius: 8, marginBottom: 12 }}>
             <Text style={{ color: '#fff', fontSize: 12, fontWeight: '700', textAlign: 'center' }}>⚠️ Mode Hors Ligne - Historique local</Text>
          </View>
        )}

        {unread > 0 && (
          <View style={[styles.unreadBanner, { backgroundColor: C.primaryGlow, borderColor: C.primaryDark }]}>
            <Text style={[styles.unreadBannerText, { color: C.primary }]}>🔔  {unread} message{unread > 1 ? "s" : ""} non lu{unread > 1 ? "s" : ""}</Text>
          </View>
        )}
      </View>

      <FlatList
        data={notifications}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.emptyWrap}>
            <Text style={styles.emptyIcon}>📭</Text>
            <Text style={[styles.emptyText, { color: C.textMuted }]}>Aucune notification</Text>
          </View>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  headerRow: { paddingHorizontal: 20, paddingBottom: 12, paddingTop: 12 },
  headerTop: { flexDirection: "row", alignItems: "center", marginBottom: 12, gap: 12 },
  backBtn: {
    width: 36, height: 36, borderRadius: 18,
    alignItems: "center", justifyContent: "center", borderWidth: 1,
  },
  backBtnText: { fontSize: 18, fontWeight: "700" },
  headerTitle: { flex: 1, fontSize: 22, fontWeight: "900", letterSpacing: -0.5 },
  markAllBtn: { borderRadius: 10, paddingHorizontal: 12, paddingVertical: 6, borderWidth: 1 },
  markAllText: { fontSize: 12, fontWeight: "700" },
  unreadBanner: {
    borderRadius: 12, paddingHorizontal: 14, paddingVertical: 8,
    borderWidth: 1, alignSelf: "flex-start",
  },
  unreadBannerText: { fontSize: 13, fontWeight: "800" },

  list: { padding: 16, gap: 10, paddingBottom: 40 },
  card: {
    flexDirection: "row", alignItems: "center",
    borderRadius: 18, padding: 14, borderWidth: 1, gap: 14,
  },
  cardRead: { opacity: 0.6 },
  iconBox: { width: 48, height: 48, borderRadius: 14, alignItems: "center", justifyContent: "center" },
  iconEmoji: { fontSize: 24, marginTop: -2 },
  content: { flex: 1 },
  title: { fontSize: 14, fontWeight: "800", marginBottom: 4 },
  body: { fontSize: 12, lineHeight: 18, marginBottom: 6 },
  timestamp: { fontSize: 10, fontWeight: "600" },
  unreadDot: { width: 10, height: 10, borderRadius: 5 },

  emptyWrap: { alignItems: "center", marginTop: 80 },
  emptyIcon: { fontSize: 48, marginBottom: 12 },
  emptyText: { fontSize: 16, fontWeight: "600" },
});
