import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  isMuted: false,
  language: 'en-US',
  pitch: 1,
  rate: 1,
  volume: 1,
};

const settingsSlice = createSlice({
  name: 'settings',
  initialState,
  reducers: {
    toggleMute: (state) => {
      state.isMuted = !state.isMuted;
    },
    updateVoiceSettings: (state, action) => {
      return { ...state, ...action.payload };
    }
  },
});

export const { toggleMute, updateVoiceSettings } = settingsSlice.actions;
export default settingsSlice.reducer;
