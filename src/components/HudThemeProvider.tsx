import { createContext, useContext, useState, ReactNode, useEffect } from 'react';

import { settingsRepo } from '@/db/repositories/settingsRepo';
import { hudThemes, HudThemeId, HudColors } from '@/theme/hudTheme';

interface HudThemeContextType {
  id: HudThemeId;
  colors: HudColors;
  setTheme: (id: HudThemeId) => void;
}

const HudThemeContext = createContext<HudThemeContextType | undefined>(undefined);

export function HudThemeProvider({ children }: { children: ReactNode }) {
  const [themeId, setThemeIdState] = useState<HudThemeId>('violet');
  const [isLoaded, setIsLoaded] = useState(false);
  
  useEffect(() => {
    const loadTheme = () => {
      const storedTheme = settingsRepo.get('hudTheme') as HudThemeId | null;
      if (storedTheme && ['violet', 'emerald', 'crimson'].includes(storedTheme)) {
        setThemeIdState(storedTheme);
      }
      setIsLoaded(true);
    };
    
    loadTheme();
  }, []);
  
  const setTheme = (id: HudThemeId) => {
    settingsRepo.set('hudTheme', id);
    setThemeIdState(id);
  };
  
  if (!isLoaded) {
    return null;
  }
  
  const colors = hudThemes[themeId].colors;
  
  return (
    <HudThemeContext.Provider value={{ id: themeId, colors, setTheme }}>
      {children}
    </HudThemeContext.Provider>
  );
}

export function useHudTheme() {
  const context = useContext(HudThemeContext);
  if (context === undefined) {
    throw new Error('useHudTheme must be used within a HudThemeProvider');
  }
  return context;
}
