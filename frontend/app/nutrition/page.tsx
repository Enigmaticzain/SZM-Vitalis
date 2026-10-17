'use client';

import React, { useState, useEffect } from 'react';
import { apiClient } from '@/lib/api';
import { Utensils, Zap, Plus } from 'lucide-react';

export default function NutritionPage() {
  const [logs, setLogs] = useState<any[]>([]);
  const [mealName, setMealName] = useState('');
  const [carbs, setCarbs] = useState(30);
  const [protein, setProtein] = useState(25);
  const [fat, setFat] = useState(15);

  const loadLogs = () => {
    apiClient.get('/api/nutrition').then((res) => setLogs(res.data)).catch(console.error);
  };

  useEffect(() => {
    loadLogs();
  }, []);

  const handleAdd = (e: React.FormEvent) => {
    e.preventDefault();
    if (!mealName) return;
    apiClient.post('/api/nutrition', {
      meal_name: mealName,
      carbs_g: carbs,
      protein_g: protein,
      fat_g: fat,
      calories: carbs * 4 + protein * 4 + fat * 9,
      predicted_glucose_spike: 90 + carbs * 0.8
    }).then(() => {
      setMealName('');
      loadLogs();
    }).catch(console.error);
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div>
        <h2 className="text-3xl font-extrabold text-white">Nutrition & Glucose Response Engine</h2>
        <p className="text-slate-400 text-sm mt-1">Track macronutrients and forecast postprandial glycemic excursions.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <form onSubmit={handleAdd} className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4 lg:col-span-1">
          <h3 className="text-lg font-bold text-white flex items-center space-x-2">
            <Utensils className="h-5 w-5 text-cyan-400" />
            <span>Log Meal</span>
          </h3>
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300">Meal Name</label>
            <input
              type="text"
              value={mealName}
              onChange={(e) => setMealName(e.target.value)}
              placeholder="e.g., Salmon & Quinoa Bowl"
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-cyan-500"
              required
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300">Carbohydrates (g): {carbs}</label>
            <input type="range" min="0" max="150" value={carbs} onChange={(e) => setCarbs(parseInt(e.target.value))} className="w-full accent-cyan-400" />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300">Protein (g): {protein}</label>
            <input type="range" min="0" max="100" value={protein} onChange={(e) => setProtein(parseInt(e.target.value))} className="w-full accent-cyan-400" />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-300">Fat (g): {fat}</label>
            <input type="range" min="0" max="80" value={fat} onChange={(e) => setFat(parseInt(e.target.value))} className="w-full accent-cyan-400" />
          </div>
          <button type="submit" className="w-full flex items-center justify-center space-x-2 bg-cyan-600 hover:bg-cyan-500 text-white font-semibold py-3 rounded-xl transition-colors shadow-lg shadow-cyan-900/40">
            <Plus className="h-4 w-4" />
            <span>Log & Predict Glucose Response</span>
          </button>
        </form>

        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl lg:col-span-2 space-y-4">
          <h3 className="text-lg font-bold text-white">Logged Meals & Glycemic Impact</h3>
          <div className="space-y-3">
            {logs.map((log, i) => (
              <div key={i} className="bg-slate-950 p-4 rounded-2xl border border-slate-800 flex items-center justify-between">
                <div>
                  <h4 className="font-semibold text-white">{log.meal_name}</h4>
                  <p className="text-xs text-slate-400 mt-1">Carbs: {log.carbs_g}g | Protein: {log.protein_g}g | Fat: {log.fat_g}g | {log.calories} kcal</p>
                </div>
                <div className="text-right">
                  <span className="text-xs text-slate-400 block">Predicted Spike</span>
                  <span className="text-lg font-bold text-amber-400">+{log.predicted_glucose_spike} mg/dL</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
