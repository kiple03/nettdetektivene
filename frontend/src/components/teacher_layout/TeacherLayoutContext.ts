import { createContext, useContext, useEffect } from 'react';

type TeacherLayoutContextType = {
  setHideBack: (hide: boolean) => void;
    setBackTo: (path: string | null) => void;
};

export const TeacherLayoutContext = createContext<TeacherLayoutContextType>({
  setHideBack: () => {},
  setBackTo: () => {},
});

export function useHideBackButton(active = true) {
  const { setHideBack } = useContext(TeacherLayoutContext);
  useEffect(() => {
    if (active) setHideBack(true);
    return () => setHideBack(false);
  }, [active, setHideBack]);
}