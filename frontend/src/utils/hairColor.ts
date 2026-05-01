/**
 * A set of base colours in an SVG hair asset, each with a brightness ratio
 * relative to the "base" shade so highlights/shadows scale together.
 */
type HairColorProfile = Array<{ original: string; ratio: number }>;

/**
 * Per-asset colour profiles keyed by avatar item name.
 * Each entry maps the hardcoded SVG colours to a ratio used to derive
 * the equivalent shade when recolouring.
 */
const HAIR_COLOR_PROFILES: Record<string, HairColorProfile> = {
  boy_hair: [
    { original: 'rgb(90,55,35)',  ratio: 1.0    },
    { original: 'rgb(49,25,8)',   ratio: 0.4556 },
    { original: 'rgb(145,93,63)', ratio: 1.6722 },
  ],
  girl_hair: [
    { original: 'rgb(105,63,42)', ratio: 1.0    },
    { original: 'rgb(60,32,11)',  ratio: 0.4905 },
  ],
  afro: [
    { original: 'rgb(74,46,35)', ratio: 1.0  },
    { original: 'rgb(28,9,3)',   ratio: 0.38 },
  ],
  mohawk: [
    { original: 'rgb(54,63,72)', ratio: 1.0  },
    { original: 'rgb(24,8,8)',   ratio: 0.44 },
  ],
  girl_bob_hair: [
    { original: 'rgb(154,90,54)', ratio: 1.0  },
    { original: 'rgb(85,42,25)',  ratio: 0.55 },
  ],
};

/** Converts a 6-digit hex colour string to its R, G, B integer channels. */
function hexToRgbChannels(hex: string): [number, number, number] {
  const clean = hex.replace('#', '');
  return [
    parseInt(clean.slice(0, 2), 16),
    parseInt(clean.slice(2, 4), 16),
    parseInt(clean.slice(4, 6), 16),
  ];
}

/** Returns an `rgb(…)` string scaled by `ratio` relative to the given hex colour. */
function shadeHex(hex: string, ratio: number): string {
  const [r, g, b] = hexToRgbChannels(hex);
  const clamp = (v: number) => Math.min(255, Math.max(0, Math.round(v)));
  return `rgb(${clamp(r * ratio)},${clamp(g * ratio)},${clamp(b * ratio)})`;
}

/**
 * Replaces all hardcoded hair colours in an SVG string with shades derived from `targetHex`.
 * Returns the original string unchanged if no profile exists for `itemName`.
 */
export function applyHairColor(svgText: string, itemName: string, targetHex: string): string {
  const profile = HAIR_COLOR_PROFILES[itemName];
  if (!profile) return svgText;

  let result = svgText;
  for (const entry of profile) {
    result = result.replaceAll(entry.original, shadeHex(targetHex, entry.ratio));
  }
  return result;
}
