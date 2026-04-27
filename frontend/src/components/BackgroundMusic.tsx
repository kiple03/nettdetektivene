import { useAuth } from "@/hooks/useAuth";
import { useEffect, useRef, useState } from "react";
import backgroundMusic from '@/assets/sounds/backgroundMusic.mp3'

const MUTE_KEY = 'musicMuted';

export default function BackgroundMusic(){
    const {isLoggedIn, role} = useAuth();
    const audioRef = useRef<HTMLAudioElement | null>(null);
    const [muted, setMuted] = useState(() => localStorage.getItem(MUTE_KEY) === 'true');

    useEffect(() => {
        const audio = new Audio(backgroundMusic);
        audio.loop = true;
        audio.volume = 0.4;
        audio.muted = muted;
        audioRef.current = audio;
        return () => {audio.pause(); audioRef.current = null;};
    }, []);

    useEffect(() => {
        const audio = audioRef.current;
        if(!audio) return;
        if(isLoggedIn && role === 'STUDENT'){
            audio.play().catch(() => {
                const resume = () => audio.play().catch(() => {});
                document.addEventListener('click', resume, {once: true});
            });
        } else{
            audio.pause();
        }
    }, [isLoggedIn, role]);

    useEffect(() => {
        if(audioRef.current) audioRef.current.muted = muted;
        localStorage.setItem(MUTE_KEY, String(muted));
    }, [muted]);

    if(!isLoggedIn || role !== 'STUDENT') return null;

    return (
        <button onClick={() => setMuted(m => !m)} 
                style={{position: 'fixed', bottom: '1rem', right: '1rem', zIndex: 9999}}
                aria-label={muted ? 'Slå på musikk' : 'Slå av musikk'}>
            {muted ? '🔇' : '🔊'}
        </button>
    );
}