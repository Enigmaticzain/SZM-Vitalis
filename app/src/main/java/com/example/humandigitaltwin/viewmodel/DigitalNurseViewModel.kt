package com.example.humandigitaltwin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.humandigitaltwin.DigitalTwinApplication
import com.example.humandigitaltwin.data.models.*
import com.example.humandigitaltwin.data.repository.DigitalTwinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DigitalNurseViewModel : ViewModel() {
    private val repository: DigitalTwinRepository = try {
        DigitalTwinApplication.repository
    } catch (e: Exception) {
        DigitalTwinRepository()
    }

    // Hospital State
    private val _hospitalStatus = MutableStateFlow<HospitalStatusResponse?>(null)
    val hospitalStatus: StateFlow<HospitalStatusResponse?> = _hospitalStatus.asStateFlow()

    // Doctor State
    private val _doctorPatients = MutableStateFlow<List<DoctorPatient>>(emptyList())
    val doctorPatients: StateFlow<List<DoctorPatient>> = _doctorPatients.asStateFlow()

    private val _doctorAlerts = MutableStateFlow<List<DoctorAlert>>(emptyList())
    val doctorAlerts: StateFlow<List<DoctorAlert>> = _doctorAlerts.asStateFlow()

    // Remote Care State
    private val _carePlan = MutableStateFlow<CarePlan?>(null)
    val carePlan: StateFlow<CarePlan?> = _carePlan.asStateFlow()

    private val _checkIns = MutableStateFlow<List<CheckInEntry>>(emptyList())
    val checkIns: StateFlow<List<CheckInEntry>> = _checkIns.asStateFlow()

    init {
        refreshAllModules()
    }

    fun refreshAllModules() {
        viewModelScope.launch {
            try {
                _hospitalStatus.value = repository.getHospitalStatus()
                _doctorPatients.value = repository.getDoctorPatients()
                _doctorAlerts.value = repository.getDoctorAlerts()
                _carePlan.value = repository.getRemoteCarePlan()
                _checkIns.value = repository.getRemoteCheckIns()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun forceEscalation(level: Int) {
        viewModelScope.launch {
            try {
                _hospitalStatus.value = repository.forceHospitalEscalation(level)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resolveAlerts() {
        viewModelScope.launch {
            try {
                _hospitalStatus.value = repository.resolveHospitalAlerts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitDoctorReview(patientId: String, notes: String, actionItem: String) {
        viewModelScope.launch {
            try {
                repository.submitDoctorReview(patientId, notes, actionItem)
                _doctorPatients.value = repository.getDoctorPatients()
                _doctorAlerts.value = repository.getDoctorAlerts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitCheckIn(symptomScore: Int, painLevel: Int, temp: Double, notes: String) {
        viewModelScope.launch {
            try {
                repository.submitRemoteCheckIn(symptomScore, painLevel, temp, notes)
                _checkIns.value = repository.getRemoteCheckIns()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
