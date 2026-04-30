import { useRef } from "react";

interface Props {
    onComplete: () => void;
}

export function VictoryCutscene({ onComplete }: Props) {
    const called = useRef(false);
    if (!called.current) {
        called.current = true;
        setTimeout(onComplete, 5000);
    }
    return null;
}
