import React from 'react';
import { LucideIcon } from 'lucide-react';

interface VitalCardProps {
  title: string;
  value: string | number;
  unit: string;
  icon: LucideIcon;
  statusColor?: string;
  description?: string;
}

export default function VitalCard({ title, value, unit, icon: Icon, statusColor = "text-cyan-400", description }: VitalCardProps) {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg hover:border-slate-700 transition-all">
      <div className="flex items-center justify-between mb-3">
        <span className="text-sm font-medium text-slate-400">{title}</span>
        <div className="p-2 bg-slate-800 rounded-xl">
          <Icon className={`h-5 w-5 ${statusColor}`} />
        </div>
      </div>
      <div className="flex items-baseline space-x-2">
        <span className="text-3xl font-bold text-white">{value}</span>
        <span className="text-sm text-slate-400">{unit}</span>
      </div>
      {description && <p className="mt-2 text-xs text-slate-500">{description}</p>}
    </div>
  );
}
