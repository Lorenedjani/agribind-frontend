import { AppLanguage } from "../types/models";

type Translations = Record<string, Record<AppLanguage, string>>;

export const TRANSLATIONS: Translations = {
  nav_prices: { en: "Prices", fr: "Prix", ful: "Coggu", ewe: "Asi", dua: "Sawa" },
  nav_announcements: { en: "Voice", fr: "Annonces", ful: "Kabaaru", ewe: "Nya", dua: "Bebi" },
  nav_weather: { en: "Weather", fr: "Météo", ful: "Wakkati", ewe: "Yame", dua: "Mbua" },
  nav_credits: { en: "Loans", fr: "Crédits", ful: "Nyamande", ewe: "Fe", dua: "Diba" },
  nav_profile: { en: "Profile", fr: "Profil", ful: "Tinde", ewe: "Amenu", dua: "Mulema" },

  market_title: { en: "Market Prices", fr: "Prix du marché", ful: "Coggu luumo", ewe: "Asimasi", dua: "Sawa luumo" },
  weather_title: { en: "Weather", fr: "Météo Locale", ful: "Wakkati gari", ewe: "Yame nyati", dua: "Mbua lokoti" },
  credits_title: { en: "My Loans", fr: "Mes Crédits", ful: "Nyamande am", ewe: "Nye fe", dua: "Diba am" },
  profile_title: { en: "My Profile", fr: "Mon Profil", ful: "Tinde am", ewe: "Nye amenu", dua: "Mulema am" },

  audio_guide: { en: "Tap to listen", fr: "Écouter l'aide", ful: "Nanndu walla", ewe: "Taa fe", dua: "Sengane mboa" },

  up: { en: "Up", fr: "Hausse", ful: "Yeeso", ewe: "Dzi", dua: "Mboa" },
  down: { en: "Down", fr: "Baisse", ful: "Caggal", ewe: "Anyi", dua: "Sita" },
  stable: { en: "Same", fr: "Stable", ful: "Fotde", ewe: "Nenema", dua: "Na bena" },

  amount_paid: { en: "Paid: ", fr: "Payé: ", ful: "Yoɓaa: ", ewe: "Xea: ", dua: "Koba: " },
  amount_left: { en: "Left: ", fr: "Reste: ", ful: "Keddi: ", ewe: "Susu: ", dua: "Tika: " },
  due: { en: "Due: ", fr: "Échéance: ", ful: "Wakkati: ", ewe: "Azan: ", dua: "Ngo: " },

  rain_chance: { en: "Rain", fr: "Pluie", ful: "Ndiyam", ewe: "Tsi", dua: "Mbua" },
  temperature: { en: "Heat", fr: "Chaleur", ful: "Nguleeki", ewe: "Dzo", dua: "Ndolo" },
};

export const t = (key: string, lang: AppLanguage): string => {
  return TRANSLATIONS[key]?.[lang] || TRANSLATIONS[key]?.["en"] || key;
};
