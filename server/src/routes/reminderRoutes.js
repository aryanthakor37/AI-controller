const express = require('express');
const router = express.Router();
const { createReminder, getReminders, deleteReminder } = require('../controllers/reminderController');
const { protect } = require('../middleware/authMiddleware');

router.post('/', createReminder);
router.get('/', getReminders);
router.delete('/:id', deleteReminder);

module.exports = router;
