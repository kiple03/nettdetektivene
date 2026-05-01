import { createContext, useContext, useEffect } from 'react';

type TeacherLayoutContextType = {
  setHideBack: (hide: boolean) => void;
    setBackTo: (path: string | null) => void;
    setBackLabel: (label: string | null) => void;
};

/** Context for controlling teacher layout UI state, such as back-button visibility. */
export const TeacherLayoutContext = createContext<TeacherLayoutContextType>({
  setHideBack: () => {},
  setBackTo: () => {},
  setBackLabel: () => {},
});

/**
 * Hides the back button in the teacher layout while the calling component is mounted.
 * @param active - When false, the hook has no effect. Defaults to `true`.
 */
export function useHideBackButton(active = true) {
  const { setHideBack } = useContext(TeacherLayoutContext);
  useEffect(() => {
    if (active) setHideBack(true);
    return () => setHideBack(false);
  }, [active, setHideBack]);
}