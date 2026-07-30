import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

export const fetchReminders = createAsyncThunk(
  'reminders/fetchReminders',
  async (deviceId, { rejectWithValue }) => {
    try {
      const response = await api.get(`/reminders${deviceId ? `?deviceId=${deviceId}` : ''}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch reminders');
    }
  }
);

export const createReminder = createAsyncThunk(
  'reminders/createReminder',
  async (reminderData, { rejectWithValue }) => {
    try {
      const response = await api.post('/reminders', reminderData);
      return response.data.reminder;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create reminder');
    }
  }
);

export const deleteReminder = createAsyncThunk(
  'reminders/deleteReminder',
  async (id, { rejectWithValue }) => {
    try {
      await api.delete(`/reminders/${id}`);
      return id;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete reminder');
    }
  }
);

const reminderSlice = createSlice({
  name: 'reminders',
  initialState: {
    items: [],
    loading: false,
    error: null
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchReminders.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchReminders.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchReminders.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(createReminder.fulfilled, (state, action) => {
        state.items.push(action.payload);
      })
      .addCase(deleteReminder.fulfilled, (state, action) => {
        state.items = state.items.filter(item => item._id !== action.payload);
      });
  }
});

export default reminderSlice.reducer;
