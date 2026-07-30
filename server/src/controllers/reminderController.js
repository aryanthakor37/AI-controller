const Reminder = require('../models/Reminder');

const createReminder = async (req, res) => {
  try {
    const { deviceId, title, date, time, repeat, contact } = req.body;
    if (!deviceId || !title || !date || !time) {
      return res.status(400).json({ message: 'deviceId, title, date, and time are required.' });
    }

    const reminder = await Reminder.create({
      user: req.user ? req.user._id : null,
      deviceId,
      title,
      date,
      time,
      repeat: repeat || 'NONE',
      contact: contact || ''
    });

    res.status(201).json({ message: 'Reminder created successfully', reminder });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

const getReminders = async (req, res) => {
  try {
    const { deviceId } = req.query;
    const filter = {};
    if (deviceId) filter.deviceId = deviceId;
    if (req.user) filter.user = req.user._id;

    const reminders = await Reminder.find(filter).sort({ date: 1, time: 1 });
    res.json(reminders);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

const deleteReminder = async (req, res) => {
  try {
    const { id } = req.params;
    await Reminder.findByIdAndDelete(id);
    res.json({ message: 'Reminder deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  createReminder,
  getReminders,
  deleteReminder
};
