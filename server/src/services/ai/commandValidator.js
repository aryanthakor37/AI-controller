const VALID_INTENTS = new Set([
  "OPEN_APP",
  "OPEN_CAMERA",
  "OPEN_GALLERY",
  "OPEN_CHROME",
  "OPEN_YOUTUBE",
  "OPEN_MAPS",
  "CALL_CONTACT",
  "SEND_SMS",
  "FLASHLIGHT_ON",
  "FLASHLIGHT_OFF",
  "SET_ALARM",
  "SET_TIMER",
  "INCREASE_VOLUME",
  "DECREASE_VOLUME",
  "BATTERY_STATUS",
  "DEVICE_INFO",
  "READ_NOTIFICATIONS",
  "UNKNOWN_COMMAND",
  "SEARCH_YOUTUBE"
]);

/**
 * Validates and normalizes the parsed command object.
 * Cross-checks against the whitelist of supported intents.
 * 
 * @param {Object} parsedObj - The parsed JSON object from Gemini
 * @returns {Object} - A validated command object guaranteed to have a valid 'intent'
 */
const validateCommand = (parsedObj) => {
  if (!parsedObj || typeof parsedObj !== 'object') {
    return { intent: "UNKNOWN_COMMAND", confidence: 0.0 };
  }

  // Ensure confidence is a number
  let confidence = typeof parsedObj.confidence === 'number' ? parsedObj.confidence : 0.5;

  let intent = parsedObj.intent;
  
  if (!intent || typeof intent !== 'string') {
    return { intent: "UNKNOWN_COMMAND", confidence: 0.0 };
  }

  intent = intent.toUpperCase().trim();

  // Normalize common hallucinations (e.g. if Gemini returns "OPEN_BROWSER" instead of "OPEN_CHROME")
  if (intent === 'OPEN_BROWSER') intent = 'OPEN_CHROME';

  // Strict enforcement
  if (!VALID_INTENTS.has(intent)) {
    console.warn(`[Command Validator] Invalid intent detected: ${intent}. Falling back to UNKNOWN_COMMAND.`);
    return { intent: "UNKNOWN_COMMAND", confidence: 0.0 };
  }

  return {
    ...parsedObj,
    intent,
    confidence
  };
};

module.exports = {
  validateCommand,
  VALID_INTENTS
};
