import React from 'react';
import { Activity, Shield, User } from 'lucide-react';

export default function Navbar() {
  return (
    <header className="bg-slate-900 border-b border-slate-800 text-white px-6 py-4 flex items-center justify-between">
      <div className="flex items-center space-x-3">
        <Activity className="h-8 w-8 text-cyan-400 animate-pulse" />
        <div>
          <h1 className="text-xl font-bold tracking-wide">Human Digital Twin</h1>
          <p className="text-xs text-slate-400">Personal Longevity & Biometric Intelligence Platform</p>
        </div>
      </div>
      <div className="flex items-center space-x-4">
        <div className="flex items-center space-x-2 bg-slate-800 px-3 py-1.5 rounded-full border border-slate-700">
          <Shield className="h-4 w-4 text-emerald-400" />
          <span className="text-xs font-medium text-emerald-300">Secure Biometric Stream</span>
        </div>
        <div className="flex items-center space-x-2 bg-cyan-950 px-3 py-1.5 rounded-full border border-cyan-800">
          <User className="h-4 w-4 text-cyan-400" />
          <span className="text-xs font-medium text-cyan-200">Alex Mercer (Age 32)</span>
        </div>
      </div>
    </header>
  );
}
