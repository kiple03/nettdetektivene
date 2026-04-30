import { useState, useRef, useCallback } from 'react';
import { AxiosError } from 'axios';
import { LayeredAvatar, SKIN_TONES } from '@/components/miscellaneous/LayeredAvatar';
import {
  useSaveAvatarOutfit,
  useUserInventory,
  useUserProfile,
  type EquippedItem,
  useUpdateUserProfile,
} from '@/hooks/useUserProfile';
import { getClassroomId } from '@/stores/authStore';
import styles from './AvatarDevPage.module.css';
import { useNavigate, useLocation } from "react-router-dom";
import detectiveRoom from "@/assets/detective_room_interior.png";
import BackButton from '@/components/ui/buttons/BackButton';

type Gender = 'boy' | 'girl';

const ITEM_GENDER: Record<string, Gender> = {
  face_1: 'boy',
  face_2: 'boy',
  face_3: 'boy',
  face_girl: 'girl',
  face_girl_2: 'girl',
  face_girl_3: 'girl',
  boy_hair: 'boy',
  girl_hair: 'girl',
  girl_bob_hair: 'girl',
  mohawk: 'boy',
};

const DEFAULT_FACE: Record<Gender, string> = {
  boy: 'face_1',
  girl: 'face_girl',
};

const DEFAULT_HAIR: Record<Gender, string> = {
  boy: 'boy_hair',
  girl: 'girl_hair',
};

const CATEGORY_ORDER = ['hair', 'face', 'head', 'body', 'accessories'] as const;
const CATEGORY_LABELS: Record<string, string> = {
  hair: 'Hår',
  face: 'Ansikt',
  head: 'Hodeplagg',
  body: 'Klær',
  accessories: 'Tilbehør',
};

const HAIR_COLORS: { name: string; color: string }[] = [
  { name: 'black',       color: '#1a1008' },
  { name: 'dark-brown',  color: '#3b1f0a' },
  { name: 'brown',       color: '#7b4a1e' },
  { name: 'auburn',      color: '#c47b2b' },
  { name: 'blonde',      color: '#d4a84b' },
  { name: 'red',         color: '#b53a1a' },
  { name: 'grey',        color: '#888888' },
];

export default function AvatarDevPage() {
  const {data: userProfile, isLoading: isProfileLoading} = useUserProfile();
  const {data: inventory, isLoading: isInventoryLoading } = useUserInventory();

  if(isProfileLoading || isInventoryLoading) return <div>Loading wardrobe...</div>;
  if(!userProfile || !inventory) return <div>Error loading profile</div>;

  return <ChangingRoom initialItems={userProfile.equippedItems || []} inventory={inventory} />;
}

