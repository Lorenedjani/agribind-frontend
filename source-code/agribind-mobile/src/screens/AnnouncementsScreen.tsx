import React, { useRef, useEffect, useState, useMemo } from "react";
import {
  View, Text, StyleSheet, FlatList, TouchableOpacity,
  Animated, TextInput, ScrollView,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSelector, useDispatch } from "react-redux";
import { RootState } from "../store";
import { Announcement, AppLanguage } from "../types/models";
import { t } from "../utils/i18n";
import { playAudioFromUrl, stopCurrentAudio } from "../services/audioService";
import { Header } from "../components/Header";
import { THEMES } from "../theme";
import { fetchAnnouncements } from "../store/slices/announcementSlice";
import { useNetInfo } from "@react-native-community/netinfo";

// ─────────────────────────────────────────────────────────────────────────────
// Filter tabs
// ─────────────────────────────────────────────────────────────────────────────
type FilterType = "all" | "government" | "cooperative";

const FILTERS: { key: FilterType; label: string; emoji: string }[] = [
  { key: "all",         label: "Tous",           emoji: "📋" },
  { key: "government",  label: "Gouvernement",   emoji: "🏛️" },
  { key: "cooperative", label: "Coopérative",    emoji: "🤝" },
];

// ─────────────────────────────────────────────────────────────────────────────
// Component
// ─────────────────────────────────────────────────────────────────────────────
export const AnnouncementsScreen = () => {
  const insets   = useSafeAreaInsets();
  const dispatch = useDispatch<any>();
  const lang     = useSelector((state: RootState) => state.app.language);
  const themeMode = useSelector((state: RootState) => state.app.theme) || "dark";
  const { items: ALL_ANNOUNCEMENTS, loading, error } = useSelector(
    (state: RootState) => state.announcements
  );
  const C       = THEMES[themeMode].colors;
  const netInfo = useNetInfo();

  // ── Auto-fetch on mount and connectivity changes ──────────────────────────
  useEffect(() => {
    if (netInfo.isConnected) {
      dispatch(fetchAnnouncements());
    }
  }, [netInfo.isConnected, dispatch]);

  // ── Local state ───────────────────────────────────────────────────────────
  const [playing,     setPlaying]     = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [filter,      setFilter]      = useState<FilterType>("all");

  const pulseAnim = useRef(new Animated.Value(1)).current;

  // ── Filter + search ───────────────────────────────────────────────────────
  const filtered = useMemo(() => {
    let result = ALL_ANNOUNCEMENTS;

    // Source filter
    if (filter === "government") {
      result = result.filter((a) => a.isGovernment);
    } else if (filter === "cooperative") {
      result = result.filter((a) => !a.isGovernment);
    }

    // Text search
    if (searchQuery.trim().length > 0) {
      const q = searchQuery.trim().toLowerCase();
      result  = result.filter(
        (a) =>
          a.title.toLowerCase().includes(q) ||
          (a.source ?? "").toLowerCase().includes(q)
      );
    }

    return result;
  }, [searchQuery, filter, ALL_ANNOUNCEMENTS]);

  // Government announcement count badge
  const govCount = useMemo(
    () => ALL_ANNOUNCEMENTS.filter((a) => a.isGovernment).length,
    [ALL_ANNOUNCEMENTS]
  );

  // ── Pulse animation while playing ─────────────────────────────────────────
  useEffect(() => {
    if (playing) {
      Animated.loop(
        Animated.sequence([
          Animated.timing(pulseAnim, { toValue: 1.06, duration: 700, useNativeDriver: true }),
          Animated.timing(pulseAnim, { toValue: 1,    duration: 700, useNativeDriver: true }),
        ])
      ).start();
    } else {
      pulseAnim.stopAnimation();
      pulseAnim.setValue(1);
    }
  }, [playing]);

  // ── Play / stop ───────────────────────────────────────────────────────────
  const handlePlay = async (item: Announcement) => {
    if (playing === item.id) {
      stopCurrentAudio();
      setPlaying(null);
      return;
    }
    stopCurrentAudio();

    // audioUrl is normalized in announcementSlice via toAbsoluteApiUrl().
    // Keep support for cached file:// assets in offline mode.
    const resolvedUrl = (item.audioUrl || "").trim();
    if (!resolvedUrl) {
      console.warn("handlePlay: no audio URL for item", item.id);
      return;
    }
    if (
      !resolvedUrl.startsWith("file://") &&
      !resolvedUrl.startsWith("http://") &&
      !resolvedUrl.startsWith("https://")
    ) {
      console.warn("handlePlay: invalid audio URL format", resolvedUrl);
      return;
    }

    setPlaying(item.id);
    try {
      const player = await playAudioFromUrl(resolvedUrl);
      // Clear playing state when playback ends
      if (player) {
        const checkEnded = setInterval(() => {
          // expo-audio player signals completion via playing property
          try {
            // @ts-ignore
            if (!player.playing) {
              clearInterval(checkEnded);
              setPlaying((curr) => (curr === item.id ? null : curr));
            }
          } catch {
            clearInterval(checkEnded);
          }
        }, 500);
      }
    } catch (err) {
      console.error("Audio playback error:", err);
      setPlaying(null);
    }
    // Fallback reset after 30s
    setTimeout(() => setPlaying((curr) => (curr === item.id ? null : curr)), 30_000);
  };

  // ── Render item ───────────────────────────────────────────────────────────
  const renderItem = ({ item }: { item: Announcement }) => {
    const isPlaying = playing === item.id;
    const isGov     = item.isGovernment;

    return (
      <Animated.View
        style={[
          styles.card,
          { backgroundColor: C.surface, borderColor: C.glassBorder },
          isGov     && styles.cardGov,
          isPlaying && { borderColor: C.primary, backgroundColor: C.primaryGlow },
          isPlaying && { transform: [{ scale: pulseAnim }] },
        ]}
      >
        {/* Government badge strip */}
        {isGov && (
          <View style={[styles.govStrip, { backgroundColor: "#1a3a2a" }]}>
            <Text style={styles.govStripText}>🏛️ OFFICIEL — GOUVERNEMENT</Text>
          </View>
        )}

        <View style={styles.cardBody}>
          {/* Icon */}
          <View
            style={[
              styles.cardIcon,
              { backgroundColor: C.glass, borderColor: C.glassBorder },
              isGov     && { backgroundColor: "#1a3a2a", borderColor: "#2d6a4f" },
              isPlaying && { backgroundColor: C.actionBlue, borderColor: C.primary },
            ]}
          >
            <Text style={styles.cardIconEmoji}>{isGov ? "🏛️" : "📢"}</Text>
          </View>

          {/* Content */}
          <View style={styles.cardContent}>
            <Text
              style={[
                styles.cardTitle,
                { color: C.textPrimary },
                isPlaying && { color: C.primary },
              ]}
              numberOfLines={2}
            >
              {item.title}
            </Text>

            {/* Source label */}
            {item.source && (
              <Text style={[styles.cardSource, { color: isGov ? "#4ade80" : C.textMuted }]}>
                {isGov ? "🏛️ " : "🤝 "}{item.source}
              </Text>
            )}

            <View style={styles.cardMeta}>
              <Text style={[styles.metaTag, { color: C.textMuted }]}>
                🗣 {item.language.toUpperCase()}
              </Text>
              {item.downloaded && (
                <Text style={[styles.metaTag, { color: C.textMuted }]}>📥 Hors-ligne</Text>
              )}
            </View>
          </View>

          {/* Play button */}
          <TouchableOpacity
            style={[
              styles.playBtn,
              { backgroundColor: C.glass, borderColor: C.glassBorder },
              isPlaying && { backgroundColor: C.primary, borderColor: C.primary },
            ]}
            onPress={() => handlePlay(item)}
            activeOpacity={0.8}
          >
            <Text style={styles.playIcon}>{isPlaying ? "⏸" : "▶"}</Text>
          </TouchableOpacity>
        </View>
      </Animated.View>
    );
  };

  // ── Screen ────────────────────────────────────────────────────────────────
  return (
    <View style={[styles.container, { backgroundColor: C.bg, paddingTop: insets.top }]}>
      <Header />

      {/* ── Title ── */}
      <View style={styles.headerRow}>
        <View style={styles.headerTitleRow}>
          <Text style={[styles.headerTitle, { color: C.textPrimary }]}>
            {t("nav_announcements", lang)}
          </Text>
          {govCount > 0 && (
            <View style={styles.govBadge}>
              <Text style={styles.govBadgeText}>{govCount} officiel{govCount > 1 ? "s" : ""}</Text>
            </View>
          )}
        </View>
      </View>

      {/* ── Offline banner ── */}
      {netInfo.isConnected === false && (
        <View style={styles.offlineBanner}>
          <Text style={styles.offlineBannerText}>
            ⚠️ Mode Hors Ligne — Annonces téléchargées disponibles
          </Text>
        </View>
      )}

      {/* ── Search bar ── */}
      <View style={styles.searchRow}>
        <View style={[styles.searchBar, { backgroundColor: C.surface, borderColor: C.glassBorder }]}>
          <Text style={styles.searchIcon}>🔍</Text>
          <TextInput
            style={[styles.searchInput, { color: C.textPrimary }]}
            placeholder="Rechercher une annonce…"
            placeholderTextColor={C.textMuted}
            value={searchQuery}
            onChangeText={setSearchQuery}
          />
        </View>
      </View>

      {/* ── Filter pills ── */}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        style={styles.filterScroll}
        contentContainerStyle={styles.filterRow}
      >
        {FILTERS.map((f) => {
          const active = filter === f.key;
          return (
            <TouchableOpacity
              key={f.key}
              onPress={() => setFilter(f.key)}
              style={[
                styles.filterPill,
                {
                  backgroundColor: active ? C.primary : C.surface,
                  borderColor:     active ? C.primary : C.glassBorder,
                },
              ]}
            >
              <Text style={[styles.filterPillText, { color: active ? "#fff" : C.textMuted }]}>
                {f.emoji} {f.label}
              </Text>
            </TouchableOpacity>
          );
        })}
      </ScrollView>

      {/* ── List ── */}
      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <Text style={styles.emptyEmoji}>🎧</Text>
            <Text style={[styles.emptyText, { color: C.textMuted }]}>
              {loading ? "Chargement…" : "Aucun message trouvé."}
            </Text>
            {!!error && (
              <Text style={[styles.errorText, { color: "#ef4444" }]}>{error}</Text>
            )}
          </View>
        }
      />
    </View>
  );
};

