import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function fetchTwinState() {
  const res = await apiClient.get('/api/twin');
  return res.data;
}

export async function fetchWearableData() {
  const res = await apiClient.get('/api/wearable/current');
  return res.data;
}

export async function simulateWearable(spike: boolean = false) {
  const res = await apiClient.post(`/api/wearable/simulate?activity_spike=${spike}`);
  return res.data;
}

export async function runSimulation(scenario: string, intensity: number, duration: number) {
  const res = await apiClient.post('/api/simulation', {
    scenario_type: scenario,
    intensity,
    duration_hours: duration,
  });
  return res.data;
}

export async function fetchChatHistory() {
  const res = await apiClient.get('/api/chat');
  return res.data;
}

export async function sendChatMessage(message: string) {
  const res = await apiClient.post('/api/chat', { message });
  return res.data;
}

export async function fetchRespiratoryData() {
  const res = await apiClient.get('/api/respiratory');
  return res.data;
}