function ChangingRoom({initialItems, inventory}: {initialItems: EquippedItem[], inventory: EquippedItem[] }) {
  const navigate = useNavigate();
  const location = useLocation();
  const hideBackButton = location.state?.fromAuth === true;

  const { data: userProfile } = useUserProfile();
  const { mutate: updateUserProfile } = useUpdateUserProfile();
  const {mutate: saveOutfit, isPending: isSaving} = useSaveAvatarOutfit();

  const [equipped, setEquipped] = useState<EquippedItem[]>(() => {
    let items = [...initialItems];
    if (!items.some(i => i.category === 'skin_tone')) {
      const defaultTone = inventory.find(i => i.name === 'tone_dark' && i.category === 'skin_tone');
      if (defaultTone) items = [...items, defaultTone];
    }
    if (!items.some(i => i.category === 'hair')) {
      const defaultHair = inventory.find(i => i.name === 'boy_hair' && i.category === 'hair');
      if (defaultHair) items = [...items, defaultHair];
    }
    return items;
  });

  const [username, setUsername] = useState(
    userProfile?.username?.replace("Detektiv ", "") || ""
  );
  const [usernameError, setUsernameError] = useState('');
  const nameInputMouseRef = useRef(false);
  const [nameInputKeyboardFocused, setNameInputKeyboardFocused] = useState(false);

  const [skinToneKey, setSkinToneKey] = useState<string>(() => {
    const existing = initialItems.find(i => i.category === 'skin_tone');
    return existing?.name || 'tone_dark';
  });
  const colors = SKIN_TONES[skinToneKey] || SKIN_TONES['tone_dark'];

  const [gender, setGender] = useState<Gender>(() => {
    const equippedHair = initialItems.find(i => i.category === 'hair');
    return (equippedHair && ITEM_GENDER[equippedHair.name] === 'girl') ? 'girl' : 'boy';
  });

  const [hairColor, setHairColor] = useState(() => {
    const savedHex = userProfile?.hairColor;
    return HAIR_COLORS.find(c => c.color === savedHex)?.name ?? HAIR_COLORS[0].name;
  });
  const hairColorHex = HAIR_COLORS.find(c => c.name === hairColor)?.color;

  const [saveButtonVisible, setSaveButtonVisible] = useState(false);
  const scrollableRef = useRef<HTMLDivElement>(null);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const saveButtonRef = useCallback((node: HTMLButtonElement | null) => {
    observerRef.current?.disconnect();
    observerRef.current = null;
    if (!node) return;
    observerRef.current = new IntersectionObserver(
      ([entry]) => setSaveButtonVisible(entry.isIntersecting),
      { root: scrollableRef.current, threshold: 0.1 }
    );
    observerRef.current.observe(node);
  }, []);

  // Derive initial equipped for edit detection (props don't change across renders)
  const initEquipped: EquippedItem[] = (() => {
    let items = [...initialItems];
    if (!items.some(i => i.category === 'skin_tone')) {
      const defaultTone = inventory.find(i => i.name === 'tone_dark' && i.category === 'skin_tone');
      if (defaultTone) items = [...items, defaultTone];
    }
    if (!items.some(i => i.category === 'hair')) {
      const defaultHair = inventory.find(i => i.name === 'boy_hair' && i.category === 'hair');
      if (defaultHair) items = [...items, defaultHair];
    }
    return items;
  })();
  const initEquippedIds = new Set(initEquipped.map(i => i.id));
  const initGender: Gender = (() => {
    const hair = initEquipped.find(i => i.category === 'hair');
    return (hair && ITEM_GENDER[hair.name] === 'girl') ? 'girl' : 'boy';
  })();
  const initHairColor = HAIR_COLORS.find(c => c.color === userProfile?.hairColor)?.name ?? HAIR_COLORS[0].name;
  const initSkinTone = initEquipped.find(i => i.category === 'skin_tone')?.name || 'tone_dark';
  const initUsername = userProfile?.username?.replace("Detektiv ", "") || "";

  const hasEdited =
    username !== initUsername ||
    gender !== initGender ||
    hairColor !== initHairColor ||
    skinToneKey !== initSkinTone ||
    equipped.length !== initEquippedIds.size ||
    equipped.some(i => !initEquippedIds.has(i.id));

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

      const hairItem = inventory.find(i => i.name === DEFAULT_HAIR[newGender]);
      if (hairItem) {
        next = next.filter(i => i.category !== 'hair');
        next.push(hairItem);
      }

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
      setUsernameError('Brukernavn må være mellom 3 og 30 tegn');
      return;
    }
    setUsernameError('');

    const itemIds = equipped.map(item => item.id);
    saveOutfit(itemIds);

    updateUserProfile({ username: `Detektiv ${trimmed}`, hairColor: hairColorHex }, {
      onSuccess: () => {
        navigate(getClassroomId() ? '/home' : '/join-with-code');
      },
      onError: (err) => {
        const axiosErr = err as AxiosError<{ message?: string }>;
        const status = axiosErr.response?.status;
        if (status === 400) {
          setUsernameError(axiosErr.response?.data?.message ?? 'Ugyldig brukernavn');
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
      {!hideBackButton && <BackButton className={styles.backBtn} ariaLabel='Gå tilbake'/>}
      <img src={detectiveRoom} alt="" className={styles.bg}/>
      <div className={styles.container}>

        {/* Left column */}
        <div className={styles.previewSection}>
          {hasEdited && !saveButtonVisible && <div className={styles.bouncingArrow}>▼</div>}
          {/* Sticky top: title + avatar */}
          <div className={styles.previewTop}>
            <h2 className={styles.sectionTitle}>Din profil</h2>
            <div className={styles.avatarWrapper}>
              <LayeredAvatar items={equipped} size={160} skinToneBase={colors.base} skinToneShadowDark={colors.shadowDark} skinToneShadowLight={colors.shadowLight} hairColor={hairColorHex} />
            </div>
            </div>

          {/* Scrollable controls */}
          <div className={styles.previewScrollable} ref={scrollableRef}>
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

            <div className={styles.hairColorRow}>
              <span className={styles.hairColorLabel}>Hårfarge</span>
              <div className={styles.hairColorButtons}>
                {HAIR_COLORS.map(({ name, color }) => (
                  <button
                    key={name}
                    onClick={() => setHairColor(name)}
                    style={{
                      width: '28px',
                      height: '28px',
                      borderRadius: '50%',
                      backgroundColor: color,
                      border: hairColor === name ? '3px solid #2563eb' : '2px solid transparent',
                      cursor: 'pointer',
                      boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
                    }}
                    title={name}
                    aria-label={`Velg hårfarge ${name}`}
                  />
                ))}
              </div>
            </div>

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
                onMouseDown={() => { nameInputMouseRef.current = true; }}
                onFocus={() => {
                  if (!nameInputMouseRef.current) setNameInputKeyboardFocused(true);
                  nameInputMouseRef.current = false;
                }}
                onBlur={() => setNameInputKeyboardFocused(false)}
                className={`${styles.nameInput}${nameInputKeyboardFocused ? ` ${styles.nameInputKeyboardFocused}` : ''}`}
                placeholder="Ditt detektivnavn"
                required
                minLength={3}
                maxLength={30}
              />
            </div>
            <div className={styles.usernameErrorSlot}>
              {usernameError && <p className={styles.usernameError}>{usernameError}</p>}
            </div>

            <button
              onClick={handleSave}
              ref={saveButtonRef}
              disabled={isSaving || username.trim().length < 3}
              className={styles.saveButton}
            >
              {isSaving ? "Lagrer..." : "Lagre antrekk"}
            </button>
          </div>
        </div>

        {/* Right column: wardrobe */}
        <div className={styles.wardrobeSection}>
          <h2 className={styles.sectionTitle}>Din garderobe</h2>
          <div className={styles.categoriesContainer}>
            {CATEGORY_ORDER.map((cat) => {
              const items = visibleInventory.filter(i => i.category === cat);
              if (items.length === 0) return null;
              return (
                <div key={cat} className={styles.categorySection}>
                  <h3 className={styles.categoryLabel}>{CATEGORY_LABELS[cat]}</h3>
                  <div className={styles.horizontalRow}>
                    {items.map((item) => {
                      if (item.locked) {
                        return (
                          <button
                            key={item.id}
                            disabled
                            aria-disabled="true"
                            title="Beseir bossen for å låse opp"
                            className={`${styles.itemButton} ${styles.itemButtonLocked}`}
                          >
                            <div className={styles.lockedOverlay}>
                              <span className={styles.lockedQuestionMark}>?</span>
                            </div>
                          </button>
                        );
                      }
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
              );
            })}
          </div>
        </div>

      </div>
    </div>
  );
}
