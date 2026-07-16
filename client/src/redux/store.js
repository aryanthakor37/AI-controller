import { configureStore } from '@reduxjs/toolkit';
import themeReducer from './slices/themeSlice';
import userReducer from './slices/userSlice';
import deviceReducer from './slices/deviceSlice';
import chatReducer from './slices/chatSlice';
import commandReducer from './slices/commandSlice';
import voiceReducer from './slices/voiceSlice';
import speechReducer from './slices/speechSlice';
import conversationReducer from './slices/conversationSlice';
import settingsReducer from './slices/settingsSlice';

export const store = configureStore({
  reducer: {
    theme: themeReducer,
    user: userReducer,
    device: deviceReducer,
    chat: chatReducer,
    commands: commandReducer,
    voice: voiceReducer,
    speech: speechReducer,
    conversation: conversationReducer,
    settings: settingsReducer,
  },
  devTools: process.env.NODE_ENV !== 'production',
});
