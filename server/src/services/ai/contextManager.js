// In-memory conversation store mapping sessionId -> Array of messages
const sessions = new Map();

// Max messages to keep in context per session
const MAX_HISTORY = 20;

const getContext = (sessionId) => {
  if (!sessions.has(sessionId)) {
    sessions.set(sessionId, []);
  }
  return sessions.get(sessionId);
};

const addMessage = (sessionId, role, text) => {
  const history = getContext(sessionId);
  history.push({ role, text });
  
  if (history.length > MAX_HISTORY) {
    history.shift(); // Keep only recent messages
  }
};

const formatContextForPrompt = (sessionId, limit = 5) => {
  const history = getContext(sessionId);
  if (history.length === 0) return '';
  
  const recentHistory = history.slice(-limit);
  let formatted = 'PREVIOUS CONVERSATION CONTEXT:\n';
  recentHistory.forEach(msg => {
    formatted += `${msg.role === 'user' ? 'User' : 'AI'}: ${msg.text}\n`;
  });
  return formatted + '\n\nCURRENT COMMAND:\n';
};

const clearSession = (sessionId) => {
  sessions.delete(sessionId);
};

module.exports = {
  getContext,
  addMessage,
  formatContextForPrompt,
  clearSession
};
