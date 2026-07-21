const mongoose = require('mongoose');
const dotenv = require('dotenv');
dotenv.config();

const User = require('./src/models/User');

mongoose.connect(process.env.MONGODB_URI)
.then(async () => {
  console.log('✅ MongoDB Connected');
  const users = await User.find().sort({ createdAt: -1 }).limit(5);
  console.log('\n--- Recent 5 Users in Database ---');
  if (users.length === 0) {
    console.log('No users found.');
  } else {
    users.forEach((user, index) => {
      console.log(`[User ${index + 1}]`);
      console.log(`  ID:`, user._id);
      console.log(`  Name:`, user.fullName);
      console.log(`  Email:`, user.email);
      console.log(`  isVerified:`, user.isVerified);
      console.log(`  Verification Code:`, user.verificationCode);
      console.log(`  Code Expires:`, user.verificationCodeExpires);
      console.log(`  Created At:`, user.createdAt);
      console.log('---------------------------');
    });
  }
  process.exit(0);
})
.catch(err => {
  console.error('❌ MongoDB Connection Error:', err);
  process.exit(1);
});
