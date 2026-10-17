'use client';

import React, { useState } from 'react';
import { runSimulation } from '@/lib/api';
import { Sliders, Play, Award, Zap, Activity } from 'lucide-react';

export default function SimulationsPage() {
  const [scenario, setScenario] = useState('marathon_training');
  const [intensity, setIntensity] = useState(0.8);
  const [duration, setDuration] = useState(12);
  const [result, setResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [activeCurve, setActiveCurve] = useState<'all' | 'rr' | 'glucose' | 'health'>('all');

  const handleRun = () => {
    setLoading(true);
    runSimulation(scenario, intensity, duration)
      .then((res) => {
        setResult(res);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  // Helper to generate SVG polyline points from time series
  const renderSvgCurve = (points: any[], key: string, minVal: number, maxVal: number, color: string) => {
    if (!points || points.length < 2) return null;
    const width = 600;
    const height = 180;
    const padding = 20;

    const coords = points.map((p, i) => {
      const x = padding + (i / (points.length - 1)) * (width - 2 * padding);
      const val = p[key] ?? minVal;
      const normalized = Math.max(0, Math.min(1, (val - minVal) / (maxVal - minVal || 1)));
      const y = height - padding - normalized * (height - 2 * padding);
      return `${x},${y}`;
    }).join(' ');

    return (
      <polyline
        fill="none"
        stroke={color}
        strokeWidth="2.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        points={coords}
      />
    );
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div>
        <h2 className="text-3xl font-extrabold text-white">What-If Simulation Sandbox</h2>
        <p className="text-slate-400 text-sm mt-1">
          Project how lifestyle interventions, hypoxic stressors, and autonomic perturbations alter respiratory kinetics and digital twin homeostasis.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-6 lg:col-span-1">
          <h3 className="text-lg font-bold text-white flex items-center space-x-2">
            <Sliders className="h-5 w-5 text-cyan-400" />
            <span>Scenario Parameters</span>
          </h3>

          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300">Scenario Type</label>
            <select
              value={scenario}
              onChange={(e) => setScenario(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-cyan-500"
            >
              <option value="marathon_training">Marathon Endurance Training</option>
              <option value="sleep_deprivation">Sleep Deprivation & Autonomic Strain</option>
              <option value="altitude_exposure">High Altitude Hypoxia Challenge</option>
              <option value="caffeine_overload">Caffeine Overload & Sympathetic Drive</option>
              <option value="high_carb_meal">High Carbohydrate Meal Spike</option>
              <option value="intermittent_fasting">Intermittent Fasting & Ketosis</option>
            </select>
          </div>

          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-slate-300 font-medium">Intensity Factor</span>
              <span className="text-cyan-400 font-mono font-bold">{Math.round(intensity * 100)}%</span>
            </div>
            <input
              type="range"
              min="0.1"
              max="1.0"
              step="0.1"
              value={intensity}
              onChange={(e) => setIntensity(parseFloat(e.target.value))}
              className="w-full accent-cyan-400 cursor-pointer"
            />
          </div>

          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-slate-300 font-medium">Duration Window</span>
              <span className="text-cyan-400 font-mono font-bold">{duration} hours</span>
            </div>
            <input
              type="range"
              min="2"
              max="48"
              step="2"
              value={duration}
              onChange={(e) => setDuration(parseInt(e.target.value))}
              className="w-full accent-cyan-400 cursor-pointer"
            />
          </div>

          <button
            onClick={handleRun}
            disabled={loading}
            className="w-full flex items-center justify-center space-x-2 bg-gradient-to-r from-cyan-600 to-teal-500 hover:from-cyan-500 hover:to-teal-400 text-white font-semibold py-3.5 rounded-xl transition-all shadow-lg shadow-cyan-900/40"
          >
            <Play className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
            <span>{loading ? 'Simulating Homeostasis...' : 'Run Twin Simulation'}</span>
          </button>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl lg:col-span-2 space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <h3 className="text-lg font-bold text-white">Simulation Results & Physiological Curves</h3>
            {result && (
              <div className="flex items-center space-x-1.5 bg-slate-950 p-1 rounded-xl border border-slate-800 text-xs">
                <button
                  onClick={() => setActiveCurve('all')}
                  className={`px-2.5 py-1 rounded-lg transition-colors ${activeCurve === 'all' ? 'bg-cyan-500 text-slate-950 font-bold' : 'text-slate-400'}`}
                >
                  All
                </button>
                <button
                  onClick={() => setActiveCurve('rr')}
                  className={`px-2.5 py-1 rounded-lg transition-colors ${activeCurve === 'rr' ? 'bg-teal-400 text-slate-950 font-bold' : 'text-slate-400'}`}
                >
                  Respiration
                </button>
                <button
                  onClick={() => setActiveCurve('glucose')}
                  className={`px-2.5 py-1 rounded-lg transition-colors ${activeCurve === 'glucose' ? 'bg-amber-400 text-slate-950 font-bold' : 'text-slate-400'}`}
                >
                  Glucose
                </button>
                <button
                  onClick={() => setActiveCurve('health')}
                  className={`px-2.5 py-1 rounded-lg transition-colors ${activeCurve === 'health' ? 'bg-cyan-400 text-slate-950 font-bold' : 'text-slate-400'}`}
                >
                  Health Score
                </button>
              </div>
            )}
          </div>

          {result ? (
            <div className="space-y-6">
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3">
                <div className="bg-slate-950 p-3.5 rounded-2xl border border-slate-800">
                  <span className="text-[11px] text-slate-400 block mb-1">Health Impact</span>
                  <span className={`text-xl font-bold ${result.predicted_health_score_impact < 0 ? 'text-rose-400' : 'text-emerald-400'}`}>
                    {result.predicted_health_score_impact > 0 ? `+${result.predicted_health_score_impact}` : result.predicted_health_score_impact}
                  </span>
                </div>
                <div className="bg-slate-950 p-3.5 rounded-2xl border border-slate-800">
                  <span className="text-[11px] text-slate-400 block mb-1">Glucose Peak</span>
                  <span className="text-xl font-bold text-amber-400">{result.predicted_glucose_peak} <span className="text-xs font-normal">mg/dL</span></span>
                </div>
                <div className="bg-slate-950 p-3.5 rounded-2xl border border-slate-800">
                  <span className="text-[11px] text-slate-400 block mb-1">Respiratory Peak</span>
                  <span className="text-xl font-bold text-teal-400">{result.predicted_respiratory_peak ?? 16.0} <span className="text-xs font-normal">br/min</span></span>
                </div>
                <div className="bg-slate-950 p-3.5 rounded-2xl border border-slate-800">
                  <span className="text-[11px] text-slate-400 block mb-1">Heart Rate Peak</span>
                  <span className="text-xl font-bold text-rose-400">{result.predicted_heart_rate_peak ?? 64.0} <span className="text-xs font-normal">bpm</span></span>
                </div>
                <div className="bg-slate-950 p-3.5 rounded-2xl border border-slate-800 col-span-2 sm:col-span-1">
                  <span className="text-[11px] text-slate-400 block mb-1">Recovery Time</span>
                  <span className="text-xl font-bold text-cyan-400">{result.predicted_recovery_time_hours} <span className="text-xs font-normal">hours</span></span>
                </div>
              </div>

              {/* Time-series Dynamic Projection SVG */}
              {result.time_series_projection && result.time_series_projection.length > 0 && (
                <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800 space-y-3">
                  <div className="flex items-center justify-between text-xs text-slate-400">
                    <span className="font-semibold text-slate-300">Predictive Kinetics Projection ({duration}h)</span>
                    <div className="flex items-center space-x-3">
                      {(activeCurve === 'all' || activeCurve === 'rr') && (
                        <div className="flex items-center space-x-1">
                          <span className="h-2 w-2 rounded-full bg-teal-400" />
                          <span className="text-[11px]">Respiration</span>
                        </div>
                      )}
                      {(activeCurve === 'all' || activeCurve === 'glucose') && (
                        <div className="flex items-center space-x-1">
                          <span className="h-2 w-2 rounded-full bg-amber-400" />
                          <span className="text-[11px]">Glucose</span>
                        </div>
                      )}
                      {(activeCurve === 'all' || activeCurve === 'health') && (
                        <div className="flex items-center space-x-1">
                          <span className="h-2 w-2 rounded-full bg-cyan-400" />
                          <span className="text-[11px]">Health Score</span>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="relative w-full overflow-hidden">
                    <svg viewBox="0 0 600 180" className="w-full h-44 drop-shadow">
                      {/* Grid lines */}
                      <line x1="20" y1="30" x2="580" y2="30" stroke="#1e293b" strokeDasharray="3 3" />
                      <line x1="20" y1="90" x2="580" y2="90" stroke="#1e293b" strokeDasharray="3 3" />
                      <line x1="20" y1="150" x2="580" y2="150" stroke="#1e293b" strokeDasharray="3 3" />

                      {/* Render curves */}
                      {(activeCurve === 'all' || activeCurve === 'health') &&
                        renderSvgCurve(result.time_series_projection, 'simulated_health_score', 40, 100, '#06b6d4')}
                      {(activeCurve === 'all' || activeCurve === 'glucose') &&
                        renderSvgCurve(result.time_series_projection, 'simulated_glucose', 65, 220, '#fbbf24')}
                      {(activeCurve === 'all' || activeCurve === 'rr') &&
                        renderSvgCurve(result.time_series_projection, 'simulated_respiratory_rate', 10, 45, '#2dd4bf')}
                    </svg>
                  </div>
                  <div className="flex justify-between text-[11px] text-slate-500 font-mono px-2">
                    <span>Hour 0 (Baseline)</span>
                    <span>Hour {Math.round(duration / 2)} (Peak Stress)</span>
                    <span>Hour {duration} (Recovery)</span>
                  </div>
                </div>
              )}

              <div>
                <h4 className="text-sm font-semibold text-slate-300 mb-3 flex items-center space-x-2">
                  <Award className="h-4 w-4 text-cyan-400" />
                  <span>Personalized Recovery Protocol</span>
                </h4>
                <ul className="space-y-2">
                  {result.recommendations.map((rec: string, i: number) => (
                    <li key={i} className="flex items-start space-x-3 text-sm text-slate-300 bg-slate-950 p-3.5 rounded-xl border border-slate-800">
                      <Zap className="h-4 w-4 text-cyan-400 mt-0.5 flex-shrink-0" />
                      <span>{rec}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ) : (
            <div className="text-center py-28 text-slate-500 space-y-2">
              <Activity className="h-10 w-10 text-slate-600 mx-auto animate-pulse" />
              <p className="text-slate-400 font-medium">Select a scenario and click "Run Twin Simulation" to project multivariable physiological trajectories.</p>
              <p className="text-xs text-slate-600">Calculates respiratory dynamics, glycemic response, and autonomic recovery time.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
