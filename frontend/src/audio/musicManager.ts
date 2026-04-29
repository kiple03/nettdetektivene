import backgroundMusicSrc from '@/assets/sounds/backgroundMusic.mp3';
import bossSoundtrackSrc from '@/assets/sounds/bossBattle.mp3';

const MUTE_KEY = 'musicMuted';

export type TrackName = 'background' | 'boss';

interface TrackConfig {
    src: string;
    volume: number;
}

const TRACKS: Record<TrackName, TrackConfig> = {
    background: { src: backgroundMusicSrc, volume: 0.4 },
    boss: { src: bossSoundtrackSrc, volume: 0.7 },
};

const audioByTrack: Partial<Record<TrackName, HTMLAudioElement>> = {};
let currentTrack: TrackName | null = null;
let muted = localStorage.getItem(MUTE_KEY) === 'true';
const muteSubscribers = new Set<(muted: boolean) => void>();

function getOrCreateAudio(name: TrackName): HTMLAudioElement {
    let audio = audioByTrack[name];
    if (!audio) {
        const cfg = TRACKS[name];
        audio = new Audio(cfg.src);
        audio.loop = true;
        audio.volume = cfg.volume;
        audio.muted = muted;
        audioByTrack[name] = audio;
    }
    return audio;
}

function tryPlay(audio: HTMLAudioElement, name: TrackName): void {
    audio.play().catch(() => {
        const resume = () => {
            if (currentTrack === name && audioByTrack[name] === audio) {
                audio.play().catch(() => {});
            }
        };
        document.addEventListener('click', resume, { once: true });
    });
}

export function playMusic(name: TrackName): void {
    if (currentTrack === name) {
        const audio = getOrCreateAudio(name);
        if (audio.paused) tryPlay(audio, name);
        return;
    }
    if (currentTrack) {
        const prev = audioByTrack[currentTrack];
        if (prev) {
            prev.pause();
            prev.currentTime = 0;
        }
    }
    currentTrack = name;
    const audio = getOrCreateAudio(name);
    audio.currentTime = 0;
    audio.muted = muted;
    tryPlay(audio, name);
}

export function stopMusic(): void {
    if (currentTrack) {
        const audio = audioByTrack[currentTrack];
        if (audio) {
            audio.pause();
            audio.currentTime = 0;
        }
        currentTrack = null;
    }
}

export function getCurrentTrack(): TrackName | null {
    return currentTrack;
}

export function setMuted(value: boolean): void {
    muted = value;
    localStorage.setItem(MUTE_KEY, String(value));
    Object.values(audioByTrack).forEach(audio => {
        if (audio) audio.muted = value;
    });
    muteSubscribers.forEach(cb => cb(value));
}

export function getMuted(): boolean {
    return muted;
}

export function subscribeMute(cb: (muted: boolean) => void): () => void {
    muteSubscribers.add(cb);
    return () => { muteSubscribers.delete(cb); };
}
