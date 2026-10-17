'use client';

import React, { useState, useEffect } from 'react';
import { fetchChatHistory, sendChatMessage } from '@/lib/api';
import { ChatMessage } from '@/lib/types';
import { Send, Bot, User } from 'lucide-react';

export default function ChatPage() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  const loadChat = () => {
    fetchChatHistory().then(setMessages).catch(console.error);
  };

  useEffect(() => {
    loadChat();
  }, []);

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || loading) return;
    const msg = input;
    setInput('');
    setLoading(true);

    sendChatMessage(msg)
      .then(() => {
        loadChat();
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto h-[calc(100vh-140px)] flex flex-col">
      <div>
        <h2 className="text-3xl font-extrabold text-white">Twin AI Longevity Coach</h2>
        <p className="text-slate-400 text-sm mt-1">Chat directly with your digital twin AI trained on your continuous biometric telemetry.</p>
      </div>

      <div className="flex-1 bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl flex flex-col justify-between overflow-hidden">
        <div className="flex-1 overflow-y-auto space-y-4 pr-2">
          {messages.map((m, i) => (
            <div key={i} className={`flex items-start space-x-3 ${m.sender === 'user' ? 'flex-row-reverse space-x-reverse' : ''}`}>
              <div className={`p-2 rounded-xl flex-shrink-0 ${m.sender === 'user' ? 'bg-cyan-600' : 'bg-slate-800 border border-slate-700'}`}>
                {m.sender === 'user' ? <User className="h-5 w-5 text-white" /> : <Bot className="h-5 w-5 text-cyan-400" />}
              </div>
              <div className={`max-w-md p-4 rounded-2xl text-sm ${m.sender === 'user' ? 'bg-cyan-600 text-white rounded-tr-none' : 'bg-slate-950 border border-slate-800 text-slate-200 rounded-tl-none'}`}>
                <p>{m.message}</p>
                <span className="text-[10px] text-slate-400 mt-1 block">{new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
              </div>
            </div>
          ))}
        </div>

        <form onSubmit={handleSend} className="mt-4 flex items-center space-x-3 pt-4 border-t border-slate-800">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask your twin about sleep, glucose, workouts..."
            className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-cyan-500"
          />
          <button
            type="submit"
            disabled={loading}
            className="bg-cyan-600 hover:bg-cyan-500 text-white p-3 rounded-xl transition-colors shadow-lg shadow-cyan-900/40"
          >
            <Send className="h-5 w-5" />
          </button>
        </form>
      </div>
    </div>
  );
}
