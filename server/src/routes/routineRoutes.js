const express = require('express');
const router = express.Router();
const {
  getRoutines,
  createRoutine,
  executeRoutine,
  deleteRoutine
} = require('../controllers/routineController');
const { protect } = require('../middleware/authMiddleware');

router.get('/', getRoutines);
router.post('/', createRoutine);
router.post('/execute/:id?', executeRoutine);
router.delete('/:id', deleteRoutine);

module.exports = router;
