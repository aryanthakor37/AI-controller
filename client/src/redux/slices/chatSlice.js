import { createSlice } from '@reduxjs/toolkit';
import { mockChatHistory } from '../../utils/mockData';

const initialState = {
  messages: mockChatHistory,
  isTyping: false,
};

export const chatSlice = createSlice({
  name: 'chat',
  initialState,
  reducers: {
    addMessage: (state, action) => {
      state.messages.push(action.payload);
    },
    setTyping: (state, action) => {
      state.isTyping = action.payload;
    },
  },
});

export const { addMessage, setTyping } = chatSlice.actions;
export default chatSlice.reducer;
