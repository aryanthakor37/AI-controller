const express = require('express');
const router = express.Router();
const os = require('os');

/**
 * @route GET /api/health/details
 * @desc  Returns server status, uptime, system load, and memory telemetry
 * @access Public
 */
router.get('/details', (req, res) => {
  const memoryUsage = process.memoryUsage();
  const systemInfo = {
    status: 'healthy',
    timestamp: new Date().toISOString(),
    uptime: `${Math.floor(process.uptime())} seconds`,
    nodeVersion: process.version,
    platform: os.platform(),
    arch: os.arch(),
    memory: {
      rss: `${Math.round(memoryUsage.rss / 1024 / 1024)} MB`,
      heapTotal: `${Math.round(memoryUsage.heapTotal / 1024 / 1024)} MB`,
      heapUsed: `${Math.round(memoryUsage.heapUsed / 1024 / 1024)} MB`,
    },
    systemFreeMemory: `${Math.round(os.freemem() / 1024 / 1024)} MB`,
    systemTotalMemory: `${Math.round(os.totalmem() / 1024 / 1024)} MB`,
    cpuCores: os.cpus().length,
  };

  res.status(200).json(systemInfo);
});

module.exports = router;
