package com.topinfrared.tc001.standalone

import android.app.Application
import android.util.Log

class TC001Application : Application() {
    
    companion object {
        private const val TAG = "TC001Application"
        lateinit var instance: TC001Application
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "TC001 Standalone Application initialized")
        
        // Initialize any global components needed for TC001
        initializeTC001Components()
    }
    
    private fun initializeTC001Components() {
        // Initialize logging, crash reporting, or other global components
        Log.d(TAG, "TC001 components initialized")
    }
}