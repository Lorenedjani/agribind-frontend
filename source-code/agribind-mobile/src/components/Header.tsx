import React from "react";
import {
  View, Text, StyleSheet, TouchableOpacity, Image,
} from "react-native";
import { useSelector } from "react-redux";
import { RootState } from "../store";
import { THEMES } from "../theme";
import { useNavigation } from "@react-navigation/native";

type Props = { transparent?: boolean };

export const Header = ({ transparent }: Props = {}) => {
  const themeMode = useSelector((state: RootState) => state.app.theme) || "dark";
  const isOnline = useSelector((state: RootState) => state.app.isOnline);
  const profile = useSelector((state: RootState) => state.auth.profile);
  const C = THEMES[themeMode].colors;
  const navigation = useNavigation<any>();

  // Use profile from auth, fallback to mockup data
  const userName = profile?.name || "Jean Dupont";
  const memberId = profile?.id || "CM-2024-0123";

  return (
    <View style={[styles.container, { backgroundColor: transparent ? "transparent" : C.bg }]}>
      {/* ── User & Notification Info ── */}
      <View style={styles.topRow}>
        <View style={styles.userInfo}>
          <View style={[styles.avatar, { backgroundColor: C.primaryLight }]}>
            <Text style={styles.avatarText}>{userName.substring(0, 2).toUpperCase()}</Text>
          </View>
          <View>
            <Text style={[styles.userName, { color: C.textPrimary }]}>{userName}</Text>
            <Text style={[styles.userId, { color: C.textMuted }]}>{memberId}</Text>
          </View>
        </View>

        <View style={styles.actions}>
          <TouchableOpacity 
            style={[styles.iconBtn, { backgroundColor: C.glass }]} 
            onPress={() => navigation.navigate("Notifications")}
            activeOpacity={0.7}
          >
            <Text style={styles.iconText}>🔔</Text>
            {/* Notification badge */}
            <View style={[styles.badge, { backgroundColor: C.danger, borderColor: C.bg }]}>
              <Text style={styles.badgeText}>3</Text>
            </View>
          </TouchableOpacity>
        </View>
      </View>

      {/* ── System Status (Offline / Online) ── */}
      {!isOnline ? (
        <View style={[styles.statusBanner, { backgroundColor: C.amberBg, borderColor: C.warning }]}>
          <View style={styles.statusLeft}>
            <Text style={[styles.statusIcon, { color: C.warning }]}>⚡</Text>
            <Text style={[styles.statusLabel, { color: C.warning }]}>Offline</Text>
          </View>
          <Text style={[styles.statusTime, { color: C.warning }]}>Last sync: 2 mins ago</Text>
        </View>
      ) : (
        <View style={[styles.statusBanner, { backgroundColor: "rgba(76,175,80,0.1)", borderColor: C.success }]}>
          <View style={styles.statusLeft}>
            <Text style={[styles.statusIcon, { color: C.success }]}>●</Text>
            <Text style={[styles.statusLabel, { color: C.success }]}>System Online</Text>
          </View>
          <Text style={[styles.statusTime, { color: C.success }]}>Synchronized</Text>
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 20,
    paddingTop: 12,
    paddingBottom: 8,
  },
  topRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 16,
  },
  userInfo: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: 14,
    alignItems: "center",
    justifyContent: "center",
  },
  avatarText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "800",
  },
  userName: {
    fontSize: 18,
    fontWeight: "900",
  },
  userId: {
    fontSize: 12,
    fontWeight: "600",
  },
  actions: {
    flexDirection: "row",
    gap: 10,
  },
  iconBtn: {
    width: 42,
    height: 42,
    borderRadius: 21,
    alignItems: "center",
    justifyContent: "center",
    position: "relative",
  },
  iconText: {
    fontSize: 18,
  },
  badge: {
    position: "absolute",
    top: -2,
    right: -2,
    width: 18,
    height: 18,
    borderRadius: 9,
    borderWidth: 2,
    borderColor: "#0D1117",
    alignItems: "center",
    justifyContent: "center",
  },
  badgeText: {
    color: "#fff",
    fontSize: 9,
    fontWeight: "900",
  },
  statusBanner: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 12,
    borderWidth: 1,
  },
  statusLeft: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  statusIcon: {
    fontSize: 12,
  },
  statusLabel: {
    fontSize: 12,
    fontWeight: "800",
    textTransform: "uppercase",
    letterSpacing: 0.5,
  },
  statusTime: {
    fontSize: 11,
    fontWeight: "600",
    opacity: 0.8,
  },
});
