import { useUserProfile } from "@/hooks/useUserProfile";

export function Header(){
    const {data: userProfile, isLoading, isError} = useUserProfile();

    if(isLoading) return <div>Loading...</div>;
    if(isError) return <div>Error upon loading username and avatar</div>;
    return (
        <div>
            <img src={userProfile?.avatar} alt="Avatar picture" />
            <span>{userProfile?.username}</span>
        </div>
    );
}