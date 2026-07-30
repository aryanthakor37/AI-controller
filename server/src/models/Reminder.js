const mongoose = require('mongoose');

const reminderSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: false
  },
  deviceId: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  date: {
    type: String, // YYYY-MM-DD
    required: true
  },
  time: {
    type: String, // HH:MM
    required: true
  },
  repeat: {
    type: String,
    enum: ['NONE', 'DAILY', 'WEEKLY', 'YEARLY'],
    default: 'NONE'
  },
  contact: {
    type: String,
    default: ''
  },
  status: {
    type: String,
    enum: ['ACTIVE', 'COMPLETED', 'CANCELLED'],
    default: 'ACTIVE'
  }
}, { timestamps: true });

reminderSchema.index({ deviceId: 1, date: 1 });

module.exports = mongoose.model('Reminder', reminderSchema);
