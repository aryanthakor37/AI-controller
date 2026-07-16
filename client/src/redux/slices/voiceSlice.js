import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  status: 'idle', // idle, listening, processing, speaking, error
  error: null,
};

const voiceSlice = createSlice({
  name: 'voice',
  initialState,
  reducers: {
    setVoiceStatus: (state, action) => {
      state.status = action.payload;
      if (action.payload !== 'error') {
        state.error = null;
      }
    },
    setVoiceError: (state, action) => {
      state.status = 'error';
      state.error = action.payload;
    },
  },
});

export const { setVoiceStatus, setVoiceError } = voiceSlice.actions;
export default voiceSlice.reducer;
