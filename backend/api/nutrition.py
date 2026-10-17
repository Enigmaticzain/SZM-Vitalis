from fastapi import APIRouter
from models import NutritionLog
from typing import List

router = APIRouter(prefix="/api/nutrition", tags=["Nutrition Engine"])

logs_db: List[NutritionLog] = [
    NutritionLog(meal_name="Avocado Toast with Eggs", carbs_g=32.0, protein_g=22.0, fat_g=18.0, calories=380.0, predicted_glucose_spike=112.0),
    NutritionLog(meal_name="Grilled Chicken Salad", carbs_g=12.0, protein_g=45.0, fat_g=14.0, calories=420.0, predicted_glucose_spike=98.0)
]

@router.get("/", response_model=List[NutritionLog])
def get_nutrition_logs():
    return logs_db

@router.post("/", response_model=NutritionLog)
def add_nutrition_log(log: NutritionLog):
    logs_db.append(log)
    return log
