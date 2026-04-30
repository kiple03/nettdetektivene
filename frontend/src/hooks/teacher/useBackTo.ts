import { TeacherLayoutContext } from "@/components/teacher_layout/TeacherLayoutContext";
import { useContext, useEffect } from "react";

export function useBackTo(path: string, label?: string) {
    const { setBackTo, setBackLabel } = useContext(TeacherLayoutContext);
    useEffect(() => {
        setBackTo(path);
        setBackLabel(label ?? null);
        return () => {
            setBackTo(null);
            setBackLabel(null);
        };
    }, [path, label, setBackTo, setBackLabel]);
}