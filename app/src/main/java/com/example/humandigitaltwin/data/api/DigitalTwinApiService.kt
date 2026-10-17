package com.example.humandigitaltwin.data.api

import com.example.humandigitaltwin.data.models.*
import retrofit2.http.*

interface DigitalTwinApiService {

    @GET("/api/twin")
    suspend fun getTwinState(): TwinState

    @GET("/api/wearable/current")
    suspend fun getCurrentWearable(): WearableDataPoint

    @POST("/api/wearable/simulate")
    suspend fun simulateWearable(@Query("activity_spike") spike: Boolean): WearableDataPoint

    @POST("/api/simulation")
    suspend fun runSimulation(@Body request: SimulationRequest): SimulationResult

    @GET("/api/nutrition")
    suspend fun getNutritionLogs(): List<NutritionLog>

    @POST("/api/nutrition")
    suspend fun addNutritionLog(@Body log: NutritionLog): NutritionLog

    @GET("/api/chat")
    suspend fun getChatHistory(): List<ChatMessage>

    @POST("/api/chat")
    suspend fun sendChatMessage(@Body request: ChatRequest): ChatMessage

    @Multipart
    @POST("/api/reports/parse")
    suspend fun parseReport(@Part file: okhttp3.MultipartBody.Part): ReportAnalysisResult

    @GET("/api/hospital/status")
    suspend fun getHospitalStatus(): HospitalStatusResponse

    @POST("/api/hospital/escalate")
    suspend fun forceHospitalEscalation(@Body update: Map<String, Int>): HospitalStatusResponse

    @POST("/api/hospital/resolve")
    suspend fun resolveHospitalAlerts(): HospitalStatusResponse

    @GET("/api/doctor/patients")
    suspend fun getDoctorPatients(): List<DoctorPatient>

    @GET("/api/doctor/alerts")
    suspend fun getDoctorAlerts(): List<DoctorAlert>

    @POST("/api/doctor/review")
    suspend fun submitDoctorReview(@Body review: DoctorReviewRequest): Map<String, Any>

    @GET("/api/remote/care-plan")
    suspend fun getRemoteCarePlan(): CarePlan

    @POST("/api/remote/checkin")
    suspend fun submitRemoteCheckIn(@Body checkIn: CheckInRequest): Map<String, Any>

    @GET("/api/remote/checkins")
    suspend fun getRemoteCheckIns(): List<CheckInEntry>

    @GET("/api/confidence")
    suspend fun getConfidenceBreakdown(): ConfidenceResponse

    @GET("/api/performance")
    suspend fun getModelPerformance(): ModelPerformanceResponse
}
