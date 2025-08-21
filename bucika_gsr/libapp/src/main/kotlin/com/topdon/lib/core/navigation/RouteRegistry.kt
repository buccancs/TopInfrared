package com.topdon.lib.core.navigation

import com.topdon.lib.core.config.RouterConfig

/**
 * Route registration for Modern Router - replaces ARouter @Route annotations
 * This must be called during application initialization
 */
object RouteRegistry {
    
    fun registerAllRoutes() {
        // Register all routes from different modules
        registerAppRoutes()
        // Add other module route registrations here as needed
    }
    
    private fun registerAppRoutes() {
        // Note: These would need to be updated with actual activity classes
        // For now, just showing the pattern - actual activity classes would be imported from their modules
        
        /*
        // App module routes  
        ModernRouter.register(RouterConfig.MAIN, MainActivity::class.java)
        ModernRouter.register(RouterConfig.CLAUSE, ClauseActivity::class.java)
        ModernRouter.register(RouterConfig.POLICY, PolicyActivity::class.java)
        ModernRouter.register(RouterConfig.VERSION, VersionActivity::class.java)
        ModernRouter.register(RouterConfig.PDF, PdfActivity::class.java)
        ModernRouter.register(RouterConfig.IR_GALLERY_EDIT, IRGalleryEditActivity::class.java)
        
        // Add more routes as needed
        */
    }
    
    // Modules can register their own routes using this method
    fun registerModuleRoute(path: String, activityClass: Class<out android.app.Activity>) {
        ModernRouter.register(path, activityClass)
    }
}