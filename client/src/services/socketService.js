import { io } from 'socket.io-client';
import { store } from '../redux/store';
import { setDevices, updateDeviceTelemetry } from '../redux/slices/deviceSlice';
import { getSocketUrl } from '../config';

class SocketService {
  constructor() {
    this.socket = null;
    this.url = getSocketUrl();
  }

  connect() {
    if (this.socket && this.socket.connected) return;

    this.socket = io(this.url, {
      transports: ['websocket', 'polling'],
      reconnection: true,
      reconnectionDelay: 1000,
      reconnectionDelayMax: 5000,
      reconnectionAttempts: Infinity
    });

    this.socket.on('connect', () => {
      console.log('[SocketService] Connected to Backend Socket.IO Server');
      this.socket.emit('dashboard:request_devices');
    });

    this.socket.on('disconnect', (reason) => {
      console.log(`[SocketService] Disconnected: ${reason}`);
    });

    // Listen for full device list updates (Registrations / Disconnects)
    this.socket.on('dashboard:devices_update', (devices) => {
      store.dispatch(setDevices(devices));
    });

    // Listen for high-frequency telemetry (Ping / Heartbeat updates)
    this.socket.on('dashboard:device_telemetry', (telemetryData) => {
      store.dispatch(updateDeviceTelemetry(telemetryData));
    });
  }

  sendCommand(deviceId, commandPayload) {
    if (this.socket) {
      this.socket.emit('dashboard:send_command', { deviceId, command: commandPayload });
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }
}

export default new SocketService();
