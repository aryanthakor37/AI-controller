const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const User = require('../models/User');
const RefreshToken = require('../models/RefreshToken');
const sendEmail = require('../utils/sendEmail');

const JWT_SECRET = process.env.JWT_SECRET || 'fallback_secret_change_in_prod';
const JWT_EXPIRES_IN = '15m';       // Short-lived access token
const REFRESH_EXPIRES_DAYS = 30;   // Long-lived refresh token

// --- Token Generators ---
const generateAccessToken = (userId) => {
  return jwt.sign({ id: userId }, JWT_SECRET, { expiresIn: JWT_EXPIRES_IN });
};

const generateRefreshToken = async (userId) => {
  const token = crypto.randomBytes(40).toString('hex');
  const expiresAt = new Date();
  expiresAt.setDate(expiresAt.getDate() + REFRESH_EXPIRES_DAYS);
  await RefreshToken.create({ token, user: userId, expiresAt });
  return token;
};

// POST /api/auth/register
const registerUser = async (req, res) => {
  try {
    const { fullName, email, password } = req.body;
    if (!fullName || !email || !password) {
      return res.status(400).json({ message: 'All fields are required' });
    }
    const userExists = await User.findOne({ email });
    if (userExists) {
      return res.status(400).json({ message: 'User already exists' });
    }

    const user = await User.create({
      fullName,
      email,
      password,
      isVerified: true
    });

    const accessToken = generateAccessToken(user._id);
    const refreshToken = await generateRefreshToken(user._id);

    res.status(201).json({
      _id: user._id,
      fullName: user.fullName,
      email: user.email,
      avatar: user.avatar,
      role: user.role,
      accessToken,
      refreshToken
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// POST /api/auth/verify-email
const verifyEmail = async (req, res) => {
  try {
    const { email, code } = req.body;
    if (!email || !code) {
      return res.status(400).json({ message: 'Email and verification code are required' });
    }
    const user = await User.findOne({
      email: email.toLowerCase(),
      verificationCode: code,
      verificationCodeExpires: { $gt: new Date() }
    });
    if (!user) {
      return res.status(400).json({ message: 'Invalid or expired verification code' });
    }
    user.isVerified = true;
    user.verificationCode = null;
    user.verificationCodeExpires = null;
    await user.save();

    const accessToken = generateAccessToken(user._id);
    const refreshToken = await generateRefreshToken(user._id);

    res.json({
      _id: user._id,
      fullName: user.fullName,
      email: user.email,
      avatar: user.avatar,
      role: user.role,
      accessToken,
      refreshToken,
      token: accessToken,
      message: 'Email verified successfully!'
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// POST /api/auth/login
const loginUser = async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ message: 'Email and password are required' });
    }
    const user = await User.findOne({ email });
    if (!user || !(await user.comparePassword(password))) {
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    const accessToken = generateAccessToken(user._id);
    const refreshToken = await generateRefreshToken(user._id);
    res.json({
      _id: user._id,
      fullName: user.fullName,
      email: user.email,
      avatar: user.avatar,
      role: user.role,
      accessToken,
      refreshToken,
      token: accessToken,
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// POST /api/auth/logout
const logoutUser = async (req, res) => {
  try {
    const { refreshToken } = req.body;
    if (refreshToken) {
      await RefreshToken.deleteOne({ token: refreshToken });
    }
    res.json({ message: 'Logged out successfully' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// POST /api/auth/refresh
const refreshAccessToken = async (req, res) => {
  try {
    const { refreshToken } = req.body;
    if (!refreshToken) {
      return res.status(401).json({ message: 'Refresh token required' });
    }
    const stored = await RefreshToken.findOne({ token: refreshToken }).populate('user');
    if (!stored || stored.expiresAt < new Date()) {
      return res.status(401).json({ message: 'Invalid or expired refresh token' });
    }
    const newAccessToken = generateAccessToken(stored.user._id);
    res.json({ accessToken: newAccessToken });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// POST /api/auth/forgot-password
const forgotPassword = async (req, res) => {
  try {
    const { email } = req.body;
    const user = await User.findOne({ email });
    if (!user) {
      return res.json({ message: 'If this email exists, a reset link has been sent.' });
    }
    const resetToken = crypto.randomBytes(32).toString('hex');
    user.resetPasswordToken = resetToken;
    user.resetPasswordExpires = new Date(Date.now() + 15 * 60 * 1000); // 15 min
    await user.save();
    
    const resetUrl = `${process.env.CLIENT_URL || 'http://localhost:3000'}/reset-password?token=${resetToken}`;
    sendEmail({
      to: user.email,
      subject: 'Agent.AI Password Reset Request',
      html: `
        <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 600px; border: 1px solid #ddd; border-radius: 8px;">
          <h2 style="color: #3b82f6;">Reset Your Password</h2>
          <p>You requested a password reset for your Agent.AI account. Click the button below to set a new password:</p>
          <div style="text-align: center; margin: 30px 0;">
            <a href="${resetUrl}" style="background-color: #3b82f6; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;">Reset Password</a>
          </div>
          <p style="color: #555;">This link is valid for 15 minutes. If you did not request this, you can ignore this email.</p>
        </div>
      `
    }).catch(emailError => {
      console.error('Failed to send forgot-password email:', emailError);
    });

    res.json({ message: 'Reset link sent.' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// POST /api/auth/reset-password
const resetPassword = async (req, res) => {
  try {
    const { resetToken, newPassword } = req.body;
    const user = await User.findOne({
      resetPasswordToken: resetToken,
      resetPasswordExpires: { $gt: new Date() }
    });
    if (!user) {
      return res.status(400).json({ message: 'Invalid or expired reset token' });
    }
    user.password = newPassword;
    user.resetPasswordToken = null;
    user.resetPasswordExpires = null;
    await user.save();
    res.json({ message: 'Password reset successful' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// GET /api/auth/profile
const getProfile = async (req, res) => {
  try {
    const user = await User.findById(req.user._id).select('-password -resetPasswordToken -resetPasswordExpires');
    if (!user) return res.status(404).json({ message: 'User not found' });
    res.json(user);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// PUT /api/auth/profile
const updateProfile = async (req, res) => {
  try {
    const { fullName, avatar } = req.body;
    const user = await User.findById(req.user._id);
    if (!user) return res.status(404).json({ message: 'User not found' });
    if (fullName) user.fullName = fullName;
    if (avatar) user.avatar = avatar;
    await user.save();
    res.json({ _id: user._id, fullName: user.fullName, email: user.email, avatar: user.avatar });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  registerUser,
  verifyEmail,
  loginUser,
  logoutUser,
  refreshAccessToken,
  forgotPassword,
  resetPassword,
  getProfile,
  updateProfile,
};
