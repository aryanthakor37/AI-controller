const express = require('express');
const router = express.Router();
const voiceController = require('../controllers/voiceController');

// Dummy endpoint for transcribe (since we use Web Speech API in frontend, 
// this is just for future server-side expansions like Whisper)
router.post('/transcribe', (req, res) => res.json({ transcript: req.body.audio_text || '' }));

// Respond to the voice transcript via Gemini AI
router.post('/respond', voiceController.respondToVoice);

router.get('/settings', voiceController.getVoiceSettings);
router.post('/history', voiceController.getVoiceHistory);

module.exports = router;
