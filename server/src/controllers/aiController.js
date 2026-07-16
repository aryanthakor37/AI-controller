const { processCommand } = require('../services/ai/geminiService');
const { addMessage, getContext, clearSession } = require('../services/ai/contextManager');
const { logAiError } = require('../utils/aiLogger');
const History = require('../models/History');
const Memory = require('../models/Memory');

// Generate a random session ID if not provided by client
const getOrCreateSession = (req) => {
  let sessionId = req.headers['x-session-id'];
  if (!sessionId) {
    sessionId = 'session_' + Math.random().toString(36).substring(7);
  }
  return sessionId;
};

const parseCommand = async (req, res) => {
  const { command } = req.body;
  if (!command) {
    return res.status(400).json({ error: 'Command is required' });
  }

  const sessionId = getOrCreateSession(req);
  const startTime = Date.now();

  try {
    const aiResult = await processCommand(command, sessionId);
    const executionTimeMs = Date.now() - startTime;

    // Save conversation context
    addMessage(sessionId, 'user', command);
    if (aiResult.success && aiResult.data.intent === 'GENERAL_CHAT') {
      addMessage(sessionId, 'model', aiResult.data.reply || '...');
    } else {
      addMessage(sessionId, 'model', `[Intent Executed: ${aiResult.data.intent}]`);
    }

    // Save history logs and update memory stats in MongoDB
    if (req.user) {
      const intent = aiResult.data?.intent || 'UNKNOWN_COMMAND';
      const status = aiResult.success ? 'Success' : 'Failed';
      
      // Log to History
      await History.create({
        user: req.user._id,
        deviceId: req.headers['x-device-id'] || 'device_id_unknown',
        deviceName: req.headers['x-device-name'] || 'Android Device',
        command,
        intent,
        status,
        executionTimeMs,
        errorMessage: aiResult.success ? null : aiResult.error
      });

      // Update AI Memory
      try {
        const memory = await Memory.findOne({ user: req.user._id }) || new Memory({ user: req.user._id });
        const freq = memory.frequentCommands.get(intent) || 0;
        memory.frequentCommands.set(intent, freq + 1);
        await memory.save();
      } catch (err) {
        console.error('Failed to update AI Memory:', err.message);
      }
    }

    // Check if processCommand failed internally
    if (!aiResult.success) {
      logAiError('processCommand returned success: false', aiResult.error);
      return res.status(502).json({
        success: false,
        error: aiResult.error || 'Bad Gateway: AI Engine failed to process command',
        data: aiResult.data
      });
    }

    res.json({
      sessionId,
      success: true,
      data: aiResult.data
    });
  } catch (error) {
    logAiError('Unhandled Exception in aiController.parseCommand', error);
    res.status(500).json({ error: error.message || 'Internal server error processing AI command' });
  }
};

const getHistory = (req, res) => {
  const sessionId = getOrCreateSession(req);
  const history = getContext(sessionId);
  res.json({ sessionId, history });
};

const getIntents = (req, res) => {
  // Returns supported intents schema
  res.json({
    supportedIntents: [
      'OPEN_APP', 'CALL_CONTACT', 'SEND_SMS', 'OPEN_CAMERA', 
      'OPEN_GALLERY', 'OPEN_BROWSER', 'OPEN_YOUTUBE', 'OPEN_MAPS',
      'OPEN_GMAIL', 'OPEN_CALCULATOR', 'SET_ALARM', 'START_TIMER',
      'REMINDER', 'FLASHLIGHT_ON', 'FLASHLIGHT_OFF', 'VOLUME_CONTROL',
      'BATTERY_STATUS', 'STORAGE_STATUS', 'READ_NOTIFICATIONS',
      'UNKNOWN_COMMAND', 'GENERAL_CHAT'
    ]
  });
};

const clearHistory = (req, res) => {
  const sessionId = getOrCreateSession(req);
  clearSession(sessionId);
  res.json({ success: true, message: 'Session history cleared' });
};

module.exports = {
  parseCommand,
  getHistory,
  getIntents,
  clearHistory
};
