import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { loginUser } from '../redux/slices/userSlice';
import { Card } from '../components/atoms/Card';
import { Input } from '../components/atoms/Input';
import { Button } from '../components/atoms/Button';
import { motion } from 'framer-motion';

const Login = () => {
  const navigate = useNavigate();

  const dispatch = useDispatch();
  const { loading, error } = useSelector(state => state.user);

  const handleLogin = async (e) => {
    e.preventDefault();
    const email = e.target[0].value;
    const password = e.target[1].value;

    const resultAction = await dispatch(loginUser({ email, password }));
    if (loginUser.fulfilled.match(resultAction)) {
      navigate('/dashboard');
    } else {
      alert(resultAction.payload || 'Login failed');
    }
  };

  return (
    <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}>
      <Card>
        <h2 className="text-2xl font-semibold mb-6 text-center">Sign In</h2>
        <form onSubmit={handleLogin} className="space-y-4">
          <Input label="Email" type="email" placeholder="alex@example.com" required />
          <Input label="Password" type="password" placeholder="••••••••" required />

          <div className="flex items-center justify-between text-sm">
            <label className="flex items-center text-slate-300">
              <input type="checkbox" className="mr-2 rounded border-white/10 bg-surface/50 text-primary focus:ring-primary/50" />
              Remember me
            </label>
            <a href="#" className="text-primary hover:text-blue-400 transition-colors">Forgot Password?</a>
          </div>

          <Button type="submit" className="w-full mt-6">
            Log In
          </Button>
        </form>

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
