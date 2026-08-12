const { Server } = require('socket.io');
const connectionManager = require('./ConnectionManager');

const fs = require('fs');

const logToFile = (msg) => {
  fs.appendFileSync('socket_debug.log', `[${new Date().toISOString()}] ${msg}\n`);
  console.log(msg);
};

const jwt = require('jsonwebtoken');

let ioInstance;

const initSocket = (httpServer) => {
  ioInstance = new Server(httpServer, {
    cors: { origin: process.env.CLIENT_URL || '*' },
    allowEIO3: true,
    pingTimeout: 120000,
    pingInterval: 25000
  });

  ioInstance.use((socket, next) => {
    const token = socket.handshake.auth?.token || socket.handshake.query?.token;
    if (token) {
      try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'fallback_secret');
        socket.userId = decoded.id || decoded._id || decoded.owner;
      } catch (err) {
        logToFile(`[SocketManager] Auth token verification failed for ${socket.id}: ${err.message}`);
      }
    }
    // Proceed to allow connection
    next();
  });

  ioInstance.on('connection', (socket) => {
    logToFile(`[SocketManager] New connection: ${socket.id} (user: ${socket.userId || 'guest'})`);

    // Immediately send current connected devices list to newly connected sockets
    socket.emit('dashboard:devices_update', connectionManager.getAllDevices());

    // Device Registration
    socket.on('device:register', (deviceData) => {
      if (socket.userId && !deviceData.owner) {
        deviceData.owner = socket.userId;
      }
      logToFile(`[SocketManager] device:register from ${socket.id} - ${JSON.stringify(deviceData)}`);
      connectionManager.registerDevice(socket.id, deviceData);
      socket.emit('device:connected', { status: 'success', message: 'Device registered successfully' });
      
      // Notify Dashboard about the updated device list
      ioInstance.emit('dashboard:devices_update', connectionManager.getAllDevices());
    });

    // Heartbeat logic
    socket.on('device:heartbeat', (data) => {
      const latency = Date.now() - data.timestamp;
      connectionManager.updateHeartbeat(socket.id, latency);
      logToFile(`[SocketManager] heartbeat from ${socket.id}, latency: ${latency}`);
      
      // Send real-time telemetry to Dashboard
      ioInstance.emit('dashboard:device_telemetry', {
        socketId: socket.id,
        latency,
        lastSeen: Date.now()
      });
    });

    // Handle commands from Dashboard
    socket.on('device:screenshot_result', (data) => {
      // Forward the screenshot result to all connected dashboards
      ioInstance.emit('dashboard:screenshot_result', {
        socketId: socket.id,
        image: data.image
      });
    });

    socket.on('device:screen_frame', (data) => {
      // Forward the live screen frame to all connected dashboards
      ioInstance.emit('dashboard:screen_frame', {
        socketId: socket.id,
        frame: data.frame
      });
    });

    socket.on('device:screen_frame_error', (data) => {
      ioInstance.emit('dashboard:screen_frame_error', {
        socketId: socket.id,
        error: data.error
      });
    });

    // Handle commands from Dashboard
    socket.on('dashboard:send_command', ({ deviceId, command }) => {
      logToFile(`[SocketManager] Sending command to ${deviceId}: ${JSON.stringify(command)}`);
      if (deviceId === 'all') {
        const devices = connectionManager.getAllDevices();
        devices.forEach(device => {
          ioInstance.to(device.socketId).emit('command:execute', command);
        });
      } else {
        // Forward command to the specific device's socket
        ioInstance.to(deviceId).emit('command:execute', command);
      }
    });

    socket.on('dashboard:perform_gesture', ({ socketId, gesture }) => {
      if (socketId) {
        logToFile(`[SocketManager] Forwarding gesture to ${socketId}`);
        ioInstance.to(socketId).emit('device:perform_gesture', gesture);
      }
    });

    socket.on('dashboard:inject_text', ({ socketId, text }) => {
      if (socketId) {
        // Minimal logging to avoid clutter for every keystroke
        ioInstance.to(socketId).emit('device:inject_text', { text });
      }
    });

    // Two-way Clipboard Synchronization Events
    socket.on('dashboard:sync_clipboard', ({ socketId, text }) => {
      if (socketId) {
        logToFile(`[SocketManager] Syncing PC clipboard to device ${socketId}`);
        ioInstance.to(socketId).emit('device:sync_clipboard', { text });
      }
    });

    socket.on('device:clipboard_changed', (data) => {
      logToFile(`[SocketManager] Device clipboard changed from ${socket.id}`);
      ioInstance.emit('dashboard:clipboard_changed', {
        socketId: socket.id,
        text: data.text
      });
    });

    // Handle dashboard requesting device list on connect
    socket.on('dashboard:request_devices', () => {
      logToFile(`[SocketManager] dashboard:request_devices from ${socket.id}`);
      socket.emit('dashboard:devices_update', connectionManager.getAllDevices());
    });

    // Handle command results coming back from the Android device
    socket.on('command:result', (result) => {
      logToFile(`[SocketManager] command:result from ${socket.id}: ${JSON.stringify(result)}`);
      // Broadcast this result to the dashboard so the user sees what the phone actually did
      ioInstance.emit('dashboard:command_result', { deviceId: socket.id, result });
    });

    socket.on('disconnect', (reason) => {
      logToFile(`[SocketManager] Disconnect: ${socket.id}, reason: ${reason}`);
      const device = connectionManager.removeDevice(socket.id);
      if (device) {
        // Notify Dashboard
        ioInstance.emit('dashboard:devices_update', connectionManager.getAllDevices());
      }
    });
  });

  // Start the 15-second heartbeat ping loop
  setInterval(() => {
    const devices = connectionManager.getAllDevices();
    devices.forEach((device) => {
      // We ping the devices. The devices should immediately respond with `device:heartbeat`
      ioInstance.to(device.socketId).emit('device:ping', { timestamp: Date.now() });
    });
  }, 15000);

  return ioInstance;
};

const getIo = () => {
  if (!ioInstance) {
    throw new Error('Socket.io not initialized!');
  }
  return ioInstance;
};

module.exports = {
  initSocket,
  getIo
};
