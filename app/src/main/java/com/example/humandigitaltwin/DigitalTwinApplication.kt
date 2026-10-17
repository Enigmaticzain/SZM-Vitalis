package com.example.humandigitaltwin

import android.app.Application
import com.example.humandigitaltwin.data.local.TwinDatabase
import com.example.humandigitaltwin.data.repository.DigitalTwinRepository

class DigitalTwinApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = TwinDatabase.getInstance(this)
        repository = DigitalTwinRepository(this)
    }

    companion object {
        lateinit var instance: DigitalTwinApplication
            private set
        lateinit var database: TwinDatabase
            private set
        lateinit var repository: DigitalTwinRepository
            private set
    }
}
