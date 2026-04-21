import type { EquippedItem } from '@/hooks/useUserProfile';
import { LayeredAvatar } from '@/components/user/LayeredAvatar';

interface CarWithAvatarProps {
    items: EquippedItem[];
    size?: number;
}

export function CarWithAvatar({ items, size = 100 }: CarWithAvatarProps) {
    return (
        <div style={{ position: 'relative', width: size, height: size }}>
            <LayeredAvatar items={items} size={size} />
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
