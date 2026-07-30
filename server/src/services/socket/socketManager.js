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
    socket.on('dashboard:send_command', ({ deviceId, command }) => {
      logToFile(`[SocketManager] Sending command to ${deviceId}: ${JSON.stringify(command)}`);
      // Forward command to ALL devices (to test if socketId routing is the issue)
      ioInstance.emit('command:execute', command);
    });

    // Handle dashboard requesting device list on connect
    socket.on('dashboard:request_devices', () => {
      logToFile(`[SocketManager] dashboard:request_devices from ${socket.id}`);
      socket.emit('dashboard:devices_update', connectionManager.getAllDevices());
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
