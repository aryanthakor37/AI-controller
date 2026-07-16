const { processCommand } = require('../services/ai/geminiService');
const { addMessage, getContext } = require('../services/ai/contextManager');

const respondToVoice = async (req, res) => {
  const { transcript, sessionId } = req.body;
  if (!transcript) {
    return res.status(400).json({ error: 'Transcript is required' });
  }

  // We rely on the same AI logic from Phase 4, but we can wrap it for voice-specific metadata
  try {
    const aiResult = await processCommand(transcript, sessionId || 'default_voice_session');
    
    // Determine spoken response text based on the intent
    let spokenText = '';
    const intent = aiResult.data.intent;
    
    if (intent === 'GENERAL_CHAT') {
      spokenText = aiResult.data.reply || 'I am not sure how to respond to that.';
    } else if (intent === 'UNKNOWN_COMMAND') {
      spokenText = 'Sorry, I did not understand that command.';
    } else {
      spokenText = `Executing command: ${intent.replace(/_/g, ' ').toLowerCase()}`;
      if (aiResult.data.app) spokenText += ` ${aiResult.data.app}`;
      if (aiResult.data.contact) spokenText += ` ${aiResult.data.contact}`;
    }

    res.json({
      success: true,
      data: {
        ...aiResult.data,
        spokenResponse: spokenText // Text-To-Speech engine on frontend will read this
      }
    });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error processing voice command' });
  }
};

const getVoiceSettings = (req, res) => {
  res.json({
    settings: {
      defaultLanguage: 'en-US',
      pitch: 1,
      rate: 1,
      volume: 1
    }
  });
};

const getVoiceHistory = (req, res) => {
  const { sessionId } = req.body;
  res.json({ history: getContext(sessionId || 'default_voice_session') });
};

module.exports = {
  respondToVoice,
  getVoiceSettings,
  getVoiceHistory
};
