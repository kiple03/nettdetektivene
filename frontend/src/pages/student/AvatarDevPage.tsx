import { useState } from 'react';
import { LayeredAvatar } from '@/components/user/LayeredAvatar';
import type { EquippedItem } from '@/hooks/useUserProfile';
import styles from './AvatarDevPage.module.css';

// Create some dummy items for testing
const MOCK_ITEMS: EquippedItem[] = [
  { id: '1', name: 'Cool Glasses', category: 'eyes', imageUrl: '/assets/glasses.svg' },
  { id: '2', name: 'Red Shirt', category: 'base', imageUrl: '/assets/shirt_red.svg' },
  { id: '3', name: 'Blue Cap', category: 'head', imageUrl: '/assets/cap_blue.svg' },
  { id: '4', name: 'Gold Necklace', category: 'accessories', imageUrl: '/assets/necklace.svg' },
];

export default function AvatarDevPage() {
  const [equipped, setEquipped] = useState<EquippedItem[]>([]);

  const toggleItem = (item: EquippedItem) => {
    setEquipped((prev) => {
      const isAlreadyEquipped = prev.find((i) => i.id === item.id);
      if (isAlreadyEquipped) {
        return prev.filter((i) => i.id !== item.id);
      }
      return [...prev, item];
    });
  };

  return (
    <div className={styles.container}>
      
      {/* LEFT SIDE: The Live Avatar Preview */}
      <div className={styles.previewSection}>
        <h2 className={styles.sectionTitle}>Live Preview</h2>
        <div className={styles.avatarWrapper}>
            <LayeredAvatar items={equipped} size={256} />
        </div>
        <p className={styles.equippedText}>Equipped: {equipped.length} items</p>
      </div>

      {/* RIGHT SIDE: The Wardrobe / Items list */}
      <div className={styles.wardrobeSection}>
        <h2 className={styles.sectionTitle}>Your Wardrobe</h2>
        
        <div className={styles.gridContainer}>
          {MOCK_ITEMS.map((item) => {
            const isEquipped = equipped.find((i) => i.id === item.id);
            
            return (
              <button
                key={item.id}
                onClick={() => toggleItem(item)}
                className={`${styles.itemButton} ${isEquipped ? styles.itemButtonEquipped : styles.itemButtonUnequipped}`}
              >
                <div className={styles.imageWrapper}>
                    <img 
                      src={item.imageUrl} 
                      alt={item.name} 
                      className={styles.itemImage}
                      onError={(e) => (e.currentTarget.style.display = 'none')}
                    />
                </div>
                
                <span className={styles.itemName}>{item.name}</span>
                <span className={styles.itemCategory}>{item.category}</span>
                
                {isEquipped && (
                  <span className={styles.equippedBadge}>
                    EQUIPPED
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </div>
      
    </div>
  );
}