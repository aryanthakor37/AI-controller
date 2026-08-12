import { io } from 'socket.io-client';
import { store } from '../redux/store';
import { setDevices, updateDeviceTelemetry } from '../redux/slices/deviceSlice';
import { getSocketUrl } from '../config';

class SocketService {
  constructor() {
    this.listeners = new Map();
  }

  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set());
    }
    this.listeners.get(event).add(callback);

    if (this.socket) {
      this.socket.on(event, callback);
    }
  }

  off(event, callback) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).delete(callback);
    }
    if (this.socket) {
      this.socket.off(event, callback);
    }
  }

  emit(event, payload) {
    if (this.socket) {
      this.socket.emit(event, payload);
    }
  }

  connect() {
    if (this.socket && this.socket.connected) return;

    const token = localStorage.getItem('token');
    const state = store.getState();
    const settings = state.settings || {};
    const autoReconnect = settings.autoReconnectSocket !== false;
    const debug = settings.debugLogging === true;

    this.socket = io(this.url, {
      auth: { token },
      query: { token },
      transports: ['websocket', 'polling'],
      reconnection: autoReconnect,
      reconnectionDelay: 1000,
      reconnectionDelayMax: 5000,
      reconnectionAttempts: autoReconnect ? Infinity : 0
    });

    // Re-attach registered listeners
    this.listeners.forEach((callbacks, event) => {
      callbacks.forEach((cb) => {
        this.socket.on(event, cb);
      });
    });

    this.socket.on('connect', () => {
      if (debug) console.log('[SocketService Debug] Connected to Backend Socket.IO Server:', this.socket.id);
      this.socket.emit('dashboard:request_devices');
    });

    this.socket.on('disconnect', (reason) => {
      if (debug) console.log(`[SocketService Debug] Disconnected: ${reason}`);
    });

    this.socket.on('dashboard:devices_update', (devices) => {
      if (debug) console.log('[SocketService Debug] Devices Update Payload:', devices);
      store.dispatch(setDevices(devices));
    });

    this.socket.on('dashboard:device_telemetry', (telemetryData) => {
      if (debug) console.log('[SocketService Debug] Telemetry Event:', telemetryData);
      store.dispatch(updateDeviceTelemetry(telemetryData));
    });

    this.socket.on('dashboard:command_result', (data) => {
      if (debug) console.log('[SocketService Debug] Command Result:', data);
      const { result } = data;
      const statusIcon = result.status === 'Success' ? '✅' : '❌';
      
      // Lazily import chatSlice to avoid circular dependencies if any
      import('../redux/slices/chatSlice').then(({ addMessage }) => {
        store.dispatch(addMessage({
          id: Date.now().toString() + Math.random().toString(36).substr(2, 5),
          role: 'ai',
          content: `📱 **Phone Response**\n${statusIcon} ${result.status} - ${result.message}`
        }));
      });
    });
  }

  sendCommand(deviceId, commandPayload) {
    if (this.socket) {
      const state = store.getState();
      const settings = state.settings || {};
      if (settings.debugLogging) console.log('[SocketService Debug] Transmitting Command Payload:', { deviceId, commandPayload });
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
