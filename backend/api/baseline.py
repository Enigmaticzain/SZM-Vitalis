from fastapi import APIRouter
from seed_data import default_user
from models import UserProfile

router = APIRouter(prefix="/api/baseline", tags=["Baseline Engine"])

@router.get("/", response_model=UserProfile)
def get_baseline():
    return default_user

@router.put("/", response_model=UserProfile)
def update_baseline(profile: UserProfile):
    global default_user
    default_user = profile
    return default_user
