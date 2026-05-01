export const SKIN_TONES: Record<string, { base: string; shadowDark: string; shadowLight: string }> = {
    tone_light: { base: "#f8c09b", shadowDark: "#da7e61", shadowLight: "#eba482" },
    tone_medium: { base: "#d2996d", shadowDark: "#b36b41", shadowLight: "#c38659" },
    tone_dark: { base: "#59381c", shadowDark: "#543010", shadowLight: "#3a1d06" },
    tone_wacky: { base: "#78b159", shadowDark: "#528537", shadowLight: "#679d4a" },
};

export const DEFAULT_TONE = SKIN_TONES.tone_dark;

export const HATS_WITH_HAIR_MASK = new Set([
    "detective_hat",
    "police_hat",
    "sheriff_hat",
    "VR_goggles",
]);