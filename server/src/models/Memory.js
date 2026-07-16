const mongoose = require('mongoose');

const memorySchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
    unique: true
  },
  frequentCommands: {
    type: Map,
    of: Number,
    default: {}
  },
  favoriteApps: [{
    type: String
  }],
  frequentContacts: [{
    type: String
  }],
  preferences: {
    type: Map,
    of: String,
    default: {}
  }
}, { timestamps: true });

module.exports = mongoose.model('Memory', memorySchema);
