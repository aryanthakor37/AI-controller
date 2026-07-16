const fs = require('fs');
const path = require('path');

// Simple file logger for AI events
const logDir = path.join(__dirname, '../../logs');
if (!fs.existsSync(logDir)) {
  fs.mkdirSync(logDir, { recursive: true });
}

const aiLogFile = path.join(logDir, 'ai-lifecycle.log');

/**
 * Logs the full lifecycle of an AI command.
 */
const logAiLifecycle = ({ prompt, rawResponse, parsedJson, finalIntent, confidence, executionTimeMs }) => {
  const timestamp = new Date().toISOString();
  
  const logEntry = `
=========================================
TIMESTAMP: ${timestamp}
EXECUTION TIME: ${executionTimeMs}ms
-----------------------------------------
[1. USER PROMPT]
${prompt}
-----------------------------------------
[2. GEMINI RAW RESPONSE]
${rawResponse}
-----------------------------------------
[3. PARSED JSON]
${JSON.stringify(parsedJson, null, 2)}
-----------------------------------------
[4. FINAL VALIDATED INTENT]
Intent: ${finalIntent}
Confidence: ${confidence}
=========================================
`;

  // Log to console for dev
  console.log(`[AI LIFECYCLE] ${timestamp} - Intent: ${finalIntent} (${executionTimeMs}ms)`);

  // Log to file
  fs.appendFile(aiLogFile, logEntry, (err) => {
    if (err) console.error('Failed to write to AI log file:', err);
  });
};

const util = require('util');

const logAiError = (message, error) => {
  const timestamp = new Date().toISOString();
  
  // Format the error object deeply so we don't just see [Object object]
  const errorDetails = error ? (typeof error === 'string' ? error : util.inspect(error, { showHidden: false, depth: null, colors: false })) : '';
  
  console.error(`\n[AI ERROR] ${timestamp}: ${message}\n${errorDetails}\n`);
  
  const fileLog = `[ERROR] ${timestamp}: ${message}\n${errorDetails}\n`;
  fs.appendFile(aiLogFile, fileLog, () => {});
};

module.exports = {
  logAiLifecycle,
  logAiError
};
