export type AppLanguage = "en" | "fr" | "ful" | "ewe" | "dua";

export interface FarmerProfile {
  id: string;
  name: string;
  phoneNumber: string;
  preferredLanguage: AppLanguage;
  cooperativeId?: string;
  cooperativeName?: string;
  region?: string;
  farmSizeHa?: number;
}

export interface MarketPrice {
  id:        string;
  commodity: string;
  market:    string;
  currency:  string;
  price:     number;
  updatedAt: string;
  trend?:    "up" | "down" | "stable";
}

export interface Announcement {
  id:           string;
  title:        string;
  language:     AppLanguage;
  audioUrl:     string;
  downloaded:   boolean;
  updatedAt:    string;

  /**
   * true  → published by a government agent via the government portal.
   * false → published by a cooperative manager via the dashboard.
   *
   * Government announcements are pinned to the top of the list
   * and displayed with a distinct badge.
   */
  isGovernment?: boolean;

  /**
   * Human-readable source label, e.g.:
   *   "Ministry of Agriculture"  (government)
   *   "Cooperative Baham"        (cooperative, optional)
   */
  source?: string;
}

export interface WeatherDay {
  date:        string;
  tempHigh:    number;
  tempLow:     number;
  condition:   string;
  rainChance:  number;
  humidity:    number;
  windKph:     number;
}

export interface WeatherData {
  region:               string;
  current:              WeatherDay;
  forecast:             WeatherDay[];
  agriculturalAdvisory: string;
  cachedAt:             string;
}

export type CreditStatus = "CURRENT" | "OVERDUE" | "PAID";

export interface CreditRepayment {
  id:        string;
  dueDate:   string;
  paidDate?: string;
  amount:    number;
  currency:  string;
  status:    CreditStatus;
}

export interface MicroCredit {
  id:              string;
  cooperativeId:   string;
  cooperativeName: string;
  totalAmount:     number;
  amountRepaid:    number;
  currency:        string;
  startDate:       string;
  endDate:         string;
  purpose:         string;
  status:          CreditStatus;
  repayments:      CreditRepayment[];
}

export type NotificationCategory =
  | "PRICE_ALERT"
  | "WEATHER"
  | "COOPERATIVE"
  | "PAYMENT"
  | "PLANT_HEALTH"
  | "GENERAL";

export interface AppNotification {
  id:        string;
  category:  NotificationCategory;
  title:     string;
  body:      string;
  read:      boolean;
  createdAt: string;
}

export type SeverityLevel = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export interface PlantHealthAlert {
  id:             string;
  cropName:       string;
  diseaseName:    string;
  severity:       SeverityLevel;
  affectedAreaHa: number;
  recommendation: string;
  reportedAt:     string;
}