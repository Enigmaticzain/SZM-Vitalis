'use client';

import React, { useState } from 'react';
import { runSimulation } from '@/lib/api';
import { Sliders, Play, Award, Zap, Clock } from 'lucide-react';

export default function SimulationsPage() {
  const [scenario, setScenario] = useState('sleep_deprivation');
  const [intensity, setIntensity] = useState(0.8);
  const [duration, setDuration] = useState(12);
  const [result, setResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const handleRun = () => {
    setLoading(true);
    runSimulation(scenario, intensity, duration)
      .then((res) => {
        setResult(res);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div>
        <h2 className="text-3xl font-extrabold text-white">What-If Simulation Sandbox</h2>
        <p className="text-slate-400 text-sm mt-1">Project how lifestyle interventions and stressors alter your longevity metrics.</p>
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
              <option value="sleep_deprivation">Sleep Deprivation</option>
              <option value="marathon_training">Marathon Endurance Training</option>
              <option value="caffeine_overload">Caffeine Overload</option>
              <option value="high_carb_meal">High Carbohydrate Meal Spike</option>
            </select>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300">Intensity: {intensity}</label>
            <input
              type="range"
              min="0.1"
              max="1.0"
              step="0.1"
              value={intensity}
              onChange={(e) => setIntensity(parseFloat(e.target.value))}
              className="w-full accent-cyan-400"
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300">Duration (Hours): {duration}h</label>
            <input
              type="range"
              min="2"
              max="48"
              step="2"
              value={duration}
              onChange={(e) => setDuration(parseInt(e.target.value))}
              className="w-full accent-cyan-400"
            />
          </div>

          <button
            onClick={handleRun}
            disabled={loading}
            className="w-full flex items-center justify-center space-x-2 bg-cyan-600 hover:bg-cyan-500 text-white font-semibold py-3 rounded-xl transition-colors shadow-lg shadow-cyan-900/40"
          >
            <Play className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
            <span>Run Twin Simulation</span>
          </button>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl lg:col-span-2 space-y-6">
          <h3 className="text-lg font-bold text-white">Simulation Results & Projections</h3>
          {result ? (
            <div className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800">
                  <span className="text-xs text-slate-400 block mb-1">Health Score Impact</span>
                  <span className={`text-2xl font-bold ${result.predicted_health_score_impact < 0 ? 'text-rose-400' : 'text-emerald-400'}`}>
                    {result.predicted_health_score_impact}
                  </span>
                </div>
                <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800">
                  <span className="text-xs text-slate-400 block mb-1">Predicted Glucose Peak</span>
                  <span className="text-2xl font-bold text-amber-400">{result.predicted_glucose_peak} mg/dL</span>
                </div>
                <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800">
                  <span className="text-xs text-slate-400 block mb-1">Recovery Time</span>
                  <span className="text-2xl font-bold text-cyan-400">{result.predicted_recovery_time_hours} hours</span>
                </div>
              </div>

              <div>
                <h4 className="text-sm font-semibold text-slate-300 mb-3">Personalized Recommendations</h4>
                <ul className="space-y-2">
                  {result.recommendations.map((rec: string, i: number) => (
                    <li key={i} className="flex items-start space-x-2 text-sm text-slate-400 bg-slate-950 p-3 rounded-xl border border-slate-800">
                      <Award className="h-4 w-4 text-cyan-400 mt-0.5 flex-shrink-0" />
                      <span>{rec}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ) : (
            <div className="text-center py-24 text-slate-500">
              Configure parameters and run simulation to view predictive telemetry impact.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
