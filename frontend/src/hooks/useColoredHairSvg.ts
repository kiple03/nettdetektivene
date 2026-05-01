import { useQuery } from '@tanstack/react-query';
import { applyHairColor } from '@/utils/hairColor';

interface UseColoredHairSvgResult {
  coloredSvg: string | null;
  isLoading: boolean;
}

/**
 * Fetches an SVG hair item from `imageUrl` and recolors it to `targetHex`.
 * Returns `null` while loading or when `targetHex` is undefined.
 */
export function useColoredHairSvg(
  imageUrl: string,
  itemName: string,
  targetHex: string | undefined
): UseColoredHairSvgResult {
  const { data: rawSvg, isLoading } = useQuery({
    queryKey: ['hair-svg', imageUrl],
    queryFn: () => fetch(imageUrl).then(r => r.text()),
    staleTime: Infinity,
    enabled: !!targetHex,
  });

  if (!rawSvg || !targetHex) {
    return { coloredSvg: null, isLoading };
  }

  return { coloredSvg: applyHairColor(rawSvg, itemName, targetHex), isLoading };
}
