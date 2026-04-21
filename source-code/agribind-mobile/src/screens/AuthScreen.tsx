import React, { useState, useRef, useEffect } from "react";
import {
  Alert,
  StyleSheet,
  Text,
  TextInput,
  View,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
  Animated,
  Dimensions,
} from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";
import { useSelector, useDispatch } from "react-redux";
import { CameraView, useCameraPermissions } from "expo-camera";
import { RootState } from "../store";
import { sendPhoneOtp, verifyPhoneOtp, resetError, setSession, loginWithQr } from "../store/slices/authSlice";
import { ActivityIndicator } from "react-native";
import { RootStackParamList } from "../navigation/AppNavigator";
import { THEMES } from "../theme";

const { height } = Dimensions.get("window");

type Props = NativeStackScreenProps<RootStackParamList, "Auth">;
type Mode = "phone" | "qr";

export const AuthScreen = ({ navigation }: Props) => {
  const [mode, setMode] = useState<Mode>("phone");
  const [phone, setPhone] = useState("");
  const [otp, setOtp] = useState("");
  const [scanned, setScanned] = useState(false);
  const [permission, requestPermission] = useCameraPermissions();
  const lastQrRef = useRef<{ payload: string; at: number } | null>(null);
  const QR_DEBOUNCE_MS = 3500;

  // Local guard: tracks whether WE triggered a request, so we can
  // reset it independently if the Redux slice gets stuck (e.g. network timeout)
  const [localLoading, setLocalLoading] = useState(false);
  const loadingTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const slideAnim = useRef(new Animated.Value(mode === "phone" ? 0 : 1)).current;

  const dispatch = useDispatch<any>();
  const { loading, error, otpSent } = useSelector((state: RootState) => state.auth);
  const themeMode = useSelector((state: RootState) => state.app.theme) || "dark";
  const C = THEMES[themeMode].colors;

  // When Redux loading goes back to false (resolved or rejected), clear our local guard
  useEffect(() => {
    if (!loading) {
      setLocalLoading(false);
      if (loadingTimerRef.current) {
        clearTimeout(loadingTimerRef.current);
        loadingTimerRef.current = null;
      }
    }
  }, [loading]);

  // Safety net: if loading stays stuck for more than 12 seconds, force-reset
  const startLoadingWithTimeout = () => {
    setLocalLoading(true);
    if (loadingTimerRef.current) clearTimeout(loadingTimerRef.current);
    loadingTimerRef.current = setTimeout(() => {
      setLocalLoading(false);
      dispatch(resetError()); // resets error + loading in slice
      Alert.alert(
        "Délai dépassé",
        "Le serveur ne répond pas. Vérifiez votre connexion réseau ou l'URL du serveur.",
        [{ text: "OK" }]
      );
    }, 12000);
  };

  useEffect(() => {
    return () => {
      if (loadingTimerRef.current) clearTimeout(loadingTimerRef.current);
    };
  }, []);

  useEffect(() => {
    if (error) {
       setLocalLoading(false);
       Alert.alert("Erreur d'authentification", error, [{ text: "OK", onPress: () => dispatch(resetError()) }]);
    }
  }, [error]);

  // Combined: true if Redux OR our local guard says we're loading
  const isLoading = loading || localLoading;

  const switchMode = (m: Mode) => {
    setMode(m);
    setScanned(false);
    setLocalLoading(false);
    if (loadingTimerRef.current) clearTimeout(loadingTimerRef.current);
    lastQrRef.current = null;
    if (m === "qr" && !permission?.granted) {
      requestPermission();
    }
    Animated.spring(slideAnim, { toValue: m === "phone" ? 0 : 1, friction: 8, useNativeDriver: false }).start();
  };

  const underlineLeft = slideAnim.interpolate({ inputRange: [0, 1], outputRange: ["0%", "50%"] });

  const handleSendOtp = () => {
    if (!phone.trim()) { Alert.alert("Erreur", "Entrez votre numéro."); return; }
    startLoadingWithTimeout();
    dispatch(sendPhoneOtp(phone));
  };

  const handleVerify = () => {
    if (!otp.trim()) { Alert.alert("Erreur", "Entrez le code OTP."); return; }
    startLoadingWithTimeout();
    dispatch(verifyPhoneOtp({ phoneNumber: phone, otp }));
  };

  const handleBarCodeScanned = ({ data }: { data: string }) => {
    if (scanned || isLoading) return;
    const now = Date.now();
    const prev = lastQrRef.current;
    if (prev && prev.payload === data && now - prev.at < QR_DEBOUNCE_MS) {
      return;
    }

    // Validate the scanned data looks like a valid AgriBind QR code
    if (!data || !data.trim()) {
      Alert.alert("Code invalide", "Le QR code scanné est vide.");
      return;
    }
    const parts = data.trim().split(":");
    if (parts.length < 4 || parts[0] !== "USER" || parts[2] !== "LOGIN") {
      Alert.alert(
        "Code invalide",
        `Ce QR code n'est pas un QR de connexion AgriBind valide.\nDonnées: ${data.substring(0, 40)}...`,
        [{ text: "Réessayer", onPress: () => setScanned(false) }]
      );
      return;
    }

    lastQrRef.current = { payload: data, at: now };
    setScanned(true);
    startLoadingWithTimeout();
    console.log(`[QR Login] Scanning QR for userId=${parts[1]}, payload length=${data.length}`);
    dispatch(loginWithQr(data.trim()))
      .unwrap()
      .then(() => {
        Alert.alert("Succès", "Connexion réussie !");
      })
      .catch((err: any) => {
        setScanned(false);
        Alert.alert("Erreur", err || "Format QR Code invalide ou expiré.");
      });
  };

  return (
    <KeyboardAvoidingView style={[styles.container, { backgroundColor: C.bg }]} behavior={Platform.OS === "ios" ? "padding" : undefined}>

      {/* Dark hero header with field illustration */}
      <View style={styles.hero}>
        <View style={styles.heroBg}>
          <View style={[styles.sunGlow, { backgroundColor: C.primaryLight, opacity: 0.15 }]} />
          <View style={[styles.heroOverlay, { backgroundColor: themeMode === "dark" ? "rgba(10,31,20,0.8)" : "rgba(43,94,66,0.9)" }]} />
        </View>
        <Text style={styles.heroEmoji}>🌿</Text>
        <Text style={styles.heroTitle}>{"AgriBind Login"}</Text>
        <Text style={styles.heroSub}>{"Accédez à votre espace coopératif"}</Text>
      </View>

      {/* Glass form card */}
      <View style={[styles.card, { backgroundColor: C.surface, borderColor: C.glassBorder }]}>
        {/* Mode tabs */}
        <View style={[styles.tabBar, { borderBottomColor: C.glassBorder }]}>
          {(["phone", "qr"] as Mode[]).map((m) => (
            <TouchableOpacity key={m} style={styles.tab} onPress={() => switchMode(m)}>
              <Text style={[styles.tabText, { color: C.textMuted }, mode === m && { color: C.primaryLight }]}>
                {m === "phone" ? "📱  Téléphone" : "📷  QR Code"}
              </Text>
            </TouchableOpacity>
          ))}
          <Animated.View style={[styles.tabIndicator, { backgroundColor: C.primary, left: underlineLeft }]} />
        </View>

        {/* Phone auth */}
        {mode === "phone" && (
          <View style={styles.form}>
            <Text style={[styles.fieldLabel, { color: C.primaryLight }]}>Numéro de téléphone</Text>
            <TextInput
              style={[styles.input, { backgroundColor: C.glass, borderColor: C.glassBorder, color: C.textPrimary }]}
              value={phone}
              onChangeText={setPhone}
              placeholder="+237 6XX XX XX XX"
              keyboardType="phone-pad"
              placeholderTextColor={C.textMuted}
            />

            <TouchableOpacity style={[styles.primaryBtn, { backgroundColor: C.primary }]} onPress={handleSendOtp} activeOpacity={0.85} disabled={isLoading}>
              {isLoading && !otpSent ? <ActivityIndicator color="#fff" /> : <Text style={styles.primaryBtnText}>{otpSent ? "✓ Renvoyer" : "Envoyer le code OTP"}</Text>}
            </TouchableOpacity>

            {otpSent && (
              <>
                <View style={[styles.otpBanner, { backgroundColor: `${C.primary}1A`, borderColor: `${C.primary}4D` }]}>
                  <Text style={[styles.otpBannerText, { color: C.primaryLight }]}>✅  Code envoyé · Valable 10 m</Text>
                </View>
                <Text style={[styles.fieldLabel, { color: C.primaryLight }]}>Code OTP</Text>
                <TextInput
                  style={[styles.input, styles.otpInput, { backgroundColor: C.glass, borderColor: C.glassBorder, color: C.textPrimary }]}
                  value={otp}
                  onChangeText={setOtp}
                  placeholder="• • • • • •"
                  keyboardType="number-pad"
                  maxLength={6}
                  placeholderTextColor={C.textMuted}
                />
                <TouchableOpacity style={[styles.verifyBtn, { backgroundColor: C.primaryDark, borderColor: C.primary }]} onPress={handleVerify} activeOpacity={0.85} disabled={isLoading}>
                   {isLoading && otpSent ? <ActivityIndicator color="#fff" /> : <Text style={styles.verifyBtnText}>Vérifier et accéder →</Text>}
                </TouchableOpacity>
              </>
            )}
          </View>
        )}

        {/* QR auth */}
        {mode === "qr" && (
          <View style={styles.qrSection}>
            <View style={[styles.qrFrame, { backgroundColor: C.glass, borderColor: C.glassBorder, overflow: 'hidden' }]}>
               {!permission ? (
                 <ActivityIndicator color={C.primary} />
               ) : isLoading ? (
                 <View style={{ gap: 16, alignItems: 'center' }}>
                   <ActivityIndicator size="large" color={C.primary} />
                   <Text style={{ color: C.textSecondary, fontWeight: '700' }}>Authentification...</Text>
                 </View>
               ) : !permission.granted ? (
                 <View style={{ padding: 20, alignItems: 'center' }}>
                   <Text style={{ color: C.textSecondary, textAlign: 'center', marginBottom: 12 }}>
                     L'accès à la caméra est requis pour scanner le QR Code.
                   </Text>
                   <TouchableOpacity onPress={requestPermission} style={[styles.primaryBtn, { backgroundColor: C.primary, paddingHorizontal: 20 }]}>
                     <Text style={styles.primaryBtnText}>Autoriser la caméra</Text>
                   </TouchableOpacity>
                 </View>
               ) : (
                 <CameraView
                   style={StyleSheet.absoluteFillObject}
                   onBarcodeScanned={scanned ? undefined : handleBarCodeScanned}
                   barcodeScannerSettings={{
                     barcodeTypes: ["qr"],
                   }}
                 />
               )}
              <View style={[styles.qrCornerTL, { borderColor: C.primary }]} /><View style={[styles.qrCornerTR, { borderColor: C.primary }]} />
              <View style={[styles.qrCornerBL, { borderColor: C.primary }]} /><View style={[styles.qrCornerBR, { borderColor: C.primary }]} />
            </View>
            <Text style={[styles.qrLabel, { color: C.textSecondary }]}>{"Présentez votre carte membre\n(QR Code coopérative)"}</Text>
          </View>
        )}
      </View>

      <Text style={[styles.footer, { color: C.textMuted }]}>🔒  Données protégées · Chiffrement</Text>
    </KeyboardAvoidingView>
  );
};

