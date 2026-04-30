import { TeacherLayoutContext } from "@/components/teacher_layout/TeacherLayoutContext";
import { useContext, useEffect } from "react";

export function useBackTo(path: string) {
    const { setBackTo } = useContext(TeacherLayoutContext);
    useEffect(() => {
        setBackTo(path);
        return () => setBackTo(null);
    }, [path, setBackTo]);
}