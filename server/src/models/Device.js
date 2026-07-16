const mongoose = require('mongoose');

const deviceSchema = new mongoose.Schema({
  deviceId: {
    type: String,
    required: true,
    unique: true
  },
  deviceName: {
    type: String,
    default: 'Unknown Device'
  },
  manufacturer: {
    type: String,
    default: 'Unknown'
  },
  model: {
    type: String,
    default: 'Unknown'
  },
  androidVersion: {
    type: String,
    default: 'Unknown'
  },
  socketId: {
    type: String,
    default: null
  },
  battery: {
    type: Number,
    default: 0
  },
  networkType: {
    type: String,
    default: 'UNKNOWN'
  },
  lastSeen: {
    type: Date,
    default: Date.now
  },
  isOnline: {
    type: Boolean,
    default: false
  },
  owner: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  }
}, { timestamps: true });

module.exports = mongoose.model('Device', deviceSchema);
