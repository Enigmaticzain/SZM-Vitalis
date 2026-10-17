'use client';

import React, { useEffect, useState } from 'react';
import { fetchTwinState, fetchRespiratoryData } from '@/lib/api';
import { TwinState, RespiratoryTelemetry } from '@/lib/types';
import VitalCard from '@/components/VitalCard';
import RespiratoryVitalCard from '@/components/RespiratoryVitalCard';
import { Heart, Activity, Zap, Moon, Flame, TrendingUp, ShieldCheck } from 'lucide-react';

export default function DashboardPage() {
  const [twin, setTwin] = useState<TwinState | null>(null);
  const [respiratory, setRespiratory] = useState<RespiratoryTelemetry | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.allSettled([fetchTwinState(), fetchRespiratoryData()])
      .then(([twinRes, respRes]) => {
        if (twinRes.status === 'fulfilled') {
          setTwin(twinRes.value);
        }
        if (respRes.status === 'fulfilled') {
          setRespiratory(respRes.value);
        }
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div className="text-center py-20 text-slate-400">Loading Digital Twin State...</div>;
  }

  const metrics = twin?.latest_metrics;

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div className="bg-gradient-to-r from-slate-900 via-slate-900 to-cyan-950 border border-slate-800 rounded-3xl p-8 shadow-2xl flex flex-col md:flex-row items-center justify-between">
        <div className="space-y-3">
          <div className="inline-flex items-center space-x-2 bg-cyan-500/10 border border-cyan-500/20 px-3 py-1 rounded-full">
            <ShieldCheck className="h-4 w-4 text-cyan-400" />
            <span className="text-xs font-semibold text-cyan-300">Active Longevity Twin</span>
          </div>
          <h2 className="text-3xl font-extrabold text-white">Alex Mercer's Digital Twin</h2>
          <p className="text-slate-400 max-w-xl text-sm">{twin?.current_status}</p>
        </div>
        <div className="mt-6 md:mt-0 flex space-x-6 bg-slate-950/60 p-6 rounded-2xl border border-slate-800">
          <div className="text-center">
            <span className="text-xs text-slate-400 block mb-1">Health Score</span>
            <span className="text-4xl font-extrabold text-cyan-400">{twin?.health_score}</span>
            <span className="text-xs text-slate-500 block mt-1">/ 100</span>
          </div>
          <div className="w-px bg-slate-800"></div>
          <div className="text-center">
            <span className="text-xs text-slate-400 block mb-1">Biological Age</span>
            <span className="text-4xl font-extrabold text-emerald-400">{twin?.biological_age}</span>
            <span className="text-xs text-slate-500 block mt-1">Chronological: 32</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <VitalCard
          title="Heart Rate"
          value={metrics?.heart_rate ?? 64.5}
          unit="bpm"
          icon={Heart}
          statusColor="text-rose-400"
          description="Resting average stable"
        />
        <VitalCard
          title="Heart Rate Variability"
          value={metrics?.hrv ?? 74.2}
          unit="ms"
          icon={Activity}
          statusColor="text-emerald-400"
          description="High autonomic resilience"
        />
        <VitalCard
          title="Blood Glucose"
          value={metrics?.blood_glucose ?? 92.5}
          unit="mg/dL"
          icon={Zap}
          statusColor="text-amber-400"
          description="Optimal post-absorptive state"
        />
        <VitalCard
          title="Sleep Duration"
          value={metrics?.sleep_hours ?? 7.8}
          unit="hours"
          icon={Moon}
          statusColor="text-indigo-400"
          description="89% sleep efficiency"
        />
      </div>

      {/* Respiratory Rate Vitals Card & Historical Trend */}
      <div>
        <RespiratoryVitalCard
          value={respiratory?.current?.respiratory_rate ?? metrics?.respiratory_rate ?? 15.8}
          predictedValue={respiratory?.current?.respiratory_rate ?? 15.8}
          uncertainty={respiratory?.current?.uncertainty ?? 1.2}
          confidence={respiratory?.current?.confidence ?? 94.2}
          status={respiratory?.current?.status ?? metrics?.respiratory_rate_status ?? "Within Reference Range (12–20)"}
          timestamp={respiratory?.current?.timestamp ? new Date(respiratory.current.timestamp).toLocaleTimeString() : "Just now"}
          source={respiratory?.current?.provenance_source ?? "Predicted (PhysioNet BIDMC Benchmark Regressor v1.0)"}
          history={respiratory?.history_24h}
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-white flex items-center space-x-2">
              <TrendingUp className="h-5 w-5 text-cyan-400" />
              <span>Metabolic & Recovery Index</span>
            </h3>
            <span className="text-xs text-slate-400">Real-time Stream</span>
          </div>
          <div className="space-y-4">
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-slate-400">Metabolic Flexibility</span>
                <span className="text-cyan-400 font-semibold">{twin?.metabolic_flexibility}%</span>
              </div>
              <div className="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden">
                <div className="bg-cyan-500 h-2.5 rounded-full" style={{ width: `${twin?.metabolic_flexibility}%` }}></div>
              </div>
            </div>
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-slate-400">Recovery Index</span>
                <span className="text-emerald-400 font-semibold">{twin?.recovery_index}%</span>
              </div>
              <div className="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden">
                <div className="bg-emerald-500 h-2.5 rounded-full" style={{ width: `${twin?.recovery_index}%` }}></div>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-white flex items-center space-x-2">
              <Flame className="h-5 w-5 text-amber-400" />
              <span>Daily Activity & Expenditure</span>
            </h3>
            <span className="text-xs text-slate-400">Today</span>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800">
              <span className="text-xs text-slate-400 block mb-1">Total Steps</span>
              <span className="text-2xl font-bold text-white">{metrics?.steps ?? 6420}</span>
              <span className="text-xs text-emerald-400 mt-1 block">Goal: 10,000</span>
            </div>
            <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800">
              <span className="text-xs text-slate-400 block mb-1">Energy Burned</span>
              <span className="text-2xl font-bold text-white">{metrics?.energy_expenditure_kcal ?? 2140}</span>
              <span className="text-xs text-amber-400 mt-1 block">kcal</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
