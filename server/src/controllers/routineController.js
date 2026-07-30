const Routine = require('../models/Routine');
const { getIo } = require('../services/socket/socketManager');

// Predefined system default routines
const DEFAULT_ROUTINES = [
  {
    title: 'Good Night Mode',
    triggerPhrase: 'good night',
    description: 'Set alarm at 7:00 AM, turn off flashlight, decrease volume, and check weather.',
    icon: 'Moon',
    category: 'ROUTINE',
    isSystemDefault: true,
    actions: [
      { intent: 'FLASHLIGHT_OFF', args: {}, delayMs: 300 },
      { intent: 'DECREASE_VOLUME', args: {}, delayMs: 300 },
      { intent: 'SET_ALARM', args: { time: '07:00' }, delayMs: 500 },
      { intent: 'CHECK_WEATHER', args: { reply: '🌤️ Tomorrow: 29°C Clear Sky' }, delayMs: 500 }
    ]
  },
  {
    title: 'Emergency SOS Trigger',
    triggerPhrase: 'emergency sos',
    description: 'Send immediate alert, turn on flashlight strobe, and notify contacts.',
    icon: 'AlertTriangle',
    category: 'EMERGENCY',
    isSystemDefault: true,
    actions: [
      { intent: 'FLASHLIGHT_ON', args: {}, delayMs: 200 },
      { intent: 'SEND_SMS', args: { contact: 'Emergency Contact', message: 'EMERGENCY: I need help! Please call me.' }, delayMs: 500 },
      { intent: 'INCREASE_VOLUME', args: {}, delayMs: 300 }
    ]
  },
  {
    title: 'Work Focus Mode',
    triggerPhrase: 'work mode',
    description: 'Lower media volume and set 45-minute productivity timer.',
    icon: 'Briefcase',
    category: 'FOCUS',
    isSystemDefault: true,
    actions: [
      { intent: 'DECREASE_VOLUME', args: {}, delayMs: 300 },
      { intent: 'SET_TIMER', args: { duration: 2700 }, delayMs: 500 }
    ]
  },
  {
    title: 'Heading Home',
    triggerPhrase: 'heading home',
    description: 'Open Maps for navigation and send update SMS.',
    icon: 'Navigation',
    category: 'TRAVEL',
    isSystemDefault: true,
    actions: [
      { intent: 'OPEN_MAPS', args: {}, delayMs: 500 },
      { intent: 'SEND_SMS', args: { contact: 'Family', message: 'On my way home now!' }, delayMs: 500 }
    ]
  }
];

// Seed defaults if empty
const seedDefaultsIfNeeded = async () => {
  const count = await Routine.countDocuments({ isSystemDefault: true });
  if (count === 0) {
    await Routine.insertMany(DEFAULT_ROUTINES);
  }
};

const getRoutines = async (req, res) => {
  try {
    await seedDefaultsIfNeeded();
    const userId = req.user ? req.user._id : null;
    
    // Fetch default routines + user routines
    const routines = await Routine.find({
      $or: [
        { isSystemDefault: true },
        ...(userId ? [{ user: userId }] : [])
      ]
    }).sort({ isSystemDefault: -1, createdAt: -1 });

    res.json({ success: true, routines });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

const createRoutine = async (req, res) => {
  try {
    const { title, triggerPhrase, description, icon, category, actions } = req.body;

    if (!title || !triggerPhrase || !actions || !actions.length) {
      return res.status(400).json({ success: false, message: 'Title, trigger phrase, and at least 1 action are required' });
    }

    const routine = await Routine.create({
      user: req.user ? req.user._id : null,
      title,
      triggerPhrase,
      description: description || '',
      icon: icon || 'Zap',
      category: category || 'ROUTINE',
      isSystemDefault: false,
      actions
    });

    res.status(201).json({ success: true, routine });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

const executeRoutine = async (req, res) => {
  try {
    const { id } = req.params;
    const { triggerPhrase } = req.body;

    let routine;
    if (id) {
      routine = await Routine.findById(id);
    } else if (triggerPhrase) {
      routine = await Routine.findOne({ triggerPhrase: triggerPhrase.toLowerCase().trim() });
    }

    if (!routine) {
      return res.status(404).json({ success: false, message: 'Routine not found' });
    }

    // Dispatch actions sequentially via socket
    try {
      const io = getIo();
      routine.actions.forEach((action, index) => {
        setTimeout(() => {
          io.emit('command:execute', {
            intent: action.intent,
            ...action.args,
            routineTitle: routine.title,
            step: index + 1,
            totalSteps: routine.actions.length
          });
        }, index * (action.delayMs || 500));
      });
    } catch (socketError) {
      console.warn('Socket dispatch warning:', socketError.message);
    }

    res.json({
      success: true,
      message: `Executing routine: "${routine.title}" (${routine.actions.length} steps)`,
      routine
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

const deleteRoutine = async (req, res) => {
  try {
    const { id } = req.params;
    const routine = await Routine.findById(id);

    if (!routine) {
      return res.status(404).json({ success: false, message: 'Routine not found' });
    }

    if (routine.isSystemDefault) {
      return res.status(403).json({ success: false, message: 'Cannot delete system default routine' });
    }

    await Routine.findByIdAndDelete(id);
    res.json({ success: true, message: 'Routine deleted successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

module.exports = {
  getRoutines,
  createRoutine,
  executeRoutine,
  deleteRoutine
};
