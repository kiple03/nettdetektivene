import { useEffect, useRef } from "react";

interface Props {
    onComplete: () => void;
}

/** Stub cutscene played after the boss is defeated; immediately calls {@link Props.onComplete}. */
export function DefeatCutscene({ onComplete }: Props) {
    // Stub animation sequence:
    // 1. Boss disappears (fade out)
    // 2. Boss slowly rises back up from below the screen
    // 3. onComplete

    const called = useRef(false);
    useEffect(() => {
        if (!called.current) {
            called.current = true;
            setTimeout(onComplete, 0);
        }
    }, [onComplete]);
    return null;
}
