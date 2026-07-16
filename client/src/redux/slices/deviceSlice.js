import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  activeDevices: [], // Array of device objects registered via Socket.IO
};

export const deviceSlice = createSlice({
  name: 'device',
  initialState,
  reducers: {
    setDevices: (state, action) => {
      state.activeDevices = action.payload;
    },
    updateDeviceTelemetry: (state, action) => {
      const { socketId, latency, lastSeen } = action.payload;
      const device = state.activeDevices.find(d => d.socketId === socketId);
      if (device) {
        device.latency = latency;
        device.lastSeen = lastSeen;
      }
    },
  },
});

export const { setDevices, updateDeviceTelemetry } = deviceSlice.actions;
export default deviceSlice.reducer;
