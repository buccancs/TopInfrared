package com.topinfrared.tc001.common.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * ModernRouter - Intent-based navigation system for Kotlin 2.0
 * 
 * A modern, type-safe replacement for ARouter that uses standard Android Intents
 * with a fluent API design for seamless migration.
 *
 * Features:
 * - Type-safe navigation 
 * - Fluent API compatible with ARouter syntax
 * - Built-in parameter passing and result handling
 * - No annotation processing overhead
 * - Full Kotlin 2.0 compatibility
 * 
 * Usage:
 * ```
 * ModernRouter.build("/app/main")
 *     .withString("param", value)
 *     .navigation(this)
 * ```
 */
object ModernRouter {
    
    /**
     * Build navigation request for the specified route path
     * @param path Route path (e.g., "/app/main", "/thermal/view")
     * @return NavigationBuilder instance for fluent API
     */
    fun build(path: String): NavigationBuilder {
        return NavigationBuilder(path)
    }
}

/**
 * Builder class for constructing navigation requests with parameters
 */
class NavigationBuilder(private val path: String) {
    
    private val parameters = mutableMapOf<String, Any?>()
    private var flags: Int? = null
    private var requestCode: Int? = null
    
    /**
     * Add string parameter to navigation request
     */
    fun withString(key: String, value: String?): NavigationBuilder {
        parameters[key] = value
        return this
    }
    
    /**
     * Add integer parameter to navigation request
     */
    fun withInt(key: String, value: Int): NavigationBuilder {
        parameters[key] = value
        return this
    }
    
    /**
     * Add boolean parameter to navigation request
     */
    fun withBoolean(key: String, value: Boolean): NavigationBuilder {
        parameters[key] = value
        return this
    }
    
    /**
     * Add long parameter to navigation request  
     */
    fun withLong(key: String, value: Long): NavigationBuilder {
        parameters[key] = value
        return this
    }
    
    /**
     * Add float parameter to navigation request
     */
    fun withFloat(key: String, value: Float): NavigationBuilder {
        parameters[key] = value
        return this
    }
    
    /**
     * Add double parameter to navigation request
     */
    fun withDouble(key: String, value: Double): NavigationBuilder {
        parameters[key] = value
        return this
    }
    
    /**
     * Add serializable parameter to navigation request
     */
    fun withSerializable(key: String, value: java.io.Serializable?): NavigationBuilder {
        parameters[key] = value
        return this
    }
    
    /**
     * Add parcelable parameter to navigation request
     */
    fun withParcelable(key: String, value: android.os.Parcelable?): NavigationBuilder {
        parameters[key] = value
        return this
    }
    
    /**
     * Add Bundle parameter to navigation request
     */
    fun withBundle(key: String, value: android.os.Bundle?): NavigationBuilder {
        parameters[key] = value
        return this
    }
    
    /**
     * Set Intent flags for navigation
     */
    fun withFlags(flags: Int): NavigationBuilder {
        this.flags = flags
        return this
    }
    
    /**
     * Set request code for startActivityForResult navigation
     */
    fun withRequestCode(requestCode: Int): NavigationBuilder {
        this.requestCode = requestCode
        return this
    }
    
    /**
     * Execute navigation from Activity context
     */
    fun navigation(activity: Activity?) {
        activity?.let { ctx ->
            val intent = createIntent(ctx)
            
            when {
                requestCode != null -> {
                    ctx.startActivityForResult(intent, requestCode!!)
                }
                else -> {
                    ctx.startActivity(intent)
                }
            }
        }
    }
    
    /**
     * Execute navigation from Fragment context
     */
    fun navigation(fragment: Fragment?) {
        fragment?.context?.let { ctx ->
            val intent = createIntent(ctx)
            
            when {
                requestCode != null -> {
                    fragment.startActivityForResult(intent, requestCode!!)
                }
                else -> {
                    fragment.startActivity(intent)
                }
            }
        }
    }
    
    /**
     * Execute navigation from Context
     */
    fun navigation(context: Context?) {
        context?.let { ctx ->
            val intent = createIntent(ctx)
            ctx.startActivity(intent)
        }
    }
    
    /**
     * Create Intent with all configured parameters
     */
    private fun createIntent(context: Context): Intent {
        // Get target activity class from route registry
        val activityClass = RouteRegistry.getActivityClass(path)
            ?: throw IllegalArgumentException("No activity registered for route: $path")
        
        val intent = Intent(context, activityClass)
        
        // Apply flags if specified
        flags?.let { intent.flags = it }
        
        // Add all parameters to intent
        parameters.forEach { (key, value) ->
            when (value) {
                null -> intent.putExtra(key, null as String?)
                is String -> intent.putExtra(key, value)
                is Int -> intent.putExtra(key, value)
                is Boolean -> intent.putExtra(key, value)
                is Long -> intent.putExtra(key, value)
                is Float -> intent.putExtra(key, value)
                is Double -> intent.putExtra(key, value)
                is java.io.Serializable -> intent.putExtra(key, value)
                is android.os.Parcelable -> intent.putExtra(key, value)
                is android.os.Bundle -> intent.putExtra(key, value)
                else -> throw IllegalArgumentException("Unsupported parameter type: ${value::class}")
            }
        }
        
        return intent
    }
}

/**
 * Route registry for mapping route paths to Activity classes
 */
object RouteRegistry {
    
    private val routes = mutableMapOf<String, Class<out Activity>>()
    
    /**
     * Register route mapping
     */
    fun register(path: String, activityClass: Class<out Activity>) {
        routes[path] = activityClass
    }
    
    /**
     * Get Activity class for route path
     */
    fun getActivityClass(path: String): Class<out Activity>? {
        return routes[path]
    }
    
    /**
     * Initialize default routes for TC001 Standalone
     */
    fun initializeStandaloneRoutes() {
        // Routes will be registered by activities in their companion objects
        // This allows for type-safe route definitions
    }
}