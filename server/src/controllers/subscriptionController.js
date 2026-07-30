const User = require('../models/User');

// Returns subscription status - Currently 100% Free & Unlimited for all users
const getSubscriptionStatus = async (req, res) => {
  try {
    res.json({
      tier: 'Pro (Free Access)',
      isUnlimited: true,
      dailyCommandsUsed: 0,
      dailyCommandLimit: 'Unlimited',
      features: [
        'Unlimited AI Voice Commands',
        'Unlimited Birthday & Event Reminders',
        'Action Buttons (Call, SMS, WhatsApp)',
        'Full Accessibility Automation'
      ]
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

const upgradeToPro = async (req, res) => {
  try {
    res.json({ message: 'All features are already unlocked 100% Free!' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  getSubscriptionStatus,
  upgradeToPro
};
