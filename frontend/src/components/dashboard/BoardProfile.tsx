import { useUserProfile } from "@/hooks/useUserProfile";
import { LayeredAvatar } from "../miscellaneous/LayeredAvatar";

export function Header(){
    const {data: userProfile, isLoading, isError} = useUserProfile();

    if(isLoading) return <div>Loading...</div>;
    if(isError) return <div>Error upon loading username and avatar</div>;
    
    return (
        <div>
            <LayeredAvatar items={userProfile?.equippedItems || []} size={"14vw"} hairColor={userProfile?.hairColor}/>
            <span style={{ fontWeight: 'bold', fontSize: '1.1rem', color: '#1e293b', backgroundColor: '#f6f6f6', opacity: 0.8 }}>{userProfile?.username}</span>
        </div>
    );
}