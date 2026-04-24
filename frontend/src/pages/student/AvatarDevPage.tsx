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
import detectiveRoom from "@/assets/detective_room_interior.png";

type Gender = 'boy' | 'girl';

const ITEM_GENDER: Record<string, Gender> = {
  face_1: 'boy',
  face_2: 'boy',
  face_3: 'boy',
  face_girl: 'girl',
  boy_hair: 'boy',
  girl_hair: 'girl',
};

const DEFAULT_FACE: Record<Gender, string> = {
  boy: 'face_1',
  girl: 'face_girl',
};

const DEFAULT_HAIR: Record<Gender, string> = {
  boy: 'boy_hair',
  girl: 'girl_hair',
};

export default function AvatarDevPage() {
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

  const [skinToneKey, setSkinToneKey] = useState<string>(() => {
    const existing = initialItems.find(i => i.category === 'skin_tone');
    return existing?.name || 'tone_dark';
  });
  const colors = SKIN_TONES[skinToneKey] || SKIN_TONES['tone_dark'];

  const [gender, setGender] = useState<Gender>(() => {
    const equippedHair = initialItems.find(i => i.category === 'hair');
    return (equippedHair && ITEM_GENDER[equippedHair.name] === 'girl') ? 'girl' : 'boy';
  });

  const toggleItem = (item: EquippedItem) => {
    setEquipped((prev) => {
      const isAlreadyEquipped = prev.find((i) => i.id === item.id);
      if (isAlreadyEquipped) {
        return prev.filter((i) => i.id !== item.id);
      }
      return [...prev.filter((i) => i.category !== item.category), item];
    });
  };

  const handleGenderChange = (newGender: Gender) => {
    setGender(newGender);

    setEquipped((prev) => {
      let next = [...prev];

      // Swap hair
      const hairItem = inventory.find(i => i.name === DEFAULT_HAIR[newGender]);
      if (hairItem) {
        next = next.filter(i => i.category !== 'hair');
        next.push(hairItem);
      }

      // Swap face only if current face belongs to the other gender
      const currentFace = next.find(i => i.category === 'face');
      if (currentFace && ITEM_GENDER[currentFace.name] && ITEM_GENDER[currentFace.name] !== newGender) {
        const newFace = inventory.find(i => i.name === DEFAULT_FACE[newGender]);
        if (newFace) {
          next = next.filter(i => i.category !== 'face');
          next.push(newFace);
        }
      }

      return next;
    });
  };

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

  const visibleInventory = inventory.filter(item => {
    if (item.category === 'skin_tone') return false;
    const itemGender = ITEM_GENDER[item.name];
    return !itemGender || itemGender === gender;
  });

  return (
    <div className={styles.page}>
      <img src={detectiveRoom} alt="" className={styles.bg}/>
      <div className={styles.container}>
        <div className={styles.previewSection}>
          <h2 className={styles.sectionTitle}>Din profil</h2>
          <div className={styles.avatarWrapper}>
            <LayeredAvatar items={equipped} size={256} skinToneBase={colors.base} skinToneShadowDark={colors.shadowDark} skinToneShadowLight={colors.shadowLight} />
          </div>

          {/* Gender selection */}
          <div className={styles.genderRow}>
            <button
              className={`${styles.genderButton} ${gender === 'boy' ? styles.genderButtonActive : ''}`}
              onClick={() => handleGenderChange('boy')}
            >
              Gutt
            </button>
            <button
              className={`${styles.genderButton} ${gender === 'girl' ? styles.genderButtonActive : ''}`}
              onClick={() => handleGenderChange('girl')}
            >
              Jente
            </button>
          </div>

          {/* Skin tone */}
          <div className={styles.skinRow}>
            <span className={styles.skinLabel}>Hudfarge</span>
            <div className={styles.skinButtons}>
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
          </div>

          <div className={styles.nameField}>
            <label className={styles.nameLabel}>Detektiv navn</label>
            <input
              type="text"
              value={username}
              onChange={(e) => {
                setUsername(e.target.value);
                if (usernameError) setUsernameError('');
              }}
              className={styles.nameInput}
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
            {isSaving ? "Lagrer..." : "Lagre antrekk"}
          </button>
        </div>

        <div className={styles.wardrobeSection}>
          <h2 className={styles.sectionTitle}>Din garderobe</h2>
          <div className={styles.gridContainer}>
            {visibleInventory.map((item) => {
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
                  {isEquipped && <span className={styles.equippedBadge}>EQUIPPED</span>}
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
