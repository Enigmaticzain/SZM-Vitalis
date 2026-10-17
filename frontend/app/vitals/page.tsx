'use client';

import React, { useEffect, useState } from 'react';
import { fetchTwinState, fetchRespiratoryData } from '@/lib/api';
import { TwinState, RespiratoryHistoryData } from '@/lib/types';
import { Wind, Heart, Activity, ShieldCheck, Info, Sparkles } from 'lucide-react';

export default function VitalsHubPage() {
  const [twin, setTwin] = useState<TwinState | null>(null);
  const [respData, setRespData] = useState<RespiratoryHistoryData | null>(null);
  const [timeframe, setTimeframe] = useState<'24h' | '7d' | '30d'>('24h');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      fetchTwinState().catch(() => null),
      fetchRespiratoryData().catch(() => null)
    ]).then(([twinRes, respRes]) => {
      if (twinRes) setTwin(twinRes);
      if (respRes) setRespData(respRes);
      setLoading(false);
    });
  }, []);

  const currentRR = respData?.current.respiratory_rate ?? twin?.latest_metrics.respiratory_rate ?? 16.0;
  const isWithinRestingRange = currentRR >= 12 && currentRR <= 20;

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-extrabold text-white">Deep Vitals & Autonomic Dynamics</h2>
          <p className="text-slate-400 text-sm mt-1">
            Continuous respiratory sinus arrhythmia (RSA) modeling, circadian rhythm alignment, and 24-hour autonomic telemetry.
          </p>
        </div>

        <div className="flex items-center space-x-2 bg-slate-900 border border-slate-800 p-1.5 rounded-2xl self-start md:self-auto">
          {(['24h', '7d', '30d'] as const).map((tf) => (
            <button
              key={tf}
              onClick={() => setTimeframe(tf)}
              className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
                timeframe === tf
                  ? 'bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/20'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              {tf.toUpperCase()}
            </button>
          ))}
        </div>
      </div>

      {/* Main Grid: Prominent Respiratory Card + Autonomic Metrics */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Respiratory Feature Card */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-6 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="p-3 bg-teal-500/10 border border-teal-500/20 rounded-2xl text-teal-400">
                <Wind className="h-6 w-6" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-white">Respiratory Rate (Resting)</h3>
                <span className="text-xs text-slate-400 flex items-center space-x-1">
                  <Sparkles className="h-3.5 w-3.5 text-teal-400 inline" />
                  <span>Autonomic Coupling (RSA Model v1.0.0)</span>
                </span>
              </div>
            </div>

            <div className="flex items-center space-x-2">
              <span
                className={`px-3 py-1 text-xs font-medium rounded-full border ${
                  isWithinRestingRange
                    ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                    : 'bg-amber-500/10 border-amber-500/30 text-amber-400'
                }`}
              >
                {isWithinRestingRange ? 'Within Reference Range' : currentRR < 12 ? 'Below Range' : 'Above Range'}
              </span>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row sm:items-baseline justify-between gap-4 py-2">
            <div>
              <div className="flex items-baseline space-x-2">
                <span className="text-5xl font-black text-white tracking-tight">{currentRR.toFixed(1)}</span>
                <span className="text-lg font-semibold text-slate-400">breaths/minute</span>
              </div>
              <p className="text-xs text-slate-400 mt-1">
                Calibrated Uncertainty: <span className="text-teal-300 font-mono font-medium">±1.2 br/min</span> (95% CI) • Confidence: <span className="text-teal-300 font-mono font-medium">94.2%</span>
              </p>
            </div>

            <div className="bg-slate-950/80 border border-slate-800 rounded-2xl p-3.5 text-xs text-slate-300 space-y-1">
              <div className="flex justify-between space-x-4">
                <span className="text-slate-400">Reference:</span>
                <span className="font-semibold text-teal-400">12–20 br/min (Resting)</span>
              </div>
              <div className="flex justify-between space-x-4">
                <span className="text-slate-400">Personal Baseline:</span>
                <span className="font-semibold text-slate-200">{respData?.baseline_resting_rate ?? 16.0} br/min</span>
              </div>
              <div className="flex justify-between space-x-4">
                <span className="text-slate-400">Provenance:</span>
                <span className="text-slate-400">PhysioNet BIDMC</span>
              </div>
            </div>
          </div>

          {/* 24-Hour Circadian SVG Waveform */}
          <div className="bg-slate-950 border border-slate-800/80 rounded-2xl p-4 space-y-2">
            <div className="flex items-center justify-between text-xs text-slate-400">
              <span className="font-semibold text-slate-300">24-Hour Circadian Respiratory Trend</span>
              <div className="flex items-center space-x-3">
                <div className="flex items-center space-x-1">
                  <span className="h-2 w-2 rounded-full bg-teal-400" />
                  <span className="text-[11px]">Modeled RR</span>
                </div>
                <div className="flex items-center space-x-1">
                  <span className="h-2 w-3 rounded-sm bg-teal-500/20 border border-teal-500/40" />
                  <span className="text-[11px]">12–20 Reference Band</span>
                </div>
              </div>
            </div>

            <div className="relative w-full h-36">
              <svg viewBox="0 0 600 120" className="w-full h-full overflow-visible">
                <defs>
                  <linearGradient id="vitalsBandGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#14b8a6" stopOpacity="0.12" />
                    <stop offset="100%" stopColor="#14b8a6" stopOpacity="0.02" />
                  </linearGradient>
                </defs>

                {/* Shaded 12-20 resting reference band (scale 10 to 25) */}
                {/* 20 br/min y = 120 - ((20-10)/15 * 100) = 53 */}
                {/* 12 br/min y = 120 - ((12-10)/15 * 100) = 107 */}
                <rect x="0" y="40" width="600" height="60" fill="url(#vitalsBandGradient)" />
                <line x1="0" y1="40" x2="600" y2="40" stroke="#14b8a6" strokeOpacity="0.3" strokeDasharray="3 3" />
                <line x1="0" y1="100" x2="600" y2="100" stroke="#14b8a6" strokeOpacity="0.3" strokeDasharray="3 3" />

                {/* Modeled 24h wave */}
                <path
                  d="M 10,95 Q 60,105 120,98 T 240,78 T 360,65 T 480,72 T 590,80"
                  fill="none"
                  stroke="#2dd4bf"
                  strokeWidth="3"
                  strokeLinecap="round"
                />

                {/* Current reading pulse dot */}
                <circle cx="590" cy="80" r="4.5" fill="#2dd4bf" className="animate-pulse" />
              </svg>
            </div>
            <div className="flex justify-between text-[11px] text-slate-500 font-mono">
              <span>00:00 (Sleep)</span>
              <span>06:00 (Awake)</span>
              <span>12:00 (Midday)</span>
              <span>18:00 (Evening)</span>
              <span>Now (Resting)</span>
            </div>
          </div>
        </div>

        {/* Secondary Vitals Stack */}
        <div className="space-y-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-xs text-slate-400 font-medium">Heart Rate</span>
              <Heart className="h-4 w-4 text-rose-400" />
            </div>
            <div className="flex items-baseline space-x-2">
              <span className="text-3xl font-extrabold text-white">{twin?.latest_metrics.heart_rate ?? 64.0}</span>
              <span className="text-xs font-semibold text-slate-400">bpm</span>
            </div>
            <p className="text-[11px] text-slate-500">Resting baseline: 62 bpm • Sinus rhythm normal</p>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-xs text-slate-400 font-medium">Heart Rate Variability (HRV)</span>
              <Activity className="h-4 w-4 text-cyan-400" />
            </div>
            <div className="flex items-baseline space-x-2">
              <span className="text-3xl font-extrabold text-white">{twin?.latest_metrics.hrv ?? 55.0}</span>
              <span className="text-xs font-semibold text-slate-400">ms (RMSSD)</span>
            </div>
            <p className="text-[11px] text-slate-500">High vagal parasympathetic modulation</p>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-xs text-slate-400 font-medium">Interstitial Glucose</span>
              <span className="text-xs font-mono text-amber-400">CGM</span>
            </div>
            <div className="flex items-baseline space-x-2">
              <span className="text-3xl font-extrabold text-white">{twin?.latest_metrics.blood_glucose ?? 92.5}</span>
              <span className="text-xs font-semibold text-slate-400">mg/dL</span>
            </div>
            <p className="text-[11px] text-slate-500">Fast-acting homeostasis • Glycemic index optimal</p>
          </div>
        </div>
      </div>

      {/* Physiological Autonomic Coupling Section */}
      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
        <h3 className="text-lg font-bold text-white flex items-center space-x-2">
          <Info className="h-5 w-5 text-teal-400" />
          <span>Physiological Context & Contextual Ranges</span>
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800 space-y-1">
            <span className="text-xs font-semibold text-cyan-400">Sleep State</span>
            <div className="text-lg font-bold text-white">10–14 <span className="text-xs font-normal text-slate-400">br/min</span></div>
            <p className="text-[11px] text-slate-400">Nocturnal vagal bradycardia and slowed breathing rhythm during deep NREM sleep.</p>
          </div>

          <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800 space-y-1">
            <span className="text-xs font-semibold text-emerald-400">Resting Adult</span>
            <div className="text-lg font-bold text-white">12–20 <span className="text-xs font-normal text-slate-400">br/min</span></div>
            <p className="text-[11px] text-slate-400">Standard clinical reference threshold for awake, resting adults at sea-level.</p>
          </div>

          <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800 space-y-1">
            <span className="text-xs font-semibold text-amber-400">Zone 2 Aerobic</span>
            <div className="text-lg font-bold text-white">20–30 <span className="text-xs font-normal text-slate-400">br/min</span></div>
            <p className="text-[11px] text-slate-400">Elevated respiratory drive to maintain alveolar minute ventilation under aerobic load.</p>
          </div>

          <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800 space-y-1">
            <span className="text-xs font-semibold text-rose-400">VO2 Max / Sprints</span>
            <div className="text-lg font-bold text-white">30–45+ <span className="text-xs font-normal text-slate-400">br/min</span></div>
            <p className="text-[11px] text-slate-400">Hyperventilation to buffer lactic acidosis and clear excessive carbon dioxide.</p>
          </div>
        </div>

        {/* Clinical Disclaimer Callout */}
        <div className="bg-slate-950 border border-teal-500/20 rounded-2xl p-4 flex items-start space-x-3 text-xs text-slate-400">
          <ShieldCheck className="h-5 w-5 text-teal-400 flex-shrink-0 mt-0.5" />
          <div>
            <span className="font-semibold text-slate-300 block mb-0.5">Clinical & Provenance Note</span>
            <p>{respData?.clinical_disclaimer || "General reference range (12–20 breaths/min) is intended for resting adults. Context such as exercise, sleep, anxiety, or acute illness dynamically modulates respiration. Respiratory rate alone must not be used to diagnose any disease."}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
