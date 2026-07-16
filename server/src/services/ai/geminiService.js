const { GoogleGenAI } = require("@google/genai");

const { INTENT_SCHEMA } = require("./promptManager");
const { formatContextForPrompt } = require("./contextManager");
const { parseGeminiJson } = require("../../utils/jsonParser");
const { validateCommand } = require("./commandValidator");
const {
  logAiLifecycle,
  logAiError,
} = require("../../utils/aiLogger");

// Create Gemini client
const getGeminiClient = () => {
  const apiKey = process.env.GEMINI_API_KEY;

  if (!apiKey) {
    throw new Error("GEMINI_API_KEY is missing.");
  }

  return new GoogleGenAI({
    apiKey,
  });
};

const processCommand = async (command, sessionId) => {
  const startTime = Date.now();
  let prompt = "";
  let rawText = "";

  let ai;
  try {
    ai = getGeminiClient();
  } catch (error) {
    logAiError("Failed to initialize Gemini Client", error);
    return { success: false, error: error.message, data: { intent: "UNKNOWN_COMMAND", confidence: 0, error: "Client initialization failed" } };
  }

  const contextStr = formatContextForPrompt(sessionId);
  prompt = `${INTENT_SCHEMA}\n\n${contextStr}User: "${command}"`;

  // 1. GENERATE CONTENT
  let response;
  try {
    response = await ai.models.generateContent({
      model: "gemini-flash-lite-latest",
      contents: prompt,
    });
    rawText = response.text || "";
  } catch (error) {
    const executionTimeMs = Date.now() - startTime;
    // Extract deep error properties for @google/genai
    const errorDetails = {
      message: error.message,
      status: error.status,
      code: error.code,
      details: error.details,
      response: error.response
    };
    logAiError("Gemini API GenerateContent Failed", errorDetails);
    return { success: false, error: JSON.stringify(errorDetails), data: { intent: "UNKNOWN_COMMAND", confidence: 0, error: "API call failed" } };
  }

  // 2. PARSE JSON
  let parsedJson;
  try {
    parsedJson = parseGeminiJson(rawText);
  } catch (error) {
    logAiError("Failed to parse JSON", { error: error.message, rawText });
    return { success: false, error: "JSON parsing threw an exception", data: { intent: "UNKNOWN_COMMAND", confidence: 0, error: "Parse failed" } };
  }

  // 3. VALIDATE COMMAND
  let finalIntentData;
  try {
    finalIntentData = validateCommand(parsedJson);
  } catch (error) {
    logAiError("Failed to validate command", { error: error.message, parsedJson });
    return { success: false, error: "Validation threw an exception", data: { intent: "UNKNOWN_COMMAND", confidence: 0, error: "Validation failed" } };
  }

  const executionTimeMs = Date.now() - startTime;
  logAiLifecycle({
    prompt,
    rawResponse: rawText,
    parsedJson,
    finalIntent: finalIntentData.intent,
    confidence: finalIntentData.confidence,
    executionTimeMs,
  });

  return {
    success: true,
    data: finalIntentData,
    rawOutput: rawText,
    tokenUsage: response.usageMetadata || null,
  };
};

module.exports = {
  processCommand,
};