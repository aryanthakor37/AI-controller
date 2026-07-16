import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

// Async thunk to fetch command history from the backend
export const fetchHistory = createAsyncThunk('commands/fetchHistory', async (_, thunkAPI) => {
  try {
    const response = await api.get('/history');
    return response.data;
  } catch (error) {
    const message = error.response?.data?.message || error.message;
    return thunkAPI.rejectWithValue(message);
  }
});

const initialState = {
  history: [],
  loading: false,
  error: null,
};

export const commandSlice = createSlice({
  name: 'commands',
  initialState,
  reducers: {
    addCommand: (state, action) => {
      state.history.unshift(action.payload);
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchHistory.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchHistory.fulfilled, (state, action) => {
        state.loading = false;
        state.history = action.payload;
      })
      .addCase(fetchHistory.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { addCommand } = commandSlice.actions;
export default commandSlice.reducer;

