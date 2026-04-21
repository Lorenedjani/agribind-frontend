// AgriBind Premium "Smart Garden" Design System
// Colors strictly mapped to the Dribbble UI reference

const SHARED_THEME = {
  spacing: { xs: 4, sm: 8, md: 16, lg: 24, xl: 32, xxl: 48 },
  radius: { sm: 10, md: 14, lg: 20, xl: 28, full: 999 },
};

export const DARK_THEME = {
  ...SHARED_THEME,
  mode: "dark" as const,
  colors: {
    primary: "#a3d727", // The vibrant lime green from the leaf button
    primaryLight: "#c2eb5c",
    primaryDark: "#85b51a",
    primaryGlow: "rgba(163, 215, 39, 0.2)",
    
    bg: "#0c1810", // Very dark forest green background
    bgMid: "#111f15",
    
    surface: "#14291a", // Card surface color
    surfaceHigh: "#1d3822",
    
    glass: "rgba(255, 255, 255, 0.04)",
    glassBorder: "rgba(255, 255, 255, 0.08)",
    glassDark: "rgba(0, 0, 0, 0.4)",
    
    textPrimary: "#ffffff",
    textSecondary: "rgba(255, 255, 255, 0.7)",
    textMuted: "rgba(255, 255, 255, 0.4)",
    
    success: "#a3d727",
    warning: "#eab308",
    danger: "#ef4444",
    dangerLight: "#fca5a5",
    info: "#3b82f6",
    systemOK: "#a3d727",
    
    orange: "#f97316",
    gold: "#eab308",
    amberBg: "rgba(234, 179, 8, 0.15)",
    
    actionBlue: "rgba(59, 130, 246, 0.15)",
    actionPurple: "rgba(168, 85, 247, 0.15)",
    actionOrange: "rgba(249, 115, 22, 0.15)",
    actionPink: "rgba(236, 72, 153, 0.15)",
    actionGray: "rgba(156, 163, 175, 0.15)",
  },
};

export const LIGHT_THEME = {
  ...SHARED_THEME,
  mode: "light" as const,
  colors: {
    primary: "#7cb342", // A darker, legible green for light mode
    primaryLight: "#a3d727",
    primaryDark: "#558b2f",
    primaryGlow: "rgba(124, 179, 66, 0.15)",
    
    bg: "#f4f7f4", // Light grayish green background
    bgMid: "#eef2ee",
    
    surface: "#ffffff",
    surfaceHigh: "#f8faf8",
    
    glass: "rgba(0, 0, 0, 0.03)",
    glassBorder: "rgba(0, 0, 0, 0.08)",
    glassDark: "rgba(0, 0, 0, 0.06)",
    
    textPrimary: "#111827",
    textSecondary: "#4b5563",
    textMuted: "#9ca3af",
    
    success: "#22c55e",
    warning: "#f59e0b",
    danger: "#ef4444",
    dangerLight: "#fca5a5",
    info: "#3b82f6",
    systemOK: "#16a34a",
    
    orange: "#ea580c",
    gold: "#ca8a04",
    amberBg: "rgba(245, 158, 11, 0.15)",
    
    actionBlue: "rgba(59, 130, 246, 0.08)",
    actionPurple: "rgba(168, 85, 247, 0.08)",
    actionOrange: "rgba(249, 115, 22, 0.08)",
    actionPink: "rgba(236, 72, 153, 0.08)",
    actionGray: "rgba(107, 114, 128, 0.08)",
  },
};

export const THEMES = {
  dark: DARK_THEME,
  light: LIGHT_THEME,
};

// Legacy support
export const THEME = DARK_THEME;
export type Theme = typeof DARK_THEME;
