import { useRef } from "react";

interface Props {
    onComplete: () => void;
}

export function VictoryCutscene({ onComplete }: Props) {
    // Stub animation sequence:
    // 1. Player avatar flies across screen toward boss
    // 2. Collision effect — boss flies off-screen to the right
    // 3. onComplete

    const called = useRef(false);
    if (!called.current) {
        called.current = true;
        setTimeout(onComplete, 0);
    }
    return null;
}
