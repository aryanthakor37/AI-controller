import { createSlice } from '@reduxjs/toolkit';

const DEFAULT_SETTINGS = {
  // AI Engine
  aiConfidenceThreshold: 0.7,
  aiContextWindow: 5,
  aiModel: 'gemini-flash-lite-latest',
  
  // Voice & Audio
  voiceLanguage: 'en-US',
  autoSubmitVoice: true,
  speechFeedbackRate: 1.0,
  isMuted: false,
  pitch: 1,
  rate: 1,
  volume: 1,
  language: 'en-US',

  // Appearance & Theme
  themeMode: 'dark',
  accentGlow: true,
  compactView: false,

  // System & Connection
  autoReconnectSocket: true,
  debugLogging: false,
};

const applySettingsToDOM = (settings) => {
  if (typeof document === 'undefined') return;
  const root = document.documentElement;

  // Apply themes
  root.classList.remove('theme-oled', 'theme-cyberpunk');
  if (settings.themeMode === 'oled') {
    root.classList.add('theme-oled');
  } else if (settings.themeMode === 'cyberpunk') {
    root.classList.add('theme-cyberpunk');
  }

  // Apply Glow
  if (settings.accentGlow === false) {
    root.classList.add('no-glow');
  } else {
    root.classList.remove('no-glow');
  }

  // Apply Compact View
  if (settings.compactView === true) {
    root.classList.add('compact-view');
  } else {
    root.classList.remove('compact-view');
  }
};

const loadInitialState = () => {
  if (typeof window === 'undefined') return DEFAULT_SETTINGS;
  try {
    const saved = localStorage.getItem('ai_agent_settings');
    if (saved) {
      const parsed = JSON.parse(saved);
      const merged = { ...DEFAULT_SETTINGS, ...parsed, language: parsed.voiceLanguage || 'en-US', rate: parsed.speechFeedbackRate || 1.0 };
      applySettingsToDOM(merged);
      return merged;
    }
  } catch (e) {
    console.error('Error reading settings from localStorage:', e);
  }
  applySettingsToDOM(DEFAULT_SETTINGS);
  return DEFAULT_SETTINGS;
};

const settingsSlice = createSlice({
  name: 'settings',
  initialState: loadInitialState(),
  reducers: {
    updateSettings: (state, action) => {
      const updated = { 
        ...state, 
        ...action.payload,
        language: action.payload.voiceLanguage || state.voiceLanguage || 'en-US',
        rate: action.payload.speechFeedbackRate || state.speechFeedbackRate || 1.0
      };
      if (typeof window !== 'undefined') {
        localStorage.setItem('ai_agent_settings', JSON.stringify(updated));
      }
      applySettingsToDOM(updated);
      return updated;
    },
    updateVoiceSettings: (state, action) => {
      const updated = { ...state, ...action.payload };
      if (typeof window !== 'undefined') {
        localStorage.setItem('ai_agent_settings', JSON.stringify(updated));
      }
      applySettingsToDOM(updated);
      return updated;
    },
    toggleMute: (state) => {
      state.isMuted = !state.isMuted;
      if (typeof window !== 'undefined') {
        localStorage.setItem('ai_agent_settings', JSON.stringify(state));
      }
    },
    resetSettings: () => {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('ai_agent_settings');
      }
      applySettingsToDOM(DEFAULT_SETTINGS);
      return DEFAULT_SETTINGS;
    }
  },
});

export const { updateSettings, updateVoiceSettings, toggleMute, resetSettings } = settingsSlice.actions;
export default settingsSlice.reducer;
