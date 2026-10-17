'use client';

import React, { useState } from 'react';
import { Wind, Info, ShieldCheck, Activity, AlertCircle } from 'lucide-react';
import { RespiratoryHistoryPoint } from '@/lib/types';

interface RespiratoryVitalCardProps {
  value: number;
  predictedValue?: number;
  uncertainty?: number;
  confidence?: number;
  status?: string;
  timestamp?: string;
  source?: string;
  history?: RespiratoryHistoryPoint[];
}

export default function RespiratoryVitalCard({
  value = 16.0,
  predictedValue = 15.8,
  uncertainty = 1.2,
  confidence = 94.2,
  status = "Within Reference Range",
  timestamp = "Just now",
  source = "Predicted (Autonomic Coupling Model v1.0)",
  history = []
}: RespiratoryVitalCardProps) {
  const [showInfo, setShowInfo] = useState(false);
  const [hoveredPoint, setHoveredPoint] = useState<RespiratoryHistoryPoint | null>(null);

  // Status badge coloring
  const isOptimal = value >= 12.0 && value <= 20.0;
  const isBelow = value < 12.0;
  const statusColor = isOptimal 
    ? "bg-emerald-500/10 text-emerald-400 border-emerald-500/30"
    : isBelow 
      ? "bg-amber-500/10 text-amber-400 border-amber-500/30"
      : "bg-rose-500/10 text-rose-400 border-rose-500/30";

  // Generate fallback 24h history points if none supplied
  const chartPoints = history.length > 0 ? history : [
    { timestamp: "00:00", respiratory_rate: 13.2, is_predicted: true, uncertainty_lower: 12.0, uncertainty_upper: 14.4, reference_min: 12, reference_max: 20, status: "Normal Sleep", context: "Sleep", iso_timestamp: "" },
    { timestamp: "03:00", respiratory_rate: 12.8, is_predicted: true, uncertainty_lower: 11.6, uncertainty_upper: 14.0, reference_min: 12, reference_max: 20, status: "Normal Sleep", context: "Sleep", iso_timestamp: "" },
    { timestamp: "06:00", respiratory_rate: 14.5, is_predicted: true, uncertainty_lower: 13.3, uncertainty_upper: 15.7, reference_min: 12, reference_max: 20, status: "Resting", context: "Resting", iso_timestamp: "" },
    { timestamp: "09:00", respiratory_rate: 15.8, is_predicted: true, uncertainty_lower: 14.6, uncertainty_upper: 17.0, reference_min: 12, reference_max: 20, status: "Resting", context: "Resting", iso_timestamp: "" },
    { timestamp: "12:00", respiratory_rate: 16.5, is_predicted: true, uncertainty_lower: 15.3, uncertainty_upper: 17.7, reference_min: 12, reference_max: 20, status: "Resting", context: "Resting", iso_timestamp: "" },
    { timestamp: "15:00", respiratory_rate: 17.2, is_predicted: true, uncertainty_lower: 15.8, uncertainty_upper: 18.6, reference_min: 12, reference_max: 20, status: "Resting", context: "Resting", iso_timestamp: "" },
    { timestamp: "18:00", respiratory_rate: 16.1, is_predicted: true, uncertainty_lower: 14.9, uncertainty_upper: 17.3, reference_min: 12, reference_max: 20, status: "Resting", context: "Resting", iso_timestamp: "" },
    { timestamp: "21:00", respiratory_rate: 15.0, is_predicted: true, uncertainty_lower: 13.8, uncertainty_upper: 16.2, reference_min: 12, reference_max: 20, status: "Resting", context: "Resting", iso_timestamp: "" },
    { timestamp: "Now", respiratory_rate: value, is_predicted: true, uncertainty_lower: value - uncertainty, uncertainty_upper: value + uncertainty, reference_min: 12, reference_max: 20, status: status, context: "Resting", iso_timestamp: "" }
  ];

  // SVG Chart Dimensions
  const svgWidth = 380;
  const svgHeight = 110;
  const paddingX = 20;
  const paddingY = 15;

  const minVal = 8.0;
  const maxVal = 24.0;

  const getY = (val: number) => {
    const clamped = Math.max(minVal, Math.min(maxVal, val));
    return svgHeight - paddingY - ((clamped - minVal) / (maxVal - minVal)) * (svgHeight - paddingY * 2);
  };

  const getX = (index: number) => {
    return paddingX + (index / (chartPoints.length - 1)) * (svgWidth - paddingX * 2);
  };

  // Build SVG path string
  const pathD = chartPoints.map((pt, i) => `${i === 0 ? 'M' : 'L'} ${getX(i).toFixed(1)} ${getY(pt.respiratory_rate).toFixed(1)}`).join(' ');
  const areaD = `${pathD} L ${getX(chartPoints.length - 1).toFixed(1)} ${svgHeight - paddingY} L ${getX(0).toFixed(1)} ${svgHeight - paddingY} Z`;

  // Normal reference band Y coordinates (12 to 20 bpm)
  const yRefMin = getY(12.0);
  const yRefMax = getY(20.0);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl hover:border-slate-700 transition-all relative overflow-hidden flex flex-col justify-between">
      {/* Top Header */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2.5">
            <div className="p-2.5 bg-cyan-500/10 border border-cyan-500/20 rounded-2xl text-cyan-400">
              <Wind className="h-5 w-5" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h3 className="text-base font-bold text-white">Respiratory Rate</h3>
                <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                  Predicted
                </span>
              </div>
              <p className="text-xs text-slate-400">Autonomic vagal coupling telemetry</p>
            </div>
          </div>

          <button
            onClick={() => setShowInfo(!showInfo)}
            className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition"
            aria-label="Clinical reference info"
            title="View reference bounds & clinical context"
          >
            <Info className="h-4 w-4" />
          </button>
        </div>

        {/* Headline Value & Reference Status Badge */}
        <div className="flex items-baseline justify-between mb-3">
          <div className="flex items-baseline space-x-2">
            <span className="text-4xl font-extrabold text-white">{value}</span>
            <span className="text-sm font-medium text-slate-400">breaths/min</span>
          </div>

          <div className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${statusColor} flex items-center space-x-1.5`}>
            <span className={`h-1.5 w-1.5 rounded-full ${isOptimal ? 'bg-emerald-400 animate-pulse' : isBelow ? 'bg-amber-400' : 'bg-rose-400'}`}></span>
            <span>{isOptimal ? 'Within Reference (12–20)' : status}</span>
          </div>
        </div>

        {/* Prediction Uncertainty & Timestamp Strip */}
        <div className="flex flex-wrap items-center justify-between text-xs text-slate-400 bg-slate-950/60 rounded-xl px-3 py-2 mb-4 border border-slate-800/80">
          <div className="flex items-center space-x-1.5">
            <ShieldCheck className="h-3.5 w-3.5 text-cyan-400" />
            <span>Uncertainty: <strong className="text-slate-200">±{uncertainty} br/min</strong></span>
            <span className="text-slate-600">|</span>
            <span>Confidence: <strong className="text-cyan-300">{confidence}%</strong></span>
          </div>
          <span className="text-[11px] text-slate-500">{timestamp}</span>
        </div>

        {/* 24-Hour Historical Wave Graph */}
        <div className="bg-slate-950/80 rounded-2xl p-3 border border-slate-800/80 mb-3">
          <div className="flex justify-between items-center text-[11px] text-slate-400 mb-1">
            <span className="font-semibold flex items-center space-x-1">
              <Activity className="h-3 w-3 text-cyan-400" />
              <span>24-Hour Historical Trend</span>
            </span>
            <span className="text-slate-500">Resting Adult Band (12–20)</span>
          </div>

          <div className="relative">
            <svg viewBox={`0 0 ${svgWidth} ${svgHeight}`} className="w-full h-24 overflow-visible">
              <defs>
                {/* Gradient for area fill */}
                <linearGradient id="rrAreaGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#06b6d4" stopOpacity="0.35" />
                  <stop offset="100%" stopColor="#06b6d4" stopOpacity="0.0" />
                </linearGradient>
              </defs>

              {/* Shaded 12-20 normal resting adult reference corridor */}
              <rect
                x={paddingX}
                y={yRefMax}
                width={svgWidth - paddingX * 2}
                height={yRefMin - yRefMax}
                fill="#10b981"
                fillOpacity="0.07"
                rx="4"
              />

              {/* Dotted Reference Lines */}
              <line
                x1={paddingX}
                y1={yRefMin}
                x2={svgWidth - paddingX}
                y2={yRefMin}
                stroke="#10b981"
                strokeWidth="1"
                strokeDasharray="3 3"
                strokeOpacity="0.4"
              />
              <line
                x1={paddingX}
                y1={yRefMax}
                x2={svgWidth - paddingX}
                y2={yRefMax}
                stroke="#10b981"
                strokeWidth="1"
                strokeDasharray="3 3"
                strokeOpacity="0.4"
              />

              {/* Reference Band Labels */}
              <text x={paddingX - 2} y={yRefMin + 3} fill="#10b981" fontSize="8" textAnchor="end" opacity="0.6">12</text>
              <text x={paddingX - 2} y={yRefMax + 3} fill="#10b981" fontSize="8" textAnchor="end" opacity="0.6">20</text>

              {/* Area fill */}
              <path d={areaD} fill="url(#rrAreaGradient)" />

              {/* Main Line */}
              <path
                d={pathD}
                fill="none"
                stroke="#22d3ee"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />

              {/* Interactive Data Points */}
              {chartPoints.map((pt, i) => (
                <circle
                  key={i}
                  cx={getX(i)}
                  cy={getY(pt.respiratory_rate)}
                  r={hoveredPoint?.timestamp === pt.timestamp ? 5 : 3}
                  className="transition-all cursor-pointer fill-cyan-400 stroke-slate-900"
                  strokeWidth="2"
                  onMouseEnter={() => setHoveredPoint(pt)}
                  onMouseLeave={() => setHoveredPoint(null)}
                />
              ))}
            </svg>

            {/* Hover Tooltip */}
            {hoveredPoint && (
              <div className="absolute top-1 right-2 bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-1 text-[11px] shadow-lg pointer-events-none">
                <span className="text-slate-400">{hoveredPoint.timestamp}: </span>
                <strong className="text-cyan-300">{hoveredPoint.respiratory_rate} br/min</strong>
                <span className="text-slate-400"> ({hoveredPoint.context})</span>
              </div>
            )}
          </div>

          {/* Time axis */}
          <div className="flex justify-between text-[10px] text-slate-500 mt-1 px-2">
            <span>00:00</span>
            <span>06:00</span>
            <span>12:00</span>
            <span>18:00</span>
            <span>Now</span>
          </div>
        </div>
      </div>

      {/* Clinical Context & Educational Accordion / Notice */}
      {showInfo && (
        <div className="mt-2 p-3.5 bg-slate-950 rounded-2xl border border-slate-800 text-xs text-slate-300 space-y-2 animate-in fade-in duration-200">
          <div className="flex items-center space-x-1.5 text-cyan-400 font-semibold">
            <Info className="h-4 w-4" />
            <span>Clinical Reference & Context Guidelines</span>
          </div>
          <p className="text-slate-400 leading-relaxed text-[11px]">
            <strong>General Reference Range:</strong> 12–20 breaths per minute for a typical <em>resting adult</em>.
          </p>
          <ul className="list-disc list-inside text-slate-400 space-y-1 text-[11px]">
            <li><strong>Sleep:</strong> Nocturnal rates naturally decelerate to 10–14 br/min.</li>
            <li><strong>Exertion / Stress:</strong> Respiration dynamically increases to 20–35+ br/min to meet metabolic gas exchange.</li>
            <li><strong>Model Source:</strong> Calibrated on PhysioNet BIDMC & CapnoBase clinical benchmark datasets.</li>
          </ul>
          <div className="flex items-start space-x-1.5 p-2 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-300 text-[11px]">
            <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
            <span>
              <strong>Medical Disclaimer:</strong> Do not use respiratory rate alone to diagnose any medical condition. Never substitute predicted telemetry for clinical evaluation.
            </span>
          </div>
        </div>
      )}

      {/* Bottom Source & Separation Tag */}
      <div className="mt-3 pt-3 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-500">
        <span className="truncate max-w-[240px]">{source}</span>
        <span className="text-cyan-400/80 font-medium">Resting: 12–20 br/min</span>
      </div>
    </div>
  );
}
