const mongoose = require('mongoose');
const History = require('./src/models/History');
require('dotenv').config();

mongoose.connect(process.env.MONGODB_URI || 'mongodb+srv://aryan:12345@cluster0.tcwnh4c.mongodb.net/AI-phone')
  .then(async () => {
    const history = await History.find().sort({ createdAt: -1 }).limit(10);
    history.forEach(h => {
        console.log(`[${h.createdAt}] Command: "${h.command}" -> Intent: ${h.intent} (${h.status})`);
    });
    process.exit(0);
  })
  .catch(err => {
    console.error(err);
    process.exit(1);
  });
