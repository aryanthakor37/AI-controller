const mongoose = require('mongoose');

const actionItemSchema = new mongoose.Schema({
  intent: {
    type: String,
    required: true
  },
  args: {
    type: Object,
    default: {}
  },
  delayMs: {
    type: Number,
    default: 500
  }
}, { _id: false });

const routineSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: false
  },
  title: {
    type: String,
    required: true
  },
  triggerPhrase: {
    type: String,
    required: true,
    lowercase: true,
    trim: true
  },
  description: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: 'Zap'
  },
  category: {
    type: String,
    enum: ['ROUTINE', 'EMERGENCY', 'FOCUS', 'TRAVEL'],
    default: 'ROUTINE'
  },
  isSystemDefault: {
    type: Boolean,
    default: false
  },
  actions: [actionItemSchema]
}, { timestamps: true });

routineSchema.index({ triggerPhrase: 1 });

module.exports = mongoose.model('Routine', routineSchema);
