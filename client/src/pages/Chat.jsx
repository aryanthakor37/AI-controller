import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Send, Mic } from 'lucide-react';
import { Card } from '../components/atoms/Card';
import { Input } from '../components/atoms/Input';
import { Button } from '../components/atoms/Button';
import { ChatBubble } from '../components/molecules/ChatBubble';
import { addMessage } from '../redux/slices/chatSlice';
import { fetchHistory } from '../redux/slices/commandSlice';
import socketService from '../services/socketService';

const Chat = () => {
  const { messages } = useSelector((state) => state.chat);
  const dispatch = useDispatch();
  const [input, setInput] = useState('');

  const handleSend = (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    dispatch(addMessage({ id: Date.now().toString(), role: 'user', content: input }));
    setInput('');
    // Mock processing state
    dispatch(addMessage({ id: (Date.now()+1).toString(), role: 'ai', content: 'Command received. Processing...', isProcessing: true }));

    // Real AI response
    import('../services/api').then(({ default: api }) => {
      api.post('/ai/parse-command', { command: input })
        .then(response => {
          const { intent, app, contact, reply } = response.data.data;
          let msg = `Intent Executed: ${intent}`;
          if (app) msg += `\nApp: ${app}`;
          if (contact) msg += `\nContact: ${contact}`;
          if (reply) msg = reply;

          dispatch(addMessage({ id: Date.now().toString(), role: 'ai', content: msg }));
          socketService.sendCommand('all', response.data.data);
          dispatch(fetchHistory());
        })
        .catch(error => {
          dispatch(addMessage({ id: Date.now().toString(), role: 'ai', content: 'Error: Failed to reach AI Engine.' }));
        });
    });
  };

  return (
    <div className="h-full flex flex-col">
      <div className="mb-6">
        <h2 className="text-2xl font-bold tracking-tight">AI Assistant</h2>
        <p className="text-slate-400">Type or speak your commands.</p>
      </div>

      <Card className="flex-1 flex flex-col p-0 overflow-hidden bg-surface/30">
        <div className="flex-1 overflow-y-auto p-6 space-y-4">
          {messages.map((msg) => (
            <ChatBubble key={msg.id} role={msg.role} content={msg.content} />
          ))}
        </div>

        <form onSubmit={handleSend} className="p-4 border-t border-white/5 bg-surface/50 flex items-center space-x-2">
          <Button type="button" variant="ghost" size="icon" className="rounded-full text-slate-400 hover:text-white">
            <Mic className="w-5 h-5" />
          </Button>
          <Input 
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type a command..." 
            className="bg-background border-none flex-1"
          />
          <Button type="submit" size="icon" className="rounded-full">
            <Send className="w-5 h-5" />
          </Button>
        </form>
      </Card>
    </div>
  );
};

export default Chat;
