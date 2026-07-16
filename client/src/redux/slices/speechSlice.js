import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  interimTranscript: '',
  finalTranscript: '',
};

const speechSlice = createSlice({
  name: 'speech',
  initialState,
  reducers: {
    setInterimTranscript: (state, action) => {
      state.interimTranscript = action.payload;
    },
    setFinalTranscript: (state, action) => {
      state.finalTranscript = action.payload;
    },
    clearTranscripts: (state) => {
      state.interimTranscript = '';
      state.finalTranscript = '';
    }
  },
});

export const { setInterimTranscript, setFinalTranscript, clearTranscripts } = speechSlice.actions;
export default speechSlice.reducer;
