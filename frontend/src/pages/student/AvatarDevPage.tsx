import { useState } from 'react';
import { AxiosError } from 'axios';
import { LayeredAvatar, SKIN_TONES } from '@/components/user/LayeredAvatar';
import {
  useSaveAvatarOutfit,
  useUserInventory,
  useUserProfile,
  type EquippedItem,
  useUpdateUserProfile
} from '@/hooks/useUserProfile';
import { refreshToken } from '@/api/authApi';
import { getClassroomId } from '@/stores/authStore';
import styles from './AvatarDevPage.module.css';
import {useNavigate} from "react-router-dom";

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

  //Save backend items to local state, ensuring a skin tone is always present
  const [equipped, setEquipped] = useState<EquippedItem[]>(() => {
    const hasSkinTone = initialItems.some(i => i.category === 'skin_tone');
    if (hasSkinTone) return initialItems;
    
    const defaultTone = inventory.find(i => i.name === 'tone_dark' && i.category === 'skin_tone');
    return defaultTone ? [...initialItems, defaultTone] : initialItems;
  });
  const [username, setUsername] = useState(
    userProfile?.username?.replace("Detektiv ", "") || ""
  );
  const [usernameError, setUsernameError] = useState('');

  //SKIN color
  const [skinToneKey, setSkinToneKey] = useState<string>(() => {
  const existing = initialItems.find(i => i.category === 'skin_tone');
  return existing?.name || 'tone_dark';
  });
  const colors = SKIN_TONES[skinToneKey] || SKIN_TONES['tone_dark'];

  const toggleItem = (item: EquippedItem) => {
    setEquipped((prev) => {
      const isAlreadyEquipped = prev.find((i) => i.id === item.id);
      if (isAlreadyEquipped) {
        return prev.filter((i) => i.id !== item.id);
      }
      return [...prev.filter((i) => i.category !== item.category), item];
    });
  };

  // Save to backend
  const handleSave = () => {
    const trimmed = username.trim();
    if (trimmed.length < 3 || trimmed.length > 20) {
      setUsernameError('Brukernavn må være mellom 3 og 20 tegn');
      return;
    }
    setUsernameError('');

    const itemIds = equipped.map(item => item.id);
    saveOutfit(itemIds);

    updateUserProfile(`Detektiv ${trimmed}`, {
      onSuccess: async () => {
        await refreshToken();
        navigate(getClassroomId() ? '/home' : '/join-with-code');
      },
      onError: (err) => {
        const status = (err as AxiosError).response?.status;
        if (status === 409) {
          setUsernameError('Brukernavnet er allerede tatt');
        } else {
          setUsernameError('Kunne ikke lagre brukernavn. Prøv igjen.');
        }
      },
    });
  };
  
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
                setSkinToneKey(toneName);
                const newToneItem = inventory.find(i => i.name === toneName && i.category === 'skin_tone');
                if (newToneItem) {
                  setEquipped(prev => [...prev.filter(i => i.category !== 'skin_tone'), newToneItem]);
                }
              }}
              style={{
                width: '32px',
                height: '32px',
                borderRadius: '50%',
                backgroundColor: toneColors.base,
                border: skinToneKey === toneName ? '3px solid #2563eb' : '2px solid transparent',
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
                onChange={(e) => {
                  setUsername(e.target.value);
                  if (usernameError) setUsernameError('');
                }}
                className="w-full px-3 py-2 border rounded-lg"
                required
                minLength={3}
                maxLength={20}
            />
            {usernameError && (
              <p style={{ color: '#dc2626', marginTop: 4, fontSize: 14 }}>{usernameError}</p>
            )}
          </div>

        <button
          onClick={handleSave}
          disabled={isSaving || username.trim().length < 3}
          className="mt-6 w-full py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-bold shadow-md transition-colors disabled:bg-gray-400"
        >
          {isSaving ? "Lagrer..." : "Lagre anntrett"}
        </button>
      </div>

        <div className={styles.wardrobeSection}>
          <h2 className={styles.sectionTitle}>Din garderobe</h2>
          <div className={styles.gridContainer}>
            {inventory.filter((item) => item.category !== 'skin_tone').map((item) => {
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