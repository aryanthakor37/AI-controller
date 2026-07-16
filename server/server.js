require('dotenv').config({ override: true });

// Verify GEMINI_API_KEY presence and format on boot
const geminiKey = process.env.GEMINI_API_KEY;
if (!geminiKey) {
  console.warn('⚠️ WARNING: GEMINI_API_KEY is not defined in your .env file!');
} else {
  if (geminiKey.startsWith('AIza')) {
    console.log('✅ Found GEMINI_API_KEY (AIza format)');
  } else if (geminiKey.startsWith('AQ.')) {
    console.log('✅ Found GEMINI_API_KEY (AQ format - Google OAuth/Vertex)');
  } else {
    console.warn('⚠️ WARNING: GEMINI_API_KEY is defined, but has an unknown format. Expected AIza... or AQ...');
  }
}

const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const compression = require('compression');
const cookieParser = require('cookie-parser');
const mongoose = require('mongoose');
const { createServer } = require('http');
const { initSocket } = require('./src/services/socket/socketManager');

const app = express();
const httpServer = createServer(app);
initSocket(httpServer);

// Custom Rate Limiter to prevent API abuse without adding heavy external dependencies
const rateLimitWindow = 15 * 60 * 1000; // 15 minutes
const maxRequestsPerIP = 150;
const ipRequestCounts = new Map();

const rateLimiter = (req, res, next) => {
  // Skip rate limiting for health check
  if (req.path === '/health') return next();
  
  const ip = req.ip || req.headers['x-forwarded-for'] || req.socket.remoteAddress;
  const now = Date.now();
  
  if (!ipRequestCounts.has(ip)) {
    ipRequestCounts.set(ip, []);
  }
  
  const timestamps = ipRequestCounts.get(ip).filter(time => now - time < rateLimitWindow);
  
  if (timestamps.length >= maxRequestsPerIP) {
    return res.status(429).json({ error: "Too many requests. Please try again in 15 minutes." });
  }
  
  timestamps.push(now);
  ipRequestCounts.set(ip, timestamps);
  next();
};

// Middleware
app.use(helmet());
app.use(cors({ origin: process.env.CLIENT_URL || '*', credentials: true }));
app.use(morgan('dev'));
app.use(compression());
app.use(rateLimiter);
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());

// Database Connection
mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/aimobile')
  .then(() => console.log('MongoDB connected'))
  .catch(err => console.error('MongoDB connection error:', err));

// Basic Health Route
app.get('/health', (req, res) => res.status(200).json({ status: 'ok' }));

// AI Routes
const aiRoutes = require('./src/routes/aiRoutes');
const voiceRoutes = require('./src/routes/voiceRoutes');
const authRoutes = require('./src/routes/authRoutes');
const deviceRoutes = require('./src/routes/deviceRoutes');
const cloudRoutes = require('./src/routes/cloudRoutes');

app.use('/api/ai', aiRoutes);
app.use('/api/voice', voiceRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/device', deviceRoutes);
app.use('/api', cloudRoutes);

const PORT = process.env.PORT || 5000;
httpServer.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
