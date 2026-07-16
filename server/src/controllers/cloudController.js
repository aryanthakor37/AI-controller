const History = require('../models/History');
const Memory = require('../models/Memory');
const Backup = require('../models/Backup');
const Device = require('../models/Device');

// GET /api/history
const getHistory = async (req, res) => {
  try {
    const { deviceId, status, search } = req.query;
    const query = { user: req.user._id };
    
    if (deviceId) query.deviceId = deviceId;
    if (status) query.status = status;
    if (search) {
      query.$or = [
        { command: { $regex: search, $options: 'i' } },
        { intent: { $regex: search, $options: 'i' } }
      ];
    }

    const logs = await History.find(query).sort({ createdAt: -1 }).limit(100);
    res.json(logs);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// GET /api/analytics
const getAnalytics = async (req, res) => {
  try {
    const userId = req.user._id;

    // Command counts grouped by day, intent, or success
    const totalCommands = await History.countDocuments({ user: userId });
    const successCommands = await History.countDocuments({ user: userId, status: 'Success' });
    const failedCommands = await History.countDocuments({ user: userId, status: 'Failed' });

    // Aggregate most controlled apps & intents
    const popularIntents = await History.aggregate([
      { $match: { user: userId } },
      { $group: { _id: '$intent', count: { $sum: 1 } } },
      { $sort: { count: -1 } },
      { $limit: 5 }
    ]);

    // Average execution speed
    const speedStats = await History.aggregate([
      { $match: { user: userId, executionTimeMs: { $gt: 0 } } },
      { $group: { _id: null, avgSpeed: { $avg: '$executionTimeMs' } } }
    ]);

    const avgSpeed = speedStats.length > 0 ? Math.round(speedStats[0].avgSpeed) : 0;

    res.json({
      totalCommands,
      successRate: totalCommands > 0 ? Math.round((successCommands / totalCommands) * 100) : 0,
      failedCommands,
      avgSpeedMs: avgSpeed,
      popularIntents
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// POST /api/backup
const backupSettings = async (req, res) => {
  try {
    const { deviceId, deviceName, settingsPayload } = req.body;
    if (!deviceId || !settingsPayload) {
      return res.status(400).json({ message: 'Device ID and settings payload required' });
    }

    const backup = await Backup.findOneAndUpdate(
      { user: req.user._id, deviceId },
      { deviceName, settingsPayload },
      { new: true, upsert: true }
    );

    res.json({ message: 'Settings successfully backed up to cloud', backup });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// POST /api/restore
const restoreSettings = async (req, res) => {
  try {
    const { deviceId } = req.body;
    const backup = await Backup.findOne({ user: req.user._id, deviceId });
    if (!backup) {
      return res.status(404).json({ message: 'No backup found for this device' });
    }
    res.json(backup);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// POST /api/sync
const syncOfflineQueue = async (req, res) => {
  try {
    const { events } = req.body; // Array of offline events
    if (!Array.isArray(events)) {
      return res.status(400).json({ message: 'Events queue array required' });
    }

    const savedEvents = [];
    for (const event of events) {
      const saved = await History.create({
        user: req.user._id,
        deviceId: event.deviceId,
        deviceName: event.deviceName,
        command: event.command,
        intent: event.intent,
        status: event.status || 'Success',
        executionTimeMs: event.executionTimeMs || 0,
        errorMessage: event.errorMessage
      });
      savedEvents.push(saved);
    }

    res.json({ message: 'Offline sync completed', count: savedEvents.length });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// GET /api/device/all
const getAllDevices = async (req, res) => {
  try {
    const devices = await Device.find({ owner: req.user._id });
    res.json(devices);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  getHistory,
  getAnalytics,
  backupSettings,
  restoreSettings,
  syncOfflineQueue,
  getAllDevices
};
