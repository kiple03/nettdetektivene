import { useAuth } from "@/hooks/useAuth";
import { useEffect, useState } from "react";
import { playMusic, stopMusic, getMuted, setMuted as setMutedManager, subscribeMute, getCurrentTrack } from "@/audio/musicManager";
import styles from "./MusicMuteButton.module.css";

/**
 * Floating mute/unmute button for background music, visible only to logged-in students.
 * Starts background music on mount if no track is already playing, and stops it on logout.
 */
export default function BackgroundMusic() {
    const { isLoggedIn, role } = useAuth();
    const [muted, setMutedState] = useState(getMuted);

    useEffect(() => {
        return subscribeMute(setMutedState);
    }, []);

    useEffect(() => {
        if (isLoggedIn && role === 'STUDENT') {
            // Only claim playback if no other page (e.g. boss battle) already owns it
            if (getCurrentTrack() === null) {
                playMusic('background');
            }
        } else {
            stopMusic();
        }
    }, [isLoggedIn, role]);

    if (!isLoggedIn || role !== 'STUDENT') return null;

    const handleToggle = () => {
        setMutedManager(!muted);
    };

    return (
        <button onClick={handleToggle}
                className={styles.button}
                aria-label={muted ? 'Slå på musikk' : 'Slå av musikk'}>
            {muted ? '🔇' : '🔊'}
        </button>
    );
}
