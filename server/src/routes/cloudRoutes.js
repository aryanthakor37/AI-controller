const express = require('express');
const router = express.Router();
const {
  getHistory,
  getAnalytics,
  backupSettings,
  restoreSettings,
  syncOfflineQueue,
  getAllDevices
} = require('../controllers/cloudController');
const { protect } = require('../middleware/authMiddleware');

router.get('/history', protect, getHistory);
router.get('/analytics', protect, getAnalytics);
router.get('/device/all', protect, getAllDevices);
router.post('/backup', protect, backupSettings);
router.post('/restore', protect, restoreSettings);
router.post('/sync', protect, syncOfflineQueue);

module.exports = router;
