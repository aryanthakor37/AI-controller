const nodemailer = require('nodemailer');

const sendEmail = async ({ to, subject, html }) => {
  let transporter;

  const host = process.env.SMTP_HOST;
  const port = process.env.SMTP_PORT;
  const user = process.env.SMTP_USER;
  const pass = process.env.SMTP_PASS;

  if (host && user && pass) {
    if (host === 'smtp.gmail.com' || host.includes('gmail')) {
      // Use optimal built-in Gmail service settings
      transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: { user, pass },
        connectionTimeout: 10000, // 10 seconds
        socketTimeout: 10000
      });
    } else {
      transporter = nodemailer.createTransport({
        host,
        port: parseInt(port) || 587,
        secure: port === '465',
        auth: { user, pass },
        connectionTimeout: 10000,
        socketTimeout: 10000
      });
    }
  } else {
    // Generate test SMTP service account from ethereal.email
    const testAccount = await nodemailer.createTestAccount();
    transporter = nodemailer.createTransport({
      host: 'smtp.ethereal.email',
      port: 587,
      secure: false,
      auth: {
        user: testAccount.user,
        pass: testAccount.pass
      }
    });
  }

  const mailOptions = {
    from: process.env.SMTP_FROM || '"Agent.AI Support" <support@agent.ai>',
    to,
    subject,
    html
  };

  const info = await transporter.sendMail(mailOptions);

  if (!host || !user || !pass) {
    console.log(`✉️ Test Email Sent! Preview URL: ${nodemailer.getTestMessageUrl(info)}`);
  }

  return info;
};

module.exports = sendEmail;
