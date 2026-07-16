const jwt = require('jsonwebtoken');
const User = require('../models/User');

const protect = async (req, res, next) => {
  let token;

  if (
    req.headers.authorization &&
    req.headers.authorization.startsWith('Bearer')
  ) {
    try {
      token = req.headers.authorization.split(' ')[1];
      console.log('Received auth token:', token);
      const decoded = jwt.verify(token, process.env.JWT_SECRET || 'fallback_secret');
      console.log('Decoded payload:', decoded);

      const userId = decoded.id || decoded.owner;
      req.user = await User.findById(userId).select('-password');
      if (!req.user) {
        console.warn('User not found in DB for ID:', userId);
        return res.status(401).json({ message: 'Not authorized, user not found' });
      }
      console.log('Auth check passed for user:', req.user.email);
      next();
    } catch (error) {
      console.error('JWT verification failed:', error.message);
      res.status(401).json({ message: 'Not authorized, token failed', error: error.message });
    }
  }

  if (!token) {
    console.warn('Authorization header missing or invalid format');
    res.status(401).json({ message: 'Not authorized, no token' });
  }
};

module.exports = { protect };
