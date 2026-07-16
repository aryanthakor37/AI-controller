const express = require('express');
const { generatePairingCode, linkDevice, getMyDevices } = require('../controllers/deviceController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

// Protected route for dashboard to generate a code
router.get('/generate-code', protect, generatePairingCode);

// Protected route for dashboard to see linked devices
router.get('/list', protect, getMyDevices);

// Get currently active connected devices via socket for the logged-in user
router.get('/list-active', protect, (req, res) => {
  const connectionManager = require('../services/socket/ConnectionManager');
  const allActive = connectionManager.getAllDevices();
  // Filter only devices belonging to the logged-in user
  const userDevices = allActive.filter(d => d.owner && d.owner.toString() === req.user._id.toString());
  res.json(userDevices);
});

// Public route for Android app to link device using the code
router.post('/link', linkDevice);

module.exports = router;
