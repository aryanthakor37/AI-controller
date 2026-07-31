import React, { useState, useRef, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Send, Mic, Trash2, Sparkles, AlertCircle, CheckCircle2 } from 'lucide-react';
import { Card } from '../components/atoms/Card';
import { Input } from '../components/atoms/Input';
import { Button } from '../components/atoms/Button';
import { ChatBubble } from '../components/molecules/ChatBubble';
import { addMessage, clearMessages } from '../redux/slices/chatSlice';
import { fetchHistory } from '../redux/slices/commandSlice';
import socketService from '../services/socketService';

const QUICK_SUGGESTIONS = [
  "Take a photo",
  "Set an alarm for 7 AM",
  "Open Spotify",
  "Turn on Wi-Fi",
  "Check battery status",
  "Call Mom"
];

const Chat = () => {
  const { messages } = useSelector((state) => state.chat);
  const { activeDevices } = useSelector((state) => state.device);
  const settings = useSelector((state) => state.settings);
  const dispatch = useDispatch();
  const [input, setInput] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = async (textToSend) => {
    const text = textToSend || input;
    if (!text.trim() || isSubmitting) return;

    setIsSubmitting(true);
    dispatch(addMessage({ id: Date.now().toString(), role: 'user', content: text }));
    if (!textToSend) setInput('');

    // Add temporary AI processing indicator
    const tempId = (Date.now() + 1).toString();
    dispatch(addMessage({ id: tempId, role: 'ai', content: `Analyzing intent with ${settings?.aiModel || 'Gemini'}...`, isProcessing: true }));

    try {
      const { default: api } = await import('../services/api');
      const response = await api.post('/ai/parse-command', { 
        command: text,
        options: {
          model: settings?.aiModel,
          confidenceThreshold: settings?.aiConfidenceThreshold,
          contextWindow: settings?.aiContextWindow
        }
      });
      const intentData = response.data?.data || {};
      const { intent, app, contact, reply } = intentData;

      let msg = reply || `Intent Executed: ${intent || 'UNKNOWN'}`;
      if (app) msg += `\nTarget App: ${app}`;
      if (contact) msg += `\nTarget Contact: ${contact}`;

      // Dispatch final AI message
      dispatch(addMessage({ 
        id: Date.now().toString(), 
        role: 'ai', 
        content: msg,
        intent: intent,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }));

      // Forward intent command via WebSocket to active device
      if (activeDevices && activeDevices.length > 0) {
        socketService.sendCommand(activeDevices[0].socketId, intentData);
      } else {
        socketService.sendCommand('all', intentData); // fallback
      }
      dispatch(fetchHistory());
    } catch (error) {
      dispatch(addMessage({ 
        id: Date.now().toString(), 
        role: 'ai', 
        content: 'Error: Unable to connect to AI engine. Please verify server status.' 
      }));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    handleSendMessage();
  };

  const handleClearHistory = () => {
    if (window.confirm('Are you sure you want to clear chat history?')) {
      dispatch(clearMessages());
    }
  };

  return (
    <div className="h-full flex flex-col space-y-4">
      {/* Header Bar */}
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
            AI Command Center
          </h2>
          <p className="text-slate-400 text-sm">Issue natural language commands to control your Android smartphone.</p>
        </div>
        {messages.length > 0 && (
          <Button 
            onClick={handleClearHistory} 
            variant="ghost" 
            size="sm"
            className="text-slate-400 hover:text-red-400 border border-white/10 hover:bg-white/5"
          >
            <Trash2 className="w-4 h-4 mr-2" />
            Clear Chat
          </Button>
        )}
      </div>

      {/* Quick Suggestion Chips */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-1 scrollbar-hide">
        <span className="text-xs font-semibold text-slate-400 flex items-center mr-1 flex-shrink-0">
          <Sparkles className="w-3.5 h-3.5 mr-1 text-primary" /> Suggestions:
        </span>
        {QUICK_SUGGESTIONS.map((suggestion, idx) => (
          <button
            key={idx}
            onClick={() => handleSendMessage(suggestion)}
            disabled={isSubmitting}
            className="px-3 py-1.5 rounded-full text-xs font-medium bg-white/5 border border-white/10 text-slate-300 hover:text-white hover:bg-primary/20 hover:border-primary/50 transition-all flex-shrink-0"
          >
            "{suggestion}"
          </button>
        ))}
      </div>

      {/* Main Chat Conversation Window */}
      <Card className="flex-1 flex flex-col p-0 overflow-hidden bg-surface/30 border border-white/10">
        <div className="flex-1 overflow-y-auto p-6 space-y-4">
          {messages.map((msg) => (
            <div key={msg.id} className="space-y-1">
              <ChatBubble role={msg.role} content={msg.content} isProcessing={msg.isProcessing} />
              {msg.intent && (
                <div className="flex items-center space-x-2 text-[11px] text-primary/80 font-mono pl-3">
                  <span className="px-2 py-0.5 rounded bg-primary/10 border border-primary/20">
                    INTENT: {msg.intent}
                  </span>
                  {msg.timestamp && <span className="text-slate-500">{msg.timestamp}</span>}
                </div>
              )}
            </div>
          ))}
          <div ref={messagesEndRef} />
        </div>

        {/* Input Bar */}
        <form onSubmit={handleSubmit} className="p-4 border-t border-white/10 bg-surface/60 flex items-center space-x-3">
          <Input 
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type a command (e.g. 'Open Camera', 'Turn on flashlight')..." 
            className="bg-background border-white/10 flex-1"
            disabled={isSubmitting}
          />
          <Button 
            type="submit" 
            disabled={isSubmitting || !input.trim()}
            className="bg-primary hover:bg-blue-600 text-white rounded-xl px-4 py-2 shadow-lg shadow-primary/25"
          >
            <Send className="w-5 h-5" />
          </Button>
        </form>
      </Card>
    </div>
  );
};

export default Chat;
