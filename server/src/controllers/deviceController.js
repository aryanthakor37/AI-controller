const Device = require('../models/Device');
const crypto = require('crypto');

// In-memory store for pairing codes (In production, use Redis)
// Format: { '123456': { userId: 'xxx', expiresAt: 'xxx' } }
const pairingCodes = {};

const generatePairingCode = async (req, res) => {
  try {
    const code = Math.floor(100000 + Math.random() * 900000).toString(); // 6 digit code
    pairingCodes[code] = {
      userId: req.user._id,
      expiresAt: Date.now() + 10 * 60 * 1000 // 10 minutes expiry
    };
    res.json({ pairingCode: code, message: 'Code expires in 10 minutes' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

const linkDevice = async (req, res) => {
  try {
    const { pairingCode, deviceId, deviceName, manufacturer, model, androidVersion } = req.body;

    const pairingData = pairingCodes[pairingCode];

    if (!pairingData || pairingData.expiresAt < Date.now()) {
      return res.status(400).json({ message: 'Invalid or expired pairing code' });
    }

    let device = await Device.findOne({ deviceId });

    if (device) {
      // Re-link to new owner if needed
      device.owner = pairingData.userId;
      device.deviceName = deviceName || device.deviceName;
      await device.save();
    } else {
      device = await Device.create({
        deviceId,
        deviceName,
        manufacturer,
        model,
        androidVersion,
        owner: pairingData.userId
      });
    }

    // Remove code after successful pairing
    delete pairingCodes[pairingCode];

    // Generate JWT for the device itself so it can authenticate
    const jwt = require('jsonwebtoken');
    const deviceToken = jwt.sign({ deviceId: device.deviceId, owner: device.owner }, process.env.JWT_SECRET || 'fallback_secret');

    res.status(200).json({ message: 'Device paired successfully', token: deviceToken, device });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

const getMyDevices = async (req, res) => {
  try {
    const devices = await Device.find({ owner: req.user._id });
    res.json(devices);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = { generatePairingCode, linkDevice, getMyDevices };
