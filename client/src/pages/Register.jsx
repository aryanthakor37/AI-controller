import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { registerUser } from '../redux/slices/userSlice';
import { Card } from '../components/atoms/Card';
import { Input } from '../components/atoms/Input';
import { Button } from '../components/atoms/Button';
import { motion } from 'framer-motion';

const Register = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { loading, error } = useSelector(state => state.user);

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleRegister = async (e) => {
    e.preventDefault();
    const resultAction = await dispatch(registerUser({ fullName, email, password }));
    if (registerUser.fulfilled.match(resultAction)) {
      navigate('/dashboard');
    } else {
      alert(resultAction.payload || 'Registration failed');
    }
  };

  return (
    <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}>
      <Card>
        <h2 className="text-2xl font-semibold mb-6 text-center">Create an Account</h2>

        <form onSubmit={handleRegister} className="space-y-4">
          <Input
            label="Full Name"
            type="text"
            placeholder="Alex Developer"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
          />
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
          <Button type="submit" className="w-full mt-6" disabled={loading}>
            {loading ? 'Signing Up...' : 'Sign Up'}
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-400">
          Already have an account?{' '}
          <Link to="/login" className="text-primary hover:text-blue-400 transition-colors font-medium">
            Sign in
          </Link>
        </p>
      </Card>
    </motion.div>
  );
};

export default Register;
