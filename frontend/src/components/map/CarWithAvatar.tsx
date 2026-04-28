import type { EquippedItem } from '@/hooks/useUserProfile';
import { LayeredAvatar } from '@/components/user/LayeredAvatar';

interface CarWithAvatarProps {
    items: EquippedItem[];
    size?: number;
    avatarScale?: number;
    hairColor?: string;
}

export function CarWithAvatar({ items, size = 100, avatarScale = 0.7, hairColor }: CarWithAvatarProps) {
    const avatarSize = size * avatarScale;
    const avatarLeft = (size - avatarSize) / 2;
    const avatarTop = (size - avatarSize) / 2 - size * 0.42;
    return (
        <div style={{ position: 'relative', width: size, height: size }}>
            <div style={{ position: 'absolute', top: avatarTop, left: avatarLeft }}>
                <LayeredAvatar items={items} size={avatarSize} circular={false} hairColor={hairColor} />
            </div>
            <img
                src="/assets/car-for-map-page.png"
                alt=""
                aria-hidden="true"
                style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '100%',
                    height: '100%',
                    zIndex: 1,
                    pointerEvents: 'none',
                }}
            />
        </div>
    );
}
