const express = require('express');
const router = express.Router();
const aiController = require('../controllers/aiController');
const { protect } = require('../middleware/authMiddleware');

// Define AI Routes
router.post('/parse-command', protect, aiController.parseCommand);
router.post('/chat', protect, aiController.parseCommand); // Both use the same parsing engine right now
router.get('/history', protect, aiController.getHistory);
router.post('/history/clear', protect, aiController.clearHistory);
router.get('/intents', protect, aiController.getIntents);

module.exports = router;
