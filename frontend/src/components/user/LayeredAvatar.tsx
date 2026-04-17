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
        <div className="relative rounded-full overflow-hidden bg-gray-200" style={{ width: size, height: size }}>
            <img 
                src="/assets/cap_blue.svg"
                alt="Base Avatar"
                className="absolute w-full h-full object-contain"
                style={{zIndex: 0}}/>

            {sortedItems.map((item, index) => (
            <img 
                key={item.id}
                src={item.imageUrl}
                alt={item.name}
                className="absolute w-full h-full object-contain"
                style={{zIndex: index + 1}}
                />
            ))}
        </div>
    );
}