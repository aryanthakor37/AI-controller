class ConnectionManager {
  constructor() {
    this.devices = new Map(); // socketId -> device data
  }

  registerDevice(socketId, deviceData) {
    // Remove any existing socket for this same deviceId to prevent ghost devices
    for (const [existingSocketId, device] of this.devices.entries()) {
      if (device.deviceId === deviceData.deviceId) {
        this.devices.delete(existingSocketId);
      }
    }

    this.devices.set(socketId, {
      ...deviceData,
      socketId,
      connectedAt: Date.now(),
      lastSeen: Date.now(),
      latency: 0
    });
    console.log(`[ConnectionManager] Device registered: ${deviceData.deviceId} (${socketId})`);
  }

  removeDevice(socketId) {
    const device = this.devices.get(socketId);
    if (device) {
      this.devices.delete(socketId);
      console.log(`[ConnectionManager] Device disconnected: ${device.deviceName} (${socketId})`);
    }
    return device;
  }

  updateHeartbeat(socketId, latency) {
    const device = this.devices.get(socketId);
    if (device) {
      device.lastSeen = Date.now();
      device.latency = latency;
      this.devices.set(socketId, device);
    }
  }

  getDevice(socketId) {
    return this.devices.get(socketId);
  }

  getAllDevices() {
    return Array.from(this.devices.values());
  }
}

module.exports = new ConnectionManager();
