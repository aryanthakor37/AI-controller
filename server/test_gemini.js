require('dotenv').config();
const { processCommand } = require('./src/services/ai/geminiService');

async function runTest() {
  const res = await processCommand("open crome and search img", "test-session");
  console.log(JSON.stringify(res, null, 2));
}

runTest();
