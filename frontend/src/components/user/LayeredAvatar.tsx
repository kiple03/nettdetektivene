import type { EquippedItem } from '@/hooks/useUserProfile';

interface LayeredAvatarProps{
    items: EquippedItem[];
    size?: number;
}

export function LayeredAvatar({items, size = 64}: LayeredAvatarProps){
    const sortedItems = [...items].sort((a,b) => {
        const order = {'base': 1, 'eyes': 2, 'head': 4, 'accessories': 5};
        // @ts-expect-error category string might not exist in order object
        return (order[a.category] || 99) - (order[b.category] || 99);
    });
    
    return (
        <div style={{ 
            position: 'relative', 
            width: size, 
            height: size, 
            borderRadius: '50%', 
            overflow: 'hidden', 
            backgroundColor: '#e5e7eb' 
        }}>
            {/* The Base Avatar Layer */}
            <img 
                src="/assets/cap_blue.svg" 
                alt="Base Avatar"
                style={{
                    position: 'absolute', 
                    top: 0, 
                    left: 0, 
                    width: '100%', 
                    height: '100%', 
                    objectFit: 'contain',
                    zIndex: 0
                }}
            />

            {/* The Equipped Items */}
            {sortedItems.map((item, index) => (
            <img 
                key={item.id}
                src={item.imageUrl}
                alt={item.name}
                style={{
                    position: 'absolute', 
                    top: 0, 
                    left: 0, 
                    width: '100%', 
                    height: '100%', 
                    objectFit: 'contain',
                    zIndex: index + 1
                }}
            />
            ))}
        </div>
    );
}