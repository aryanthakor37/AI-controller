const mongoose = require('mongoose');

const backupSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  deviceId: {
    type: String,
    required: true
  },
  deviceName: {
    type: String,
    default: 'Android Device'
  },
  settingsPayload: {
    type: String, // Stringified JSON configuration payload
    required: true
  }
}, { timestamps: true });

// Ensure unique backup per user/device combination
backupSchema.index({ user: 1, deviceId: 1 }, { unique: true });

module.exports = mongoose.model('Backup', backupSchema);
