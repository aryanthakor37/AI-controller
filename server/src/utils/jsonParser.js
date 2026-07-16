/**
 * A robust JSON parser that extracts JSON from strings even if Gemini hallucinates markdown or conversational padding.
 * 
 * @param {string} rawString - The raw response string from Gemini
 * @returns {Object} - Parsed JSON object, or a fallback UNKNOWN_COMMAND object if parsing fails entirely.
 */
const parseGeminiJson = (rawString) => {
  if (!rawString || typeof rawString !== 'string') {
    return { intent: "UNKNOWN_COMMAND", confidence: 0.0 };
  }

  let text = rawString.trim();

  // Strip markdown code block wrappers (e.g. ```json ... ```)
  text = text.replace(/^```(json)?/, '').replace(/```$/, '').trim();

  try {
    // Attempt standard parse first
    return JSON.parse(text);
  } catch (err) {
    // Standard parse failed. Let's try aggressive extraction.
    // Find the first { and the last }
    const firstBrace = text.indexOf('{');
    const lastBrace = text.lastIndexOf('}');
    
    if (firstBrace !== -1 && lastBrace !== -1 && lastBrace > firstBrace) {
      const jsonSnippet = text.substring(firstBrace, lastBrace + 1);
      try {
        return JSON.parse(jsonSnippet);
      } catch (innerErr) {
        // Still failed. Something is fundamentally malformed with the string.
        console.error("[JSON Parser] Failed aggressive JSON extraction", innerErr);
        return { intent: "UNKNOWN_COMMAND", confidence: 0.0 };
      }
    }

    console.error("[JSON Parser] No JSON object braces found in string.");
    return { intent: "UNKNOWN_COMMAND", confidence: 0.0 };
  }
};

module.exports = {
  parseGeminiJson
};
