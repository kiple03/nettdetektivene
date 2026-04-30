import { createContext, useContext, useEffect } from 'react';

type TeacherLayoutContextType = {
  setHideBack: (hide: boolean) => void;
};

export const TeacherLayoutContext = createContext<TeacherLayoutContextType>({
  setHideBack: () => {},
});

export function useHideBackButton(active = true) {
  const { setHideBack } = useContext(TeacherLayoutContext);
  useEffect(() => {
    if (active) setHideBack(true);
    return () => setHideBack(false);
  }, [active, setHideBack]);
}
