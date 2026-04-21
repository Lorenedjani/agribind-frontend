import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { Announcement, AppLanguage } from "../../types/models";
import { ENDPOINTS, toAbsoluteApiUrl } from "../../config/api";
import * as FileSystem from "expo-file-system";
import { RootState } from "../index";

// ─────────────────────────────────────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────────────────────────────────────

interface AnnouncementState {
  items:    Announcement[];
  loading:  boolean;
  error:    string | null;
  lastSync: string | null;
}

const initialState: AnnouncementState = {
  items:    [],
  loading:  false,
  error:    null,
  lastSync: null,
};

// ─────────────────────────────────────────────────────────────────────────────
// Mappers
// ─────────────────────────────────────────────────────────────────────────────

function mapBackendLanguage(lang: string | undefined): AppLanguage {
  const l = (lang || "").toLowerCase();
  if (l.startsWith("fr") || l === "french")               return "fr";
  if (l.startsWith("en") || l === "english")              return "en";
  if (l.includes("fulfulde") || l === "ful")              return "ful";
  if (l.includes("ewondo")   || l === "ewe")              return "ewe";
  if (l.includes("duala")    || l === "dua")              return "dua";
  return "fr";
}

/**
 * Maps a raw server row → Announcement.
 * Handles both cooperative broadcasts and government announcements
 * ([GOV] prefix in title).
 */
function mapMobileAnnouncement(raw: Record<string, unknown>): Announcement {
  const id       = String(raw.id ?? "");
  const rawTitle = String(raw.title ?? "");

  // Strip the internal [GOV] prefix added by the backend
  const title        = rawTitle.replace(/^\[GOV\]\s*/, "");
  const isGovernment = rawTitle.startsWith("[GOV]");

  const audioRaw = String(raw.audioUrl ?? raw.audioURL ?? "");
  const audioUrl = toAbsoluteApiUrl(audioRaw);

  const updatedRaw = raw.updatedAt;
  let updatedAt =
    typeof updatedRaw === "string" ? updatedRaw : new Date().toISOString();
  if (updatedAt && !updatedAt.includes("T")) {
    updatedAt = new Date(updatedAt).toISOString();
  }

  return {
    id,
    title,
    language:      mapBackendLanguage(String(raw.language ?? "")),
    audioUrl,
    downloaded:    false,
    updatedAt,
    isGovernment,
    source:        String(raw.source ?? (isGovernment ? "Government" : "Cooperative")),
  };
}

// ─────────────────────────────────────────────────────────────────────────────
// Thunks
// ─────────────────────────────────────────────────────────────────────────────

/** Download audio to device storage so it's available offline. */
export const downloadAudioFile = createAsyncThunk(
  "announcements/downloadAudioFile",
  async (announcement: Announcement, { rejectWithValue }) => {
    try {
      if (
        !announcement.audioUrl ||
        announcement.audioUrl.startsWith("file://") ||
        announcement.downloaded
      ) {
        return announcement;
      }

      // Query-based stream URLs (e.g. /mobile/broadcast-audio?... ) may not have
      // an extension in the URL path, so default to webm for offline cache files.
      const urlWithoutQuery = announcement.audioUrl.split("?")[0];
      const extMatch = urlWithoutQuery.match(/\.([a-zA-Z0-9]+)$/);
      const fileExt = (extMatch?.[1] || "webm").toLowerCase();
      // @ts-expect-error Expo FS documentDirectory
      const fileUri = `${FileSystem.documentDirectory}announcement_${announcement.id.replace(
        /[^a-zA-Z0-9_-]/g,
        "_"
      )}.${fileExt}`;

      const fileInfo = await FileSystem.getInfoAsync(fileUri);
      if (!fileInfo.exists) {
        const { uri } = await FileSystem.downloadAsync(
          announcement.audioUrl,
          fileUri,
          {}
        );
        return { ...announcement, audioUrl: uri, downloaded: true };
      }

      return { ...announcement, audioUrl: fileUri, downloaded: true };
    } catch (error: unknown) {
      const message =
        error instanceof Error ? error.message : "Download failed";
      return rejectWithValue({ id: announcement.id, error: message });
    }
  }
);

