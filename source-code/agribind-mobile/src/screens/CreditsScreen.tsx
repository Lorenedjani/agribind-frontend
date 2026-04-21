import React, { useRef, useEffect } from "react";
import {
  View, Text, StyleSheet, ScrollView, Animated, Dimensions,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSelector } from "react-redux";
import { RootState } from "../store";
import { Header } from "../components/Header";
import { t } from "../utils/i18n";
import { THEMES } from "../theme";

const { width } = Dimensions.get("window");

const MOCK_CREDIT = {
  id: "MC-2026-0031",
  cooperative: "AgriCentre Yaoundé",
  purpose: "Achat semences & engrais — Maïs",
  total: 350000,
  repaid: 175000,
  currency: "XAF",
  startDate: "Jan 2026",
  endDate: "Déc 2026",
  status: "CURRENT" as const,
  repayments: [
    { id: "r1", dueDate: "Fév 2026", amount: 35000, status: "PAID" as const },
    { id: "r2", dueDate: "Mar 2026", amount: 35000, status: "PAID" as const },
    { id: "r3", dueDate: "Avr 2026", amount: 35000, status: "PAID" as const },
    { id: "r4", dueDate: "Mai 2026", amount: 35000, status: "PAID" as const },
    { id: "r5", dueDate: "Jun 2026", amount: 35000, status: "PAID" as const },
    { id: "r6", dueDate: "Jul 2026", amount: 35000, status: "CURRENT" as const },
    { id: "r7", dueDate: "Aoû 2026", amount: 35000, status: "CURRENT" as const },
    { id: "r8", dueDate: "Sep 2026", amount: 35000, status: "CURRENT" as const },
    { id: "r9", dueDate: "Oct 2026", amount: 35000, status: "CURRENT" as const },
    { id: "r10", dueDate: "Nov 2026", amount: 35000, status: "CURRENT" as const },
  ],
};

