import React, { useState, useMemo } from "react";
import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity, Dimensions, TextInput,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSelector, useDispatch } from "react-redux";
import { RootState } from "../store";
import { MarketPrice } from "../types/models";
import { Header } from "../components/Header";
import { t } from "../utils/i18n";
import { THEMES } from "../theme";
import { fetchMarketPrices } from "../store/slices/marketSlice";
import { useNetInfo } from "@react-native-community/netinfo";

const { width } = Dimensions.get("window");

const COMMODITY_ICONS: Record<string, string> = {
  Maize: "🌽", Cocoa: "🍫", Coffee: "☕", Millet: "🌾",
  Cassava: "🥔", Plantain: "🍌", Palm: "🌴", Cotton: "🧶", Groundnut: "🥜",
};

const COMMODITY_FR: Record<string, string> = {
  Maize: "Maïs", Cocoa: "Cacao", Coffee: "Café", Millet: "Mil",
  Cassava: "Manioc", Plantain: "Plantain", Palm: "Huile Palme", Cotton: "Coton", Groundnut: "Arachide",
};

const CATEGORIES = ["Tous", "Céréales", "Cultures", "Fruits", "Oléagineux"];

const TREND_META: Record<string, { color: string; bg: string; arrow: string }> = {
  up:     { color: "#4CAF50", bg: "rgba(76,175,80,0.15)",    arrow: "↑" },
  down:   { color: "#EF5350", bg: "rgba(239,83,80,0.15)",    arrow: "↓" },
  stable: { color: "#FFB300", bg: "rgba(255,179,0,0.15)",    arrow: "→" },
};