/**
 * Fetch all announcements from the microservice.
 *
 * Uses ?updatedSince=<lastSync ISO> for delta sync so the mobile app
 * only downloads new/changed entries after the first full load.
 */
export const fetchAnnouncements = createAsyncThunk(
  "announcements/fetchAnnouncements",
  async (_, { dispatch, getState, rejectWithValue }) => {
    try {
      const state   = getState() as RootState;
      const token   = state.auth.token;
      const lastSync = state.announcements.lastSync;

      const headers: Record<string, string> = {};
      if (token) headers.Authorization = `Bearer ${token}`;

      // Delta sync: only request items newer than our last sync
      let url = ENDPOINTS.ANNOUNCEMENTS;
      if (lastSync) {
        url += `?updatedSince=${encodeURIComponent(lastSync)}`;
      }

      const response = await fetch(url, { headers });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const body: unknown = await response.json();
      const arr = Array.isArray(body)
        ? body
        : (body as { data?: unknown[] })?.data;
      if (!Array.isArray(arr)) return [] as Announcement[];

      const freshItems = arr.map((row) =>
        mapMobileAnnouncement(row as Record<string, unknown>)
      );

      // Kick off background downloads
      freshItems.forEach((ann) => {
        if (ann.audioUrl && !ann.audioUrl.startsWith("file://")) {
          dispatch(downloadAudioFile(ann));
        }
      });

      return freshItems;
    } catch (error: unknown) {
      const message =
        error instanceof Error ? error.message : "Failed to fetch announcements";
      return rejectWithValue(message);
    }
  }
);

// ─────────────────────────────────────────────────────────────────────────────
// Slice
// ─────────────────────────────────────────────────────────────────────────────

const announcementSlice = createSlice({
  name: "announcements",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      // ── fetchAnnouncements ──────────────────────────────────────────────
      .addCase(fetchAnnouncements.pending, (state) => {
        state.loading = true;
        state.error   = null;
      })
      .addCase(fetchAnnouncements.fulfilled, (state, action) => {
        state.loading = false;
        state.error   = null;

        const newData        = action.payload;
        const currentDataMap = new Map(state.items.map((i) => [i.id, i]));

        // Merge: prefer cached offline audio paths
        const merged = newData.map((newItem) => {
          const old = currentDataMap.get(newItem.id);
          if (old?.downloaded && old.audioUrl.startsWith("file://")) {
            return { ...newItem, audioUrl: old.audioUrl, downloaded: true };
          }
          return newItem;
        });

        if (state.lastSync) {
          // Delta sync: upsert new items, keep items not returned by server
          const newIds = new Set(merged.map((i) => i.id));
          const kept   = state.items.filter((i) => !newIds.has(i.id));
          // Government announcements first, then cooperative, then rest
          state.items = [...merged, ...kept].sort((a, b) => {
            if (a.isGovernment && !b.isGovernment) return -1;
            if (!a.isGovernment && b.isGovernment) return 1;
            return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
          });
        } else {
          // First full load — sort government first
          state.items = merged.sort((a, b) => {
            if (a.isGovernment && !b.isGovernment) return -1;
            if (!a.isGovernment && b.isGovernment) return 1;
            return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
          });
        }

        state.lastSync = new Date().toISOString();
      })
      .addCase(fetchAnnouncements.rejected, (state, action) => {
        state.loading = false;
        state.error   = action.payload as string;
      })

      // ── downloadAudioFile ───────────────────────────────────────────────
      .addCase(downloadAudioFile.fulfilled, (state, action) => {
        const index = state.items.findIndex((i) => i.id === action.payload.id);
        if (index !== -1) {
          state.items[index] = action.payload;
        }
      });
  },
});

export default announcementSlice.reducer;