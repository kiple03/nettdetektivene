import { useUserProfile } from "@/hooks/useUserProfile";
import { LayeredAvatar } from "./LayeredAvatar";

export function Header(){
    const {data: userProfile, isLoading, isError} = useUserProfile();

    if(isLoading) return <div>Loading...</div>;
    if(isError) return <div>Error upon loading username and avatar</div>;
    
    return (
        <div>
            <LayeredAvatar items={userProfile?.equippedItems || []} size={120}/>
            <span>{userProfile?.username}</span>
        </div>
    );
}