from fastapi import FastAPI, Request, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import APIKeyHeader
from api import wearable, baseline, twin, prediction, pattern, correlation, simulation, nutrition, chat, reports, ws, hospital, doctor, remote, respiratory
from app.api import confidence, performance
import os

app = FastAPI(
    title="Personal Human Digital Twin API",
    description="Comprehensive backend for biometric telemetry, simulation, twin engines, and AI longevity coaching.",
    version="1.0.0"
)

API_KEY = os.getenv("API_KEY", "twin-secret-key-2026")
API_KEY_NAME = "X-API-Key"

@app.middleware("http")
async def api_key_middleware(request: Request, call_next):
    if request.url.path.startswith("/api/"):
        client_api_key = request.headers.get(API_KEY_NAME)
        if client_api_key and client_api_key != API_KEY:
            raise HTTPException(status_code=403, detail="Invalid API Key")
    response = await call_next(request)
    return response

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(wearable.router)
app.include_router(baseline.router)
app.include_router(twin.router)
app.include_router(prediction.router)
app.include_router(pattern.router)
app.include_router(correlation.router)
app.include_router(simulation.router)
app.include_router(nutrition.router)
app.include_router(chat.router)
app.include_router(reports.router)
app.include_router(ws.router)
app.include_router(hospital.router)
app.include_router(doctor.router)
app.include_router(remote.router)
app.include_router(confidence.router)
app.include_router(performance.router)
app.include_router(respiratory.router)

@app.get("/")
def read_root():
    return {"status": "online", "system": "Personal Human Digital Twin Engine"}
