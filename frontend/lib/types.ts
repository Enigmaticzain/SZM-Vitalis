export interface WearableData {
  timestamp: string;
  heart_rate: number;
  hrv: number;
  blood_glucose: number;
  steps: number;
  sleep_hours: number;
  stress_level: number;
  energy_expenditure_kcal: number;
  respiratory_rate?: number;
  respiratory_rate_predicted?: number;
  respiratory_rate_confidence?: number;
  respiratory_rate_uncertainty?: number;
  respiratory_rate_status?: string;
  respiratory_rate_source?: string;
}

export interface RespiratoryHistoryPoint {
  timestamp: string;
  iso_timestamp: string;
  respiratory_rate: number;
  is_predicted: boolean;
  uncertainty_lower: number;
  uncertainty_upper: number;
  reference_min: number;
  reference_max: number;
  status: string;
  context: string;
}

export interface RespiratoryTelemetry {
  current: {
    timestamp: string;
    respiratory_rate: number;
    is_predicted: boolean;
    confidence: number;
    uncertainty: number;
    status: string;
    reference_range: string;
    context: string;
    model_version: string;
    provenance_source: string;
  };
  baseline_resting_rate: number;
  history_24h: RespiratoryHistoryPoint[];
  clinical_disclaimer: string;
}

export interface TwinState {
  user_id: string;
  health_score: number;
  biological_age: number;
  metabolic_flexibility: number;
  recovery_index: number;
  current_status: string;
  latest_metrics: WearableData;
  updated_at: string;
}

export interface ChatMessage {
  sender: string;
  message: string;
  timestamp: string;
}
