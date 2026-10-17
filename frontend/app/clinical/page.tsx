'use client';

import React, { useEffect, useState } from 'react';
import { apiClient } from '@/lib/api';
import { AlertTriangle, CheckCircle2, UserCheck, Stethoscope, Activity, Wind, Heart, Send } from 'lucide-react';

interface Patient {
  patient_id: string;
  name: string;
  age: number;
  risk_status: string;
  health_score: number;
  recovery_index: number;
  latest_vitals: {
    hr: number;
    glucose: number;
    bp: string;
    rr?: number;
    spo2?: number;
  };
  trajectory: number[];
  clinical_notes: string;
}

interface DoctorAlert {
  alert_id: string;
  patient_id: string;
  patient_name: string;
  severity: string;
  message: string;
  timestamp: string;
}

export default function ClinicalPortalPage() {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [alerts, setAlerts] = useState<DoctorAlert[]>([]);
  const [hospitalStatus, setHospitalStatus] = useState<any>(null);
  const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null);
  const [reviewNotes, setReviewNotes] = useState('');
  const [actionItem, setActionItem] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  const loadData = () => {
    apiClient.get('/api/doctor/patients').then((r) => setPatients(r.data)).catch(() => {});
    apiClient.get('/api/doctor/alerts').then((r) => setAlerts(r.data)).catch(() => {});
    apiClient.get('/api/hospital/status').then((r) => setHospitalStatus(r.data)).catch(() => {});
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 10000);
    return () => clearInterval(interval);
  }, []);

  const handleSubmitReview = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPatient || !reviewNotes) return;
    setSubmitting(true);
    apiClient.post('/api/doctor/review', {
      patient_id: selectedPatient.patient_id,
      notes: reviewNotes,
      action_item: actionItem || 'Continue clinical telemetry monitoring'
    }).then(() => {
      setSubmitting(false);
      setSubmitSuccess(true);
      setReviewNotes('');
      setActionItem('');
      loadData();
      setTimeout(() => setSubmitSuccess(false), 4000);
    }).catch(() => setSubmitting(false));
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-extrabold text-white flex items-center space-x-3">
            <Stethoscope className="h-8 w-8 text-cyan-400" />
            <span>Clinical Telemetry & Provider Portal</span>
          </h2>
          <p className="text-slate-400 text-sm mt-1">
            NEWS2 National Early Warning Surveillance, multi-patient vitals queue, and acute respiratory distress triage.
          </p>
        </div>

        {hospitalStatus && (
          <div className="bg-slate-900 border border-slate-800 p-3 rounded-2xl flex items-center space-x-3 self-start md:self-auto">
            <span className={`h-3 w-3 rounded-full ${hospitalStatus.escalation_level >= 3 ? 'bg-rose-500 animate-ping' : 'bg-emerald-400'}`} />
            <div>
              <div className="text-xs font-bold text-white">{hospitalStatus.level_name}</div>
              <div className="text-[11px] text-slate-400">NEWS2 Score: {hospitalStatus.news2_score ?? 0} • Unit 4B</div>
            </div>
          </div>
        )}
      </div>

      {/* Clinical Alerts Strip */}
      {alerts.length > 0 && (
        <div className="space-y-2">
          <h3 className="text-xs font-bold text-slate-400 tracking-wider uppercase">Active Clinical Escalations</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {alerts.map((alt) => (
              <div
                key={alt.alert_id}
                className={`p-4 rounded-2xl border flex items-start space-x-3 ${
                  alt.severity === 'CRITICAL'
                    ? 'bg-rose-950/20 border-rose-500/40 text-rose-300'
                    : 'bg-amber-950/20 border-amber-500/40 text-amber-300'
                }`}
              >
                <AlertTriangle className="h-5 w-5 flex-shrink-0 mt-0.5" />
                <div className="text-xs space-y-1">
                  <div className="flex items-center space-x-2">
                    <span className="font-bold">{alt.patient_name}</span>
                    <span className="px-1.5 py-0.5 rounded text-[10px] font-mono bg-slate-900 border border-slate-700">
                      {alt.severity}
                    </span>
                  </div>
                  <p>{alt.message}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Patient Telemetry Roster */}
      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
        <h3 className="text-lg font-bold text-white flex items-center space-x-2">
          <Activity className="h-5 w-5 text-cyan-400" />
          <span>Patient Telemetry Roster & NEWS2 Stratification</span>
        </h3>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="text-xs uppercase bg-slate-950 text-slate-400 border-b border-slate-800">
              <tr>
                <th className="p-3.5">Patient</th>
                <th className="p-3.5">Age</th>
                <th className="p-3.5">Risk Status</th>
                <th className="p-3.5">Respiratory Rate</th>
                <th className="p-3.5">Heart Rate</th>
                <th className="p-3.5">SpO2</th>
                <th className="p-3.5">Blood Pressure</th>
                <th className="p-3.5">Glucose</th>
                <th className="p-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-medium">
              {patients.map((p) => {
                const rr = p.latest_vitals.rr ?? 16.0;
                const rrCritical = rr < 9 || rr >= 25;
                const rrWarning = rr >= 21 && rr <= 24;

                return (
                  <tr key={p.patient_id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="p-3.5">
                      <div className="font-bold text-white">{p.name}</div>
                      <div className="text-xs text-slate-500 font-mono">{p.patient_id}</div>
                    </td>
                    <td className="p-3.5 text-slate-400">{p.age}y</td>
                    <td className="p-3.5">
                      <span
                        className={`px-2.5 py-1 text-xs rounded-full border ${
                          p.risk_status === 'Critical'
                            ? 'bg-rose-500/10 border-rose-500/30 text-rose-400'
                            : p.risk_status === 'Warning'
                            ? 'bg-amber-500/10 border-amber-500/30 text-amber-400'
                            : 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                        }`}
                      >
                        {p.risk_status}
                      </span>
                    </td>
                    <td className="p-3.5">
                      <div className={`font-mono font-bold flex items-center space-x-1 ${rrCritical ? 'text-rose-400' : rrWarning ? 'text-amber-400' : 'text-teal-400'}`}>
                        <Wind className="h-3.5 w-3.5" />
                        <span>{rr} br/min</span>
                      </div>
                      <div className="text-[10px] text-slate-500">
                        {rrCritical ? 'NEWS2: 3 (Critical)' : rrWarning ? 'NEWS2: 2' : 'Resting Normal'}
                      </div>
                    </td>
                    <td className="p-3.5">
                      <div className="font-mono text-white flex items-center space-x-1">
                        <Heart className="h-3.5 w-3.5 text-rose-400" />
                        <span>{p.latest_vitals.hr} bpm</span>
                      </div>
                    </td>
                    <td className="p-3.5 font-mono text-slate-300">
                      {p.latest_vitals.spo2 ?? 98}%
                    </td>
                    <td className="p-3.5 font-mono text-slate-400">{p.latest_vitals.bp}</td>
                    <td className="p-3.5 font-mono text-amber-400">{p.latest_vitals.glucose} mg/dL</td>
                    <td className="p-3.5 text-right">
                      <button
                        onClick={() => setSelectedPatient(p)}
                        className="px-3 py-1.5 bg-cyan-600/20 hover:bg-cyan-600/30 text-cyan-400 border border-cyan-500/30 rounded-xl text-xs font-semibold transition-colors"
                      >
                        Review
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Physician Review & Action Modal / Drawer */}
      {selectedPatient && (
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-6">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-bold text-white flex items-center space-x-2">
              <UserCheck className="h-5 w-5 text-cyan-400" />
              <span>Attending Physician Review — {selectedPatient.name} ({selectedPatient.patient_id})</span>
            </h3>
            <button
              onClick={() => setSelectedPatient(null)}
              className="text-xs text-slate-400 hover:text-white"
            >
              Close
            </button>
          </div>

          <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800 text-xs text-slate-400 space-y-1">
            <span className="font-semibold text-slate-300 block">Existing Clinical Trajectory:</span>
            <p>{selectedPatient.clinical_notes}</p>
          </div>

          <form onSubmit={handleSubmitReview} className="space-y-4">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-slate-300">Clinical Evaluation / Assessment Notes</label>
              <textarea
                rows={3}
                required
                value={reviewNotes}
                onChange={(e) => setReviewNotes(e.target.value)}
                placeholder="Enter clinical assessment, respiratory evaluation, and medication titration notes..."
                className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-white text-sm focus:outline-none focus:border-cyan-500"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-slate-300">Action Item / Order</label>
              <input
                type="text"
                value={actionItem}
                onChange={(e) => setActionItem(e.target.value)}
                placeholder="e.g., Continuous pulse oximetry, order arterial blood gas (ABG), bedside spirometry"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-white text-sm focus:outline-none focus:border-cyan-500"
              />
            </div>

            <div className="flex items-center justify-between">
              {submitSuccess && (
                <span className="text-xs text-emerald-400 flex items-center space-x-1">
                  <CheckCircle2 className="h-4 w-4 inline" />
                  <span>Physician review logged into patient electronic digital twin record.</span>
                </span>
              )}
              <button
                type="submit"
                disabled={submitting}
                className="ml-auto flex items-center space-x-2 bg-cyan-600 hover:bg-cyan-500 text-white font-semibold px-5 py-2.5 rounded-xl transition-colors shadow-lg shadow-cyan-900/40 text-xs"
              >
                <Send className="h-3.5 w-3.5" />
                <span>{submitting ? 'Recording...' : 'Submit Physician Review'}</span>
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
