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
            <LayeredAvatar items={equipped} size={256}/>
          </div>
          <p className={styles.equippedText}>Trying on: {equipped.length} items</p>

          <div className="mt-4">
            <label className="block font-semibold mb-1">Detective Name</label>
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
            {isSaving ? "Lagre..." : "Lagre antrekk"}
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