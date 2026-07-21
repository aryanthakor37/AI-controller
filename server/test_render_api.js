const https = require('https');

const renderUrl = 'https://aimobile-backend.onrender.com';

console.log('Testing Render Server API connectivity (using https):');

// 1. Health check
https.get(`${renderUrl}/health`, (res) => {
  let data = '';
  res.on('data', chunk => { data += chunk; });
  res.on('end', () => {
    console.log('✅ Render Health Check Status:', res.statusCode);
    console.log('   Response:', data);
    
    // 2. Test verify-email endpoint existence
    testVerifyEmail();
  });
}).on('error', (err) => {
  console.error('❌ Failed to reach Render server:', err.message);
});

function testVerifyEmail() {
  const url = `${renderUrl}/api/auth/verify-email`;
  const req = https.request(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    }
  }, (res) => {
    let data = '';
    res.on('data', chunk => { data += chunk; });
    res.on('end', () => {
      console.log('Response status:', res.statusCode);
      if (res.statusCode === 400) {
        console.log('✅ Verify-Email endpoint exists and is active! (Returned 400 Bad Request)');
      } else if (res.statusCode === 404) {
        console.log('❌ Verify-Email endpoint does NOT exist on Render yet! (Returned 404 Not Found)');
      } else {
        console.log('Unexpected response:', data);
      }
    });
  });
  
  req.on('error', (err) => {
    console.error('Verify-email request error:', err.message);
  });
  
  req.write(JSON.stringify({}));
  req.end();
}
