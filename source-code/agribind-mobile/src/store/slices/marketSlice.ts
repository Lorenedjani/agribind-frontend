import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { MarketPrice } from "../../types/models";
import { ENDPOINTS } from "../../config/api";
import { RootState } from "../index";

const CACHE_KEY = "agribind:market_prices";

interface MarketState {
  items: MarketPrice[];
  loading: boolean;
  error: string | null;
  lastSync: string | null;
}

const initialState: MarketState = {
  items: [],
  loading: false,
  error: null,
  lastSync: null,
};

/**
 * Map one MarketPriceDto → MarketPrice.
 *
 * FIX 1 — unique id: "CODE-MARKET" so MAIZE/Douala and MAIZE/International
 *          never share the same React key → eliminates "duplicate key" warning.
 *
 * FIX 2 — real trend: caller passes the SYSTEM baseline for this commodity so we
 *          compare actual values instead of guessing from version parity.
 */
function mapMarketPriceDto(
  raw: Record<string, unknown>,
  systemBaseline: number | undefined
): MarketPrice {
  const code   = String(raw.commodityCode ?? raw.commodityId ?? "UNK");
  const name   = String(raw.commodityName ?? raw.commodity   ?? code);
  const market = String(raw.market ?? "");

  // FIX 1: composite id — never collides across markets
  const id = `${code}-${market}`.replace(/\s+/g, "_");

  const priceRaw = raw.price;
  const price    = typeof priceRaw === "number"
    ? priceRaw
    : parseFloat(String(priceRaw ?? "0"));

  let updatedAt = String(raw.updatedAt ?? "");
  if (updatedAt && !updatedAt.includes("T")) {
    updatedAt = new Date(updatedAt).toISOString();
  }
  if (!updatedAt) updatedAt = new Date().toISOString();

  // FIX 2: compute trend from real price comparison
  const priceSource = String(raw.priceSource ?? "SYSTEM").toUpperCase();
  let trend: "up" | "down" | "stable";

  if (priceSource === "GOVERNMENT" && systemBaseline != null && systemBaseline > 0) {
    // Government official price vs system (market reference) price
    if (price > systemBaseline)      trend = "up";
    else if (price < systemBaseline) trend = "down";
    else                             trend = "stable";
  } else {
    // SYSTEM seed row — newly seeded rows (version ≤ 1) are "stable", not "down"
    const version = typeof raw.version === "number" ? raw.version : 1;
    trend = version <= 1 ? "stable" : version % 2 === 0 ? "stable" : "up";
  }

  return {
    id,
    commodity: name,
    market,
    currency:  String(raw.currency ?? "XAF"),
    price:     Number.isFinite(price) ? price : 0,
    updatedAt,
    trend,
  };
}

export const fetchMarketPrices = createAsyncThunk(
  "market/fetchMarketPrices",
  async (_, { getState, dispatch, rejectWithValue }) => {
    // 1️⃣ Hydrate from AsyncStorage immediately so UI is never blank offline
    try {
      const cached = await AsyncStorage.getItem(CACHE_KEY);
      if (cached) {
        const parsed: MarketPrice[] = JSON.parse(cached);
        if (parsed.length > 0) dispatch(marketSlice.actions.setFromCache(parsed));
      }
    } catch { /* non-fatal */ }

    // 2️⃣ Fetch fresh data from the API gateway
    try {
      const token   = (getState() as RootState).auth.token;
      const headers: Record<string, string> = {};
      if (token) headers.Authorization = `Bearer ${token}`;

      const response = await fetch(ENDPOINTS.MARKET, { headers });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);

      const body: unknown = await response.json();
      const arr = Array.isArray(body)
        ? body
        : (body as { data?: unknown[] })?.data;

      if (!Array.isArray(arr)) return [] as MarketPrice[];

      const raws = arr as Record<string, unknown>[];

      // Build commodity-code → SYSTEM price so GOVERNMENT rows get a real baseline
      const systemBaselines = new Map<string, number>();
      for (const row of raws) {
        if (String(row.priceSource ?? "").toUpperCase() === "SYSTEM") {
          const code  = String(row.commodityCode ?? "");
          const p     = typeof row.price === "number"
            ? row.price
            : parseFloat(String(row.price ?? "0"));
          if (code && Number.isFinite(p)) systemBaselines.set(code, p);
        }
      }

      const prices = raws.map((row) =>
        mapMarketPriceDto(
          row,
          systemBaselines.get(String(row.commodityCode ?? ""))
        )
      );

      // 3️⃣ Persist for offline use on next launch
      try {
        await AsyncStorage.setItem(CACHE_KEY, JSON.stringify(prices));
      } catch { /* non-fatal */ }

      return prices;
    } catch (error: unknown) {
      const message =
        error instanceof Error ? error.message : "Failed to fetch market prices";
      return rejectWithValue(message);
    }
  }
);

const marketSlice = createSlice({
  name: "market",
  initialState,
  reducers: {
    /** Hydrate state from AsyncStorage without touching lastSync */
    setFromCache(state, action) {
      if (state.items.length === 0) {
        state.items = action.payload;
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchMarketPrices.pending, (state) => {
        state.loading = true;
        state.error   = null;
      })
      .addCase(fetchMarketPrices.fulfilled, (state, action) => {
        state.loading  = false;
        state.items    = action.payload;
        state.lastSync = new Date().toISOString();
        state.error    = null;
      })
      .addCase(fetchMarketPrices.rejected, (state, action) => {
        state.loading = false;
        state.error   = action.payload as string;
        // Cached items remain visible even on network failure
      });
  },
});

export default marketSlice.reducer;