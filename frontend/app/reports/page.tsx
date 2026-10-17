'use client';

import React, { useState } from 'react';
import { apiClient } from '@/lib/api';
import { FileText, Upload, CheckCircle2, Award } from 'lucide-react';

export default function ReportsPage() {
  const [reportResult, setReportResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const handleUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setLoading(true);
    const formData = new FormData();
    formData.append('file', file);

    apiClient.post('/api/reports/parse', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
      .then((res) => {
        setReportResult(res.data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div>
        <h2 className="text-3xl font-extrabold text-white">Lab Report & Biomarker Parser</h2>
        <p className="text-slate-400 text-sm mt-1">Upload blood work or health reports for automated biomarker extraction and digital twin integration.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl lg:col-span-1 space-y-4">
          <h3 className="text-lg font-bold text-white flex items-center space-x-2">
            <FileText className="h-5 w-5 text-cyan-400" />
            <span>Upload Lab Report</span>
          </h3>
          <label className="border-2 border-dashed border-slate-700 hover:border-cyan-500 rounded-2xl p-8 flex flex-col items-center justify-center cursor-pointer transition-colors bg-slate-950">
            <Upload className="h-10 w-10 text-cyan-400 mb-3 animate-bounce" />
            <span className="text-sm font-semibold text-white">Click to upload PDF / Image</span>
            <span className="text-xs text-slate-500 mt-1">Blood panel, DNA test, metabolic report</span>
            <input type="file" onChange={handleUpload} className="hidden" accept=".pdf,.png,.jpg,.jpeg" />
          </label>
          {loading && <p className="text-center text-cyan-400 text-sm animate-pulse">Parsing lab biomarkers via AI engine...</p>}

          <div className="pt-2 border-t border-slate-800 space-y-2">
            <span className="text-xs font-semibold text-slate-400 block">Or Test With Sample Clinical Panels:</span>
            <div className="space-y-1.5">
              <button
                type="button"
                onClick={() => {
                  setLoading(true);
                  const sampleText = "Patient: Alex Mercer\nFasting Glucose: 91.0 mg/dL\nHbA1c: 5.2%\nTotal Cholesterol: 182.0 mg/dL\nLDL Cholesterol: 96.0 mg/dL\nHDL Cholesterol: 66.0 mg/dL\nTriglycerides: 90.0 mg/dL\nhs-CRP: 0.5 mg/L\nVitamin D: 54.0 ng/mL\nRespiratory Rate: 16.0 br/min\nSpO2: 99.0%";
                  const blob = new Blob([sampleText], { type: 'text/plain' });
                  const formData = new FormData();
                  formData.append('file', blob, 'Quest_Comprehensive_Metabolic_Panel.txt');
                  apiClient.post('/api/reports/parse', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
                    .then((res) => { setReportResult(res.data); setLoading(false); })
                    .catch(() => setLoading(false));
                }}
                className="w-full text-left px-3 py-2 bg-slate-950 hover:bg-slate-800/80 border border-slate-800 rounded-xl text-xs text-slate-300 transition-colors flex items-center justify-between"
              >
                <span>Comprehensive Metabolic & Lipid Panel</span>
                <span className="text-[10px] text-teal-400 font-mono">10 Biomarkers</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  setLoading(true);
                  const sampleText = "Patient: Marcus Vance\nRespiratory Rate: 28.0 br/min\nSpO2: 89.0%\nFasting Glucose: 215.0 mg/dL\nhs-CRP: 4.8 mg/L\nWBC Count: 13.5 k/uL\nHemoglobin: 11.8 g/dL";
                  const blob = new Blob([sampleText], { type: 'text/plain' });
                  const formData = new FormData();
                  formData.append('file', blob, 'Hospital_ICU_Arterial_Emergency_Panel.txt');
                  apiClient.post('/api/reports/parse', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
                    .then((res) => { setReportResult(res.data); setLoading(false); })
                    .catch(() => setLoading(false));
                }}
                className="w-full text-left px-3 py-2 bg-slate-950 hover:bg-slate-800/80 border border-slate-800 rounded-xl text-xs text-rose-300 transition-colors flex items-center justify-between"
              >
                <span>ICU Pulmonary & Acute Sepsis Panel</span>
                <span className="text-[10px] text-rose-400 font-mono">Elevated Flags</span>
              </button>
            </div>
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl lg:col-span-2 space-y-6">
          <h3 className="text-lg font-bold text-white">Extracted Biomarkers & Analysis</h3>
          {reportResult ? (
            <div className="space-y-6">
              <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800 flex items-center space-x-3">
                <CheckCircle2 className="h-6 w-6 text-emerald-400 flex-shrink-0" />
                <div>
                  <h4 className="font-semibold text-white">{reportResult.file_name}</h4>
                  <p className="text-xs text-slate-400">{reportResult.risk_assessment}</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {Object.entries(reportResult.biomarkers || {}).map(([k, v]: [string, any], i) => (
                  <div key={i} className="bg-slate-950 p-4 rounded-2xl border border-slate-800 flex justify-between items-center">
                    <span className="text-sm text-slate-400">{k}</span>
                    <span className="text-sm font-semibold text-cyan-400">{v}</span>
                  </div>
                ))}
              </div>

              <div>
                <h4 className="text-sm font-semibold text-slate-300 mb-3">Clinical Recommendations</h4>
                <ul className="space-y-2">
                  {reportResult.recommendations.map((rec: string, i: number) => (
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
              Upload a lab report PDF or scan to view extracted biomarkers and risk assessment.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
