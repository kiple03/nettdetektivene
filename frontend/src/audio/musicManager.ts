import backgroundMusicSrc from '@/assets/sounds/backgroundMusic.mp3';
import bossSoundtrackSrc from '@/assets/sounds/bossBattle.mp3';

const MUTE_KEY = 'musicMuted';

/** Available music tracks. */
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

/**
 * Starts playing the given track. Stops and resets the current track first if different.
 * Defers playback to the next user click if autoplay is blocked.
 */
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

/** Stops and resets the currently playing track. */
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

/** Returns the currently active track name, or `null` if nothing is playing. */
export function getCurrentTrack(): TrackName | null {
    return currentTrack;
}

/**
 * Mutes or unmutes all tracks and persists the preference to localStorage.
 * Notifies all mute subscribers.
 */
export function setMuted(value: boolean): void {
    muted = value;
    localStorage.setItem(MUTE_KEY, String(value));
    Object.values(audioByTrack).forEach(audio => {
        if (audio) audio.muted = value;
    });
    muteSubscribers.forEach(cb => cb(value));
}

/** Returns the current mute state. */
export function getMuted(): boolean {
    return muted;
}

/**
 * Subscribes to mute state changes.
 * @returns Unsubscribe function.
 */
export function subscribeMute(cb: (muted: boolean) => void): () => void {
    muteSubscribers.add(cb);
    return () => { muteSubscribers.delete(cb); };
}

// Vite HMR: when this module is disposed (file saved during dev), stop all audio
// elements held by the old instance so they don't keep playing alongside the new ones.
if (import.meta.hot) {
    import.meta.hot.dispose(() => {
        Object.values(audioByTrack).forEach(audio => {
            if (audio) {
                audio.pause();
                audio.src = '';
            }
        });
        muteSubscribers.clear();
    });
}
