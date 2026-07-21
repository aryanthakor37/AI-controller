const dotenv = require('dotenv');
dotenv.config();

const sendEmail = require('./src/utils/sendEmail');

console.log('Testing SMTP connection with settings:');
console.log('Host:', process.env.SMTP_HOST);
console.log('User:', process.env.SMTP_USER);
console.log('Pass length:', process.env.SMTP_PASS ? process.env.SMTP_PASS.length : 0);

sendEmail({
  to: 'thakoraryan94@gmail.com', // send to self
  subject: 'Agent.AI SMTP Connection Test',
  html: '<p>If you see this, email sending works perfectly!</p>'
})
.then(info => {
  console.log('✅ SMTP sending success!');
  console.log('Message ID:', info.messageId);
  process.exit(0);
})
.catch(err => {
  console.error('❌ SMTP sending failed!');
  console.error(err);
  process.exit(1);
});
