import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  history: [], // Array of { role: 'user' | 'ai', text: string, intent?: string }
};

const conversationSlice = createSlice({
  name: 'conversation',
  initialState,
  reducers: {
    addMessage: (state, action) => {
      state.history.push(action.payload);
    },
    clearHistory: (state) => {
      state.history = [];
    }
  },
});

export const { addMessage, clearHistory } = conversationSlice.actions;
export default conversationSlice.reducer;
