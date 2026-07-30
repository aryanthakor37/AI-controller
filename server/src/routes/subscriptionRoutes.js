const express = require('express');
const router = express.Router();
const { getSubscriptionStatus, upgradeToPro } = require('../controllers/subscriptionController');

router.get('/status', getSubscriptionStatus);
router.post('/upgrade', upgradeToPro);

module.exports = router;
