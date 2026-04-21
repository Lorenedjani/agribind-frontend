import React, { useState } from "react";
import {
  View, Text, StyleSheet, ScrollView, TouchableOpacity, Alert, TextInput,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../store";
import { clearSession, updateFarmerProfile } from "../store/slices/authSlice";
import { setLanguage, setTheme } from "../store/slices/appSlice";
import { AppLanguage, FarmerProfile } from "../types/models";
import { Header } from "../components/Header";
import { THEMES } from "../theme";
import { ActivityIndicator } from "react-native";

const LANGUAGES: { code: AppLanguage; label: string; flag: string; native: string }[] = [
  { code: "fr", label: "Français", flag: "🇫🇷", native: "Bonjour" },
  { code: "en", label: "English", flag: "🇬🇧", native: "Hello" },
  { code: "ful", label: "Fulfulde", flag: "🌍", native: "Jam waali" },
  { code: "dua", label: "Duala", flag: "🌿", native: "Mbolo" },
];

export const ProfileScreen = () => {
  const insets = useSafeAreaInsets();
  const dispatch = useDispatch<any>();
  const lang = useSelector((state: RootState) => state.app.language);
  const themeMode = useSelector((state: RootState) => state.app.theme) || "dark";
  const { profile, loading } = useSelector((state: RootState) => state.auth);
  const C = THEMES[themeMode].colors;

  // Local state for editing profile
  const [isEditing, setIsEditing] = useState(false);
  const [name, setName] = useState(profile?.name || "");
  const [phone, setPhone] = useState(profile?.phoneNumber || "");
  const [coop, setCoop] = useState(profile?.cooperativeId || "");

  React.useEffect(() => {
    if (profile) {
      setName(profile.name);
      setPhone(profile.phoneNumber);
      setCoop(profile.cooperativeId || "");
    }
  }, [profile]);

  const handleLogout = () => {
    Alert.alert("Déconnexion", "Voulez-vous vous déconnecter?", [
      { text: "Annuler", style: "cancel" },
      { text: "Déconnexion", style: "destructive", onPress: () => {
          dispatch(clearSession());
        }
      }
    ]);
  };

  const handleSave = async () => {
    if (!profile?.id) return;
    try {
      await dispatch(updateFarmerProfile({ 
        userId: profile.id, 
        profile: { name, phoneNumber: phone, cooperativeId: coop } 
      })).unwrap();
      setIsEditing(false);
      Alert.alert("Succès", "Profil mis à jour avec succès.");
    } catch (e: any) {
      Alert.alert("Erreur", e || "Échec de la mise à jour");
    }
  };

  return (
    <View style={[styles.container, { backgroundColor: C.bg, paddingTop: insets.top }]}>
      <Header />
      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scroll}>

        {/* ── Edit Profile Section ── */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={[styles.sectionTitle, { color: C.textMuted }]}>Informations Personnelles</Text>
            <TouchableOpacity onPress={() => isEditing ? handleSave() : setIsEditing(true)} disabled={loading}>
              {loading ? (
                <ActivityIndicator size="small" color={C.primary} />
              ) : (
                <Text style={[styles.editBtnText, { color: C.primary }]}>{isEditing ? "Enregistrer" : "Modifier"}</Text>
              )}
            </TouchableOpacity>
          </View>
          
          <View style={[styles.menuCard, { backgroundColor: C.surface, borderColor: C.glassBorder, padding: 16, gap: 12 }]}>
            <View>
              <Text style={[styles.inputLabel, { color: C.textMuted }]}>Nom d'utilisateur</Text>
              {isEditing ? (
                <TextInput 
                  style={[styles.input, { color: C.textPrimary, borderColor: C.primary, backgroundColor: C.bgMid }]} 
                  value={name} onChangeText={setName} 
                />
              ) : (
                <Text style={[styles.inputValue, { color: C.textPrimary }]}>{name}</Text>
              )}
            </View>
            
            <View>
              <Text style={[styles.inputLabel, { color: C.textMuted }]}>Numéro de téléphone</Text>
              {isEditing ? (
                <TextInput 
                  style={[styles.input, { color: C.textPrimary, borderColor: C.primary, backgroundColor: C.bgMid }]} 
                  value={phone} onChangeText={setPhone} keyboardType="phone-pad"
                />
              ) : (
                <Text style={[styles.inputValue, { color: C.textPrimary }]}>{phone}</Text>
              )}
            </View>

            <View>
              <Text style={[styles.inputLabel, { color: C.textMuted }]}>Coopérative Assignée</Text>
              {isEditing ? (
                <TextInput 
                  style={[styles.input, { color: C.textPrimary, borderColor: C.primary, backgroundColor: C.bgMid }]} 
                  value={coop} onChangeText={setCoop} 
                />
              ) : (
                <Text style={[styles.inputValue, { color: C.textPrimary }]}>{coop}</Text>
              )}
            </View>
          </View>
        </View>

        {/* ── Display Settings (Theme Toggle) ── */}
        <View style={styles.section}>
          <Text style={[styles.sectionTitle, { color: C.textMuted }]}>Affichage / Display</Text>
          <View style={[styles.menuCard, { backgroundColor: C.surface, borderColor: C.glassBorder }]}>
            <TouchableOpacity 
              style={styles.menuRow}
              onPress={() => dispatch(setTheme(themeMode === "dark" ? "light" : "dark"))}
              activeOpacity={0.7}
            >
              <Text style={styles.menuIcon}>{themeMode === "dark" ? "🌙" : "☀️"}</Text>
              <Text style={[styles.menuLabel, { color: C.textPrimary }]}>
                Mode {themeMode === "dark" ? "Sombre" : "Clair"}
              </Text>
              <View style={[styles.toggleBackground, { backgroundColor: themeMode === "dark" ? C.primary : C.glass }]}>
                <View style={[styles.toggleThumb, { 
                  backgroundColor: "#fff", 
                  transform: [{ translateX: themeMode === "dark" ? 20 : 0 }] 
                }]} />
              </View>
            </TouchableOpacity>
          </View>
        </View>

        {/* ── Audio Language Selection ── */}
        <View style={styles.section}>
          <Text style={[styles.sectionTitle, { color: C.textMuted }]}>Annonces Vocales / Audio</Text>
          <View style={styles.langGrid}>
            {LANGUAGES.map((l) => (
              <TouchableOpacity
                key={l.code}
                style={[
                  styles.langCard, 
                  { backgroundColor: C.surface, borderColor: C.glassBorder },
                  lang === l.code && { borderColor: C.primary, backgroundColor: C.primaryGlow }
                ]}
                onPress={() => dispatch(setLanguage(l.code))}
                activeOpacity={0.8}
              >
                <Text style={styles.langFlag}>{l.flag}</Text>
                <Text style={[styles.langLabel, { color: C.textPrimary }, lang === l.code && { color: C.primaryDark }]}>{l.label}</Text>
                <Text style={[styles.langNative, { color: C.textMuted }]}>{l.native}</Text>
                {lang === l.code && <View style={[styles.langCheck, { backgroundColor: C.primary }]}><Text style={styles.langCheckIcon}>✓</Text></View>}
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {/* ── Logout ── */}
        <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout} activeOpacity={0.8}>
          <Text style={styles.logoutIcon}>🚪</Text>
          <Text style={styles.logoutText}>Déconnexion</Text>
        </TouchableOpacity>

        <View style={{ height: 20 }} />
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  scroll: { paddingBottom: 40 },

  section: { paddingHorizontal: 16, marginTop: 20 },
  sectionHeader: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 12 },
  sectionTitle: { fontSize: 12, fontWeight: "800", textTransform: "uppercase", letterSpacing: 1 },
  editBtnText: { fontSize: 14, fontWeight: "700" },

  inputLabel: { fontSize: 11, fontWeight: "700", textTransform: "uppercase", marginBottom: 6 },
  inputValue: { fontSize: 16, fontWeight: "600", marginBottom: 6 },
  input: { borderWidth: 1, borderRadius: 10, paddingHorizontal: 12, paddingVertical: 10, fontSize: 15, fontWeight: "600", marginBottom: 4 },

  langGrid: { flexDirection: "row", flexWrap: "wrap", gap: 10 },
  langCard: {
    width: "47%", borderRadius: 18, padding: 16,
    borderWidth: 1, position: "relative",
  },
  langFlag: { fontSize: 28, marginBottom: 8 },
  langLabel: { fontSize: 14, fontWeight: "700", marginBottom: 2 },
  langNative: { fontSize: 12, fontWeight: "600" },
  langCheck: {
    position: "absolute", top: 10, right: 10, width: 20, height: 20, borderRadius: 10,
    alignItems: "center", justifyContent: "center",
  },
  langCheckIcon: { fontSize: 11, color: "#fff", fontWeight: "900" },

  menuCard: { borderRadius: 18, borderWidth: 1, overflow: "hidden" },
  menuRow: { flexDirection: "row", alignItems: "center", padding: 16, gap: 14 },
  menuIcon: { fontSize: 20 },
  menuLabel: { flex: 1, fontSize: 15, fontWeight: "700" },

  toggleBackground: {
    width: 44, height: 24, borderRadius: 12, padding: 2,
    justifyContent: "center",
  },
  toggleThumb: {
    width: 20, height: 20, borderRadius: 10,
  },

  logoutBtn: {
    marginHorizontal: 16, marginTop: 32, backgroundColor: "rgba(239,83,80,0.08)",
    borderRadius: 18, paddingVertical: 16, flexDirection: "row", alignItems: "center",
    justifyContent: "center", gap: 10, borderWidth: 1, borderColor: "rgba(239,83,80,0.25)",
  },
  logoutIcon: { fontSize: 20 },
  logoutText: { fontSize: 16, fontWeight: "800", color: "#EF9A9A" },
});