export const CreditsScreen = () => {
  const insets = useSafeAreaInsets();
  const lang = useSelector((state: RootState) => state.app.language);
  const themeMode = useSelector((state: RootState) => state.app.theme) || "dark";
  const C = THEMES[themeMode].colors;
  
  const progressAnim = useRef(new Animated.Value(0)).current;

  const pct = Math.round((MOCK_CREDIT.repaid / MOCK_CREDIT.total) * 100);
  const remaining = MOCK_CREDIT.total - MOCK_CREDIT.repaid;

  useEffect(() => {
    Animated.timing(progressAnim, { toValue: pct / 100, duration: 1200, useNativeDriver: false }).start();
  }, []);

  const barWidth = progressAnim.interpolate({ inputRange: [0, 1], outputRange: ["0%", "100%"] });

  const STATUS_META = {
    PAID: { label: "Payé", color: C.success, bg: `${C.success}26`, icon: "✅" },
    CURRENT: { label: "En cours", color: C.warning, bg: `${C.warning}1F`, icon: "⏳" },
    OVERDUE: { label: "Arriéré", color: C.danger, bg: `${C.danger}26`, icon: "❌" },
  };

  return (
    <View style={[styles.container, { backgroundColor: C.bg, paddingTop: insets.top }]}>
      <Header />
      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scroll}>

        <View style={styles.headerTitleRow}>
          <Text style={[styles.heroTitle, { color: C.textPrimary }]}>{t("credits_title", lang)}</Text>
          <Text style={[styles.heroLabel, { color: C.textSecondary }]}>🤝 {MOCK_CREDIT.cooperative}</Text>
        </View>

        {/* Big Progress Card */}
        <View style={[styles.progressCard, { backgroundColor: C.surface, borderColor: C.glassBorder }]}>
          <View style={styles.progressHeader}>
            <Text style={[styles.progressLabel, { color: C.textSecondary }]}>Remboursement</Text>
            <Text style={[styles.progressPct, { color: C.primaryLight }]}>{pct}%</Text>
          </View>
          <View style={[styles.progressTrack, { backgroundColor: C.glass }]}>
            <Animated.View style={[styles.progressFill, { backgroundColor: C.primary, width: barWidth }]} />
          </View>
          <View style={styles.progressFooter}>
            <Text style={[styles.progressDetail, { color: C.textMuted }]}>Payé: {MOCK_CREDIT.repaid.toLocaleString()} {MOCK_CREDIT.currency}</Text>
            <Text style={[styles.progressDetail, { color: C.textMuted }]}>Reste: {remaining.toLocaleString()} {MOCK_CREDIT.currency}</Text>
          </View>
        </View>

        {/* ── Quick Stats ── */}
        <View style={styles.quickStatsRow}>
          <View style={[styles.qsCard, { backgroundColor: C.surface, borderColor: C.glassBorder }]}>
            <Text style={styles.qsIcon}>💰</Text>
            <Text style={[styles.qsVal, { color: C.textPrimary }]}>{MOCK_CREDIT.total.toLocaleString()}</Text>
            <Text style={[styles.qsLbl, { color: C.textMuted }]}>Total XAF</Text>
          </View>
          <View style={[styles.qsCard, { backgroundColor: C.surface, borderColor: C.glassBorder }]}>
            <Text style={styles.qsIcon}>📅</Text>
            <Text style={[styles.qsVal, { color: C.textPrimary }]}>{MOCK_CREDIT.startDate}</Text>
            <Text style={[styles.qsLbl, { color: C.textMuted }]}>Début</Text>
          </View>
          <View style={[styles.qsCard, { backgroundColor: C.surface, borderColor: C.glassBorder }]}>
            <Text style={styles.qsIcon}>🏁</Text>
            <Text style={[styles.qsVal, { color: C.textPrimary }]}>{MOCK_CREDIT.endDate}</Text>
            <Text style={[styles.qsLbl, { color: C.textMuted }]}>Fin prévue</Text>
          </View>
        </View>

        {/* Purpose */}
        <View style={[styles.purposeCard, { backgroundColor: `${C.primary}1A`, borderColor: `${C.primary}33` }]}>
          <Text style={styles.purposeIcon}>🌱</Text>
          <View style={styles.purposeContent}>
            <Text style={[styles.purposeLabel, { color: C.primaryLight }]}>Objet du crédit</Text>
            <Text style={[styles.purposeText, { color: C.textPrimary }]}>{MOCK_CREDIT.purpose}</Text>
          </View>
        </View>

        {/* Repayment timeline */}
        <View style={styles.timelineSection}>
          <Text style={[styles.sectionTitle, { color: C.textSecondary }]}>Échéancier de remboursement</Text>
          {MOCK_CREDIT.repayments.map((r) => {
            const meta = STATUS_META[r.status];
            return (
              <View key={r.id} style={[styles.timelineRow, { backgroundColor: meta.bg, borderColor: "transparent" }]}>
                <Text style={styles.timelineIcon}>{meta.icon}</Text>
                <Text style={[styles.timelineDate, { color: C.textPrimary }]}>{r.dueDate}</Text>
                <Text style={[styles.timelineAmt, { color: C.textSecondary }]}>{r.amount.toLocaleString()} XAF</Text>
                <View style={[styles.statusBadge, { backgroundColor: meta.bg }]}>
                  <Text style={[styles.statusText, { color: meta.color }]}>{meta.label}</Text>
                </View>
              </View>
            );
          })}
        </View>
        <View style={{ height: 20 }} />
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  scroll: { paddingBottom: 40 },
  headerTitleRow: { paddingHorizontal: 20, paddingTop: 16, marginBottom: 16 },
  heroLabel: { fontSize: 13, fontWeight: "700", marginTop: 4 },
  heroTitle: { fontSize: 28, fontWeight: "900", letterSpacing: -0.5 },

  progressCard: {
    marginHorizontal: 16, borderRadius: 22, padding: 20,
    borderWidth: 1,
  },
  progressHeader: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 12 },
  progressLabel: { fontSize: 14, fontWeight: "700" },
  progressPct: { fontSize: 28, fontWeight: "900" },
  progressTrack: { height: 12, borderRadius: 6, overflow: "hidden", marginBottom: 10 },
  progressFill: { height: "100%", borderRadius: 6 },
  progressFooter: { flexDirection: "row", justifyContent: "space-between" },
  progressDetail: { fontSize: 12, fontWeight: "600" },

  quickStatsRow: { flexDirection: "row", paddingHorizontal: 16, gap: 10, marginTop: 16, marginBottom: 10 },
  qsCard: {
    flex: 1, borderRadius: 18, padding: 14,
    alignItems: "center", borderWidth: 1,
  },
  qsIcon: { fontSize: 22, marginBottom: 6 },
  qsVal: { fontSize: 14, fontWeight: "800", textAlign: "center" },
  qsLbl: { fontSize: 10, fontWeight: "700", textTransform: "uppercase", marginTop: 2 },

  purposeCard: {
    marginHorizontal: 16, borderRadius: 18,
    padding: 16, flexDirection: "row", alignItems: "center", gap: 14,
    borderWidth: 1, marginVertical: 10,
  },
  purposeIcon: { fontSize: 28 },
  purposeContent: { flex: 1 },
  purposeLabel: { fontSize: 11, fontWeight: "700", textTransform: "uppercase", letterSpacing: 0.5, marginBottom: 4 },
  purposeText: { fontSize: 14, fontWeight: "600", lineHeight: 20 },

  timelineSection: { paddingHorizontal: 16, marginTop: 10 },
  sectionTitle: { fontSize: 14, fontWeight: "800", textTransform: "uppercase", letterSpacing: 0.5, marginBottom: 12 },
  timelineRow: {
    flexDirection: "row", alignItems: "center", borderRadius: 14,
    padding: 14, marginBottom: 8, gap: 10,
  },
  timelineIcon: { fontSize: 18 },
  timelineDate: { flex: 1, fontSize: 14, fontWeight: "700" },
  timelineAmt: { fontSize: 13, fontWeight: "700" },
  statusBadge: { borderRadius: 8, paddingHorizontal: 8, paddingVertical: 4 },
  statusText: { fontSize: 11, fontWeight: "800" },
});
