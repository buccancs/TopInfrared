package com.topdon.lib.core.navigation

import com.topdon.lib.core.config.RouterConfig

object RouteRegistry {
    
    fun registerAllRoutes() {
        registerAppRoutes()
    }
    
    private fun registerAppRoutes() {

    }
    
    fun registerModuleRoute(path: String, activityClass: Class<out android.app.Activity>) {
        ModernRouter.register(path, activityClass)
    }
}