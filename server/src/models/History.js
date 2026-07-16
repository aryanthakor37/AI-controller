const mongoose = require('mongoose');

const historySchema = new mongoose.Schema({
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
  command: {
    type: String,
    required: true
  },
  intent: {
    type: String,
    default: 'UNKNOWN_COMMAND'
  },
  status: {
    type: String,
    enum: ['Success', 'Failed', 'Pending'],
    default: 'Success'
  },
  executionTimeMs: {
    type: Number,
    default: 0
  },
  errorMessage: {
    type: String,
    default: null
  }
}, { timestamps: true });

historySchema.index({ user: 1, createdAt: -1 });

module.exports = mongoose.model('History', historySchema);