const CORNER = 18;
const styles = StyleSheet.create({
  container: { flex: 1 },
  hero: { height: height * 0.32, justifyContent: "flex-end", padding: 24, overflow: "hidden" },
  heroBg: { ...StyleSheet.absoluteFillObject },
  sunGlow: {
    position: "absolute", width: 200, height: 200, borderRadius: 100,
    top: 30, right: 30,
  },
  heroOverlay: { ...StyleSheet.absoluteFillObject },
  heroEmoji: { fontSize: 36, marginBottom: 8 },
  heroTitle: { fontSize: 28, fontWeight: "900", color: "#fff", letterSpacing: -0.5 },
  heroSub: { fontSize: 13, color: "rgba(255,255,255,0.6)", marginTop: 4 },

  card: {
    marginHorizontal: 16, marginTop: -24,
    borderRadius: 24, padding: 22, borderWidth: 1,
    shadowColor: "#000", shadowOpacity: 0.3, shadowRadius: 20, elevation: 8,
  },

  tabBar: { flexDirection: "row", borderBottomWidth: 1, marginBottom: 22, position: "relative" },
  tab: { flex: 1, paddingVertical: 12, alignItems: "center" },
  tabText: { fontSize: 14, fontWeight: "700" },
  tabIndicator: { position: "absolute", bottom: -1, width: "50%", height: 3, borderRadius: 2 },

  form: { gap: 12 },
  fieldLabel: { fontSize: 11, fontWeight: "800", textTransform: "uppercase", letterSpacing: 0.5 },
  input: {
    borderWidth: 1,
    borderRadius: 14, paddingHorizontal: 16, paddingVertical: 14,
    fontSize: 15,
  },
  otpInput: { fontSize: 24, letterSpacing: 10, textAlign: "center", fontWeight: "800" },

  primaryBtn: {
    borderRadius: 14, paddingVertical: 15, alignItems: "center",
    shadowColor: "#000", shadowOpacity: 0.2, shadowRadius: 10, elevation: 5,
  },
  primaryBtnText: { color: "#fff", fontWeight: "800", fontSize: 15 },

  otpBanner: {
    borderRadius: 10, paddingVertical: 8,
    paddingHorizontal: 14, borderWidth: 1, alignItems: "center",
  },
  otpBannerText: { fontSize: 13, fontWeight: "700" },

  verifyBtn: {
    borderRadius: 14, paddingVertical: 15, alignItems: "center",
    borderWidth: 1,
  },
  verifyBtnText: { color: "#fff", fontWeight: "800", fontSize: 15 },

  qrSection: { alignItems: "center", gap: 20 },
  qrFrame: {
    width: 160, height: 160, borderRadius: 18,
    borderWidth: 2, alignItems: "center", justifyContent: "center",
    position: "relative",
  },
  qrCornerTL: { position: "absolute", top: 8, left: 8, width: CORNER, height: CORNER, borderTopWidth: 3, borderLeftWidth: 3, borderTopLeftRadius: 6 },
  qrCornerTR: { position: "absolute", top: 8, right: 8, width: CORNER, height: CORNER, borderTopWidth: 3, borderRightWidth: 3, borderTopRightRadius: 6 },
  qrCornerBL: { position: "absolute", bottom: 8, left: 8, width: CORNER, height: CORNER, borderBottomWidth: 3, borderLeftWidth: 3, borderBottomLeftRadius: 6 },
  qrCornerBR: { position: "absolute", bottom: 8, right: 8, width: CORNER, height: CORNER, borderBottomWidth: 3, borderRightWidth: 3, borderBottomRightRadius: 6 },
  qrEmoji: { fontSize: 60 },
  qrLabel: { textAlign: "center", fontSize: 14, lineHeight: 22 },

  footer: { textAlign: "center", fontSize: 11, padding: 30 },
});