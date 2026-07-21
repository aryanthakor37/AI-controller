import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { getApiUrl } from '../config';
import { loginUser } from '../redux/slices/userSlice';
import { Card } from '../components/atoms/Card';
import { Input } from '../components/atoms/Input';
import { Button } from '../components/atoms/Button';
import { motion } from 'framer-motion';

const Login = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { loading, error } = useSelector(state => state.user);

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showForgotForm, setShowForgotForm] = useState(false);
  const [forgotEmail, setForgotEmail] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    const resultAction = await dispatch(loginUser({ email, password }));
    if (loginUser.fulfilled.match(resultAction)) {
      navigate('/dashboard');
    } else {
      alert(resultAction.payload || 'Login failed');
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch(`${getApiUrl()}/auth/forgot-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: forgotEmail }),
      });
      const data = await response.json();
      alert(data.message || 'Reset link sent if email exists.');
      setShowForgotForm(false);
    } catch (err) {
      alert('Failed to send reset link.');
    }
  };

  return (
    <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}>
      <Card>
        <h2 className="text-2xl font-semibold mb-6 text-center">
          {showForgotForm ? 'Reset Password' : 'Sign In'}
        </h2>
        
        {showForgotForm ? (
          <form onSubmit={handleForgotPassword} className="space-y-4">
            <p className="text-sm text-slate-400 mb-4 text-center">
              Enter your email address and we'll send you a link to reset your password.
            </p>
            <Input 
              label="Email Address" 
              type="email" 
              placeholder="alex@example.com" 
              value={forgotEmail}
              onChange={(e) => setForgotEmail(e.target.value)}
              required 
            />
            <Button type="submit" className="w-full mt-6">
              Send Reset Link
            </Button>
            <div className="text-center mt-4">
              <button 
                type="button" 
                onClick={() => setShowForgotForm(false)} 
                className="text-sm text-primary hover:text-blue-400 transition-colors"
              >
                Back to Sign In
              </button>
            </div>
          </form>
        ) : (
          <form onSubmit={handleLogin} className="space-y-4">
            <Input 
              label="Email" 
              type="email" 
              placeholder="alex@example.com" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required 
            />
            <Input 
              label="Password" 
              type="password" 
              placeholder="••••••••" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required 
            />

            <div className="flex items-center justify-between text-sm">
              <label className="flex items-center text-slate-300">
                <input type="checkbox" className="mr-2 rounded border-white/10 bg-surface/50 text-primary focus:ring-primary/50" />
                Remember me
              </label>
              <button 
                type="button" 
                onClick={() => setShowForgotForm(true)} 
                className="text-primary hover:text-blue-400 transition-colors"
              >
                Forgot Password?
              </button>
            </div>

            <Button type="submit" className="w-full mt-6" disabled={loading}>
              {loading ? 'Signing In...' : 'Log In'}
            </Button>
          </form>
        )}

        <p className="mt-6 text-center text-sm text-slate-400">
          Don't have an account?{' '}
          <Link to="/register" className="text-primary hover:text-blue-400 transition-colors font-medium">
            Sign up
          </Link>
        </p>
      </Card>
    </motion.div>
  );
};

export default Login;
