import { useState } from 'react';
import { LayeredAvatar } from '@/components/user/LayeredAvatar';
import {
  useSaveAvatarOutfit,
  useUserInventory,
  useUserProfile,
  type EquippedItem,
  useUpdateUserProfile
} from '@/hooks/useUserProfile';
import styles from './AvatarDevPage.module.css';
import {useNavigate} from "react-router-dom";

const SKIN_TONES: Record<string, {base: string, shadowDark: string, shadowLight: string}> = {
  'tone_light': { base: "#f8c09b", shadowDark: "#da7e61", shadowLight: "#eba482" },
  'tone_medium': { base: "#d2996d", shadowDark: "#b36b41", shadowLight: "#c38659" },
  'tone_dark': { base: "#59381c", shadowDark: "#543010", shadowLight: "#3a1d06" },
  'tone_wacky': { base: "#78b159", shadowDark: "#528537", shadowLight: "#679d4a" }
}

export default function AvatarDevPage() {

  //Fetch backend user profile
  const {data: userProfile, isLoading: isProfileLoading} = useUserProfile();
  const {data: inventory, isLoading: isInventoryLoading } = useUserInventory();

  if(isProfileLoading || isInventoryLoading) return <div>Loading wardrobe...</div>;
  if(!userProfile || !inventory) return <div>Error loading profile</div>;

  return <ChangingRoom initialItems={userProfile.equippedItems || []} inventory={inventory} />;
}

function ChangingRoom({initialItems, inventory}: {initialItems: EquippedItem[], inventory: EquippedItem[] }) {
  const navigate = useNavigate();

  const { data: userProfile } = useUserProfile();
  const { mutate: updateUserProfile } = useUpdateUserProfile();

  const {mutate: saveOutfit, isPending: isSaving} = useSaveAvatarOutfit();
  //Save backend items to local state
  const [equipped, setEquipped] = useState<EquippedItem[]>(initialItems);
  const [username, setUsername] = useState(
    userProfile?.username?.replace("Detektiv ", "") || ""
  );

  //SKIN color
  const equippedSkinItem = equipped.find(i => i.category === 'skin_tone');
  const colors = SKIN_TONES[equippedSkinItem?.name || 'tone_dark'] || SKIN_TONES['tone_dark'];

  const toggleItem = (item: EquippedItem) => {
    setEquipped((prev) => {
      const isAlreadyEquipped = prev.find((i) => i.id === item.id);
      if (isAlreadyEquipped) {
        return prev.filter((i) => i.id !== item.id);
      }
      return [...prev, item];
    });
  };

  // Save to backend
  const handleSave = () => {
    const itemIds = equipped.map(item => item.id);

    updateUserProfile(`Detektiv ${username}`);
    saveOutfit(itemIds);

    navigate('/home');
  }
  
  return (

    <div className={styles.container}>
      <div className={styles.previewSection}>
        <h2 className={styles.sectionTitle}>Din profil</h2>
        <div className={styles.avatarWrapper}>
            <LayeredAvatar items={equipped} size={256} skinToneBase={colors.base} skinToneShadowDark={colors.shadowDark} skinToneShadowLight={colors.shadowLight} />
        </div>

         <div style={{ display: 'flex', justifyContent: 'center', gap: '12px', marginTop: '20px', marginBottom: '10px' }}>
          {Object.entries(SKIN_TONES).map(([toneName, toneColors]) => (
            <button
              key={toneName}
              onClick={() => {
                // Creates a simulated item for the skin tone and replaces the current one
                const skinItem: EquippedItem = { id: toneName, name: toneName, category: 'skin_tone', imageUrl: '' };
                setEquipped(prev => [...prev.filter(i => i.category !== 'skin_tone'), skinItem]);
              }}
              style={{
                width: '32px',
                height: '32px',
                borderRadius: '50%',
                backgroundColor: toneColors.base,
                border: equippedSkinItem?.name === toneName ? '3px solid #2563eb' : '2px solid transparent',
                cursor: 'pointer',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
              }}
              title={`Select ${toneName}`}
              aria-label={`Select ${toneName}`}
            />
          ))}
        </div>

        <p className={styles.equippedText}>Trying on: {equipped.length} items</p>
        
         <div className="mt-4">
            <label className="block font-semibold mb-1">Detektiv navn</label>
            <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-3 py-2 border rounded-lg"
            />
          </div>

        <button 
          onClick={handleSave}
          disabled={isSaving}
          className="mt-6 w-full py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-bold shadow-md transition-colors disabled:bg-gray-400"
        >
          {isSaving ? "Lagrer..." : "Lagre anntrett"}
        </button>
      </div>

        <div className={styles.wardrobeSection}>
          <h2 className={styles.sectionTitle}>Din garderobe</h2>
          <div className={styles.gridContainer}>
            {inventory.map((item) => {
              const isEquipped = equipped.some((i) => i.id === item.id);
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
                    {isEquipped && <span className={styles.equippedBadge}>EQUIPPED</span>}
                  </button>
              );
            })}
          </div>
        </div>
      </div>
  );
}