export const MarketPricesScreen = () => {
  const insets = useSafeAreaInsets();
  const dispatch = useDispatch<any>();
  const lang = useSelector((state: RootState) => state.app.language);
  const themeMode = useSelector((state: RootState) => state.app.theme) || "dark";
  const { items: ALL_PRICES, loading, lastSync } = useSelector((state: RootState) => state.market);
  const C = THEMES[themeMode].colors;
  const netInfo = useNetInfo();
  
  React.useEffect(() => {
     if (netInfo.isConnected) {
         dispatch(fetchMarketPrices());
     }
  }, [netInfo.isConnected, dispatch]);

  const [activeCategory, setActiveCategory] = useState("Tous");
  const [searchQuery, setSearchQuery] = useState("");

  const getCategory = (commodity: string) => {
     if (["Maize", "Millet"].includes(commodity)) return "Céréales";
     if (["Plantain", "Banana"].includes(commodity)) return "Fruits";
     if (["Palm", "Groundnut"].includes(commodity)) return "Oléagineux";
     return "Cultures";
  }

  const filtered = useMemo(() => {
    return ALL_PRICES.filter((p: any) => {
      const matchesCat = activeCategory === "Tous" || getCategory(p.commodity) === activeCategory;
      const matchesSearch = p.commodity.toLowerCase().includes(searchQuery.toLowerCase()) || 
                            p.market.toLowerCase().includes(searchQuery.toLowerCase());
      return matchesCat && matchesSearch;
    });
  }, [activeCategory, searchQuery, ALL_PRICES]);

  return (
    <View style={[styles.container, { backgroundColor: C.bg, paddingTop: insets.top }]}>
      <Header />
      <ScrollView showsVerticalScrollIndicator={false} stickyHeaderIndices={[2]}>
        
        <View style={styles.headerTitleRow}>
          <Text style={[styles.heroTitle, { color: C.textPrimary }]}>{t("market_title", lang)}</Text>
        </View>

        {netInfo.isConnected === false && (
          <View style={{ backgroundColor: '#FF9800', padding: 8, marginHorizontal: 16, borderRadius: 8, marginBottom: 12 }}>
             <Text style={{ color: '#fff', fontSize: 12, fontWeight: '700', textAlign: 'center' }}>⚠️ Mode Hors Ligne - Prix en cache de la dernière synchro</Text>
          </View>
        )}

        {/* ── Search Bar ── */}
        <View style={styles.searchRow}>
          <View style={[styles.searchBar, { backgroundColor: C.surface, borderColor: C.glassBorder }]}>
            <Text style={styles.searchIcon}>🔍</Text>
            <TextInput 
              style={[styles.searchInput, { color: C.textPrimary }]}
              placeholder="Rechercher un produit ou marché..."
              placeholderTextColor={C.textMuted}
              value={searchQuery}
              onChangeText={setSearchQuery}
            />
          </View>
        </View>

        {/* ── Category Filter (sticky) ── */}
        <View style={[styles.filterBar, { backgroundColor: C.bg, borderBottomColor: C.glassBorder }]}>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.filterScroll}>
            {CATEGORIES.map((cat) => (
              <TouchableOpacity
                key={cat}
                onPress={() => setActiveCategory(cat)}
                style={[
                  styles.filterPill, 
                  { backgroundColor: C.glass, borderColor: C.glassBorder },
                  activeCategory === cat && { backgroundColor: C.primary, borderColor: C.primary }
                ]}
                activeOpacity={0.8}
              >
                <Text style={[styles.filterText, { color: C.textSecondary }, activeCategory === cat && { color: "#fff" }]}>{cat}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>

        {/* ── Price Grid ── */}
        <View style={styles.grid}>
          {filtered.map((item: any) => {
            const trend = item.trend ?? "stable"; // Uses DB trend if we add it, defaults stable
            const tm = TREND_META[trend as keyof typeof TREND_META] || TREND_META.stable;
            const fr = COMMODITY_FR[item.commodity] ?? item.commodity;
            const icon = COMMODITY_ICONS[item.commodity] ?? "📦";

            return (
              <View key={item.id} style={[styles.card, { backgroundColor: C.surface, borderColor: C.glassBorder }]}>
                {/* Icon area */}
                <View style={[styles.cardIconWrap, { backgroundColor: C.glass, borderColor: C.glassBorder }]}>
                  <Text style={styles.cardIcon}>{icon}</Text>
                </View>

                {/* Name & market */}
                <Text style={[styles.cardName, { color: C.textPrimary }]}>{fr}</Text>
                <Text style={[styles.cardMarket, { color: C.textMuted }]}>📍 {item.market}</Text>

                {/* Price */}
                <Text style={[styles.cardPrice, { color: C.primaryLight }]}>{item.price.toLocaleString()}</Text>
                <Text style={[styles.cardCurrency, { color: C.textMuted }]}>{item.currency}/kg</Text>

                {/* Trend badge */}
                <View style={[styles.trendBadge, { backgroundColor: tm.bg }]}>
                  <Text style={[styles.trendText, { color: tm.color }]}>{tm.arrow} {trend === "up" ? "Hausse" : trend === "down" ? "Baisse" : "Stable"}</Text>
                </View>

              </View>
            );
          })}
          
          {filtered.length === 0 && (
             <View style={{ padding: 40, width: "100%", alignItems: "center" }}>
               <Text style={{ fontSize: 40, marginBottom: 12 }}>🕵️‍♀️</Text>
               <Text style={{ color: C.textMuted, fontSize: 16 }}>Aucun produit trouvé</Text>
             </View>
          )}
        </View>
        <View style={{ height: 20 }} />
      </ScrollView>
    </View>
  );
};

const CARD_W = (width - 48) / 2;

const styles = StyleSheet.create({
  container: { flex: 1 },
  headerTitleRow: { paddingHorizontal: 20, paddingTop: 16, marginBottom: 8 },
  heroTitle: { fontSize: 28, fontWeight: "900", letterSpacing: -0.5 },

  // Search
  searchRow: { paddingHorizontal: 16, paddingBottom: 12 },
  searchBar: { flexDirection: "row", alignItems: "center", paddingHorizontal: 16, height: 50, borderRadius: 25, borderWidth: 1 },
  searchIcon: { fontSize: 18, marginRight: 10 },
  searchInput: { flex: 1, fontSize: 15, fontWeight: "600" },

  // Filter
  filterBar: { paddingVertical: 12, borderBottomWidth: 1 },
  filterScroll: { paddingHorizontal: 16, gap: 8 },
  filterPill: {
    paddingHorizontal: 18, paddingVertical: 8, borderRadius: 20,
    borderWidth: 1,
  },
  filterText: { fontSize: 13, fontWeight: "700" },

  // Grid cards
  grid: { flexDirection: "row", flexWrap: "wrap", padding: 16, gap: 14 },
  card: {
    width: CARD_W, borderRadius: 22,
    padding: 16, borderWidth: 1,
    shadowColor: "#000", shadowOpacity: 0.1, shadowRadius: 10, elevation: 4,
  },
  cardIconWrap: {
    width: 52, height: 52, borderRadius: 14,
    alignItems: "center", justifyContent: "center", marginBottom: 10,
    borderWidth: 1,
  },
  cardIcon: { fontSize: 28 },
  cardName: { fontSize: 16, fontWeight: "800", marginBottom: 2 },
  cardMarket: { fontSize: 12, marginBottom: 10 },
  cardPrice: { fontSize: 22, fontWeight: "900", letterSpacing: -0.5 },
  cardCurrency: { fontSize: 11, fontWeight: "700", marginBottom: 10 },
  trendBadge: { borderRadius: 8, paddingHorizontal: 8, paddingVertical: 4, alignSelf: "flex-start", marginBottom: 10 },
  trendText: { fontSize: 11, fontWeight: "800" },
});