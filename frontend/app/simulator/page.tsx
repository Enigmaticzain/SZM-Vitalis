'use client';

import React, { useState, useEffect } from 'react';
import { fetchWearableData, simulateWearable } from '@/lib/api';
import { WearableData } from '@/lib/types';
import VitalCard from '@/components/VitalCard';
import { Heart, Activity, Zap, Footprints, Play, RefreshCw, Wind } from 'lucide-react';

export default function SimulatorPage() {
  const [data, setData] = useState<WearableData | null>(null);
  const [loading, setLoading] = useState(false);

  const loadData = () => {
    fetchWearableData().then(setData).catch(console.error);
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSimulate = (spike: boolean) => {
    setLoading(true);
    simulateWearable(spike)
      .then((res) => {
        setData(res);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-extrabold text-white">Wearable Telemetry Simulator</h2>
          <p className="text-slate-400 text-sm mt-1">Simulate real-time streaming wearable sensor feeds into your digital twin.</p>
        </div>
        <div className="flex space-x-3">
          <button
            onClick={() => handleSimulate(false)}
            disabled={loading}
            className="flex items-center space-x-2 bg-slate-800 hover:bg-slate-700 text-white px-4 py-2.5 rounded-xl font-medium text-sm transition-colors border border-slate-700"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
            <span>Tick Normal Data</span>
          </button>
          <button
            onClick={() => handleSimulate(true)}
            disabled={loading}
            className="flex items-center space-x-2 bg-cyan-600 hover:bg-cyan-500 text-white px-4 py-2.5 rounded-xl font-medium text-sm transition-colors shadow-lg shadow-cyan-900/40"
          >
            <Play className="h-4 w-4" />
            <span>Simulate Activity Spike</span>
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-6">
        <VitalCard title="Heart Rate" value={data?.heart_rate ?? 0} unit="bpm" icon={Heart} statusColor="text-rose-400" />
        <VitalCard title="HRV" value={data?.hrv ?? 0} unit="ms" icon={Activity} statusColor="text-emerald-400" />
        <VitalCard title="Blood Glucose" value={data?.blood_glucose ?? 0} unit="mg/dL" icon={Zap} statusColor="text-amber-400" />
        <VitalCard title="Respiratory Rate" value={data?.respiratory_rate ?? 16} unit="br/min" icon={Wind} statusColor="text-cyan-400" description={data?.respiratory_rate_status ?? "Optimal Resting"} />
        <VitalCard title="Steps" value={data?.steps ?? 0} unit="steps" icon={Footprints} statusColor="text-indigo-400" />
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <h3 className="text-lg font-bold text-white mb-4">Latest Stream Packet</h3>
        <pre className="bg-slate-950 p-4 rounded-2xl text-cyan-400 text-xs overflow-x-auto border border-slate-800">
          {JSON.stringify(data, null, 2)}
        </pre>
      </div>
    </div>
  );
}