// ─────────────────────────────────────────────────────────────────────────────
// Styles
// ─────────────────────────────────────────────────────────────────────────────
const styles = StyleSheet.create({
  container:      { flex: 1 },

  // Header
  headerRow:      { paddingHorizontal: 20, paddingVertical: 12 },
  headerTitleRow: { flexDirection: "row", alignItems: "center", gap: 10 },
  headerTitle:    { fontSize: 28, fontWeight: "900", letterSpacing: -0.5 },

  govBadge: {
    backgroundColor: "#1a3a2a",
    borderRadius: 12,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderWidth: 1,
    borderColor: "#2d6a4f",
  },
  govBadgeText: { fontSize: 11, fontWeight: "800", color: "#4ade80" },

  // Offline
  offlineBanner: {
    backgroundColor: "#FF9800",
    padding: 8,
    marginHorizontal: 16,
    borderRadius: 8,
    marginBottom: 8,
  },
  offlineBannerText: { color: "#fff", fontSize: 12, fontWeight: "700", textAlign: "center" },

  // Search
  searchRow:   { paddingHorizontal: 16, paddingBottom: 10 },
  searchBar: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 16,
    height: 48,
    borderRadius: 24,
    borderWidth: 1,
  },
  searchIcon:  { fontSize: 18, marginRight: 10 },
  searchInput: { flex: 1, fontSize: 15, fontWeight: "600" },

  // Filters
  filterScroll:       { paddingLeft: 16, marginBottom: 6 },
  filterRow:          { flexDirection: "row", gap: 8, paddingRight: 16 },
  filterPill: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
  },
  filterPillText: { fontSize: 13, fontWeight: "700" },

  // List
  list: { padding: 16, gap: 12, paddingBottom: 48 },

  // Card
  card: {
    borderRadius: 20,
    borderWidth: 1,
    overflow: "hidden",
  },
  cardGov: {
    borderColor: "#2d6a4f",
  },
  govStrip: {
    paddingVertical: 5,
    paddingHorizontal: 14,
  },
  govStripText: {
    fontSize: 10,
    fontWeight: "900",
    color: "#4ade80",
    letterSpacing: 1,
    textTransform: "uppercase",
  },
  cardBody: {
    flexDirection: "row",
    alignItems: "center",
    padding: 14,
    gap: 12,
  },
  cardIcon: {
    width: 50,
    height: 50,
    borderRadius: 13,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1,
  },
  cardIconEmoji: { fontSize: 24 },
  cardContent:   { flex: 1 },
  cardTitle: {
    fontSize: 15,
    fontWeight: "700",
    lineHeight: 21,
    marginBottom: 4,
  },
  cardSource: {
    fontSize: 11,
    fontWeight: "700",
    marginBottom: 4,
  },
  cardMeta:   { flexDirection: "row", gap: 10 },
  metaTag:    { fontSize: 11, fontWeight: "700" },

  playBtn: {
    width: 46,
    height: 46,
    borderRadius: 23,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 2,
  },
  playIcon: { fontSize: 18, color: "#fff", marginLeft: 3 },

  // Empty / error
  emptyState: { padding: 48, alignItems: "center" },
  emptyEmoji: { fontSize: 40, marginBottom: 12 },
  emptyText:  { fontSize: 16, textAlign: "center" },
  errorText:  { fontSize: 13, marginTop: 8, textAlign: "center" },
});