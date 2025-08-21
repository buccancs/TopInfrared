package com.topdon.lib.core.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Modern routing solution replacing ARouter for Kotlin 2.0 compatibility
 */
object ModernRouter {

    private val routes = mutableMapOf<String, Class<out Activity>>()
    
    /**
     * Register a route with its destination activity
     */
    fun register(path: String, activityClass: Class<out Activity>) {
        routes[path] = activityClass
    }
    
    /**
     * Create a navigation builder for a route
     */
    fun build(path: String): NavigationBuilder {
        return NavigationBuilder(path, routes[path])
    }
    
    /**
     * Navigation builder for fluent API
     */
    class NavigationBuilder(
        private val path: String,
        private val activityClass: Class<out Activity>?
    ) {
        private val extras = mutableMapOf<String, Any>()
        
        fun withString(key: String, value: String): NavigationBuilder {
            extras[key] = value
            return this
        }
        
        fun withInt(key: String, value: Int): NavigationBuilder {
            extras[key] = value
            return this
        }
        
        fun withBoolean(key: String, value: Boolean): NavigationBuilder {
            extras[key] = value
            return this
        }
        
        fun withSerializable(key: String, value: java.io.Serializable): NavigationBuilder {
            extras[key] = value
            return this
        }
        
        /**
         * Navigate from activity
         */
        fun navigation(activity: Activity) {
            activityClass?.let { clazz ->
                val intent = Intent(activity, clazz).apply {
                    extras.forEach { (key, value) ->
                        when (value) {
                            is String -> putExtra(key, value)
                            is Int -> putExtra(key, value)
                            is Boolean -> putExtra(key, value)
                            is java.io.Serializable -> putExtra(key, value)
                        }
                    }
                }
                activity.startActivity(intent)
            }
        }
        
        /**
         * Navigate from fragment
         */
        fun navigation(fragment: Fragment) {
            fragment.activity?.let { activity ->
                navigation(activity)
            }
        }
        
        /**
         * Navigate from context
         */
        fun navigation(context: Context) {
            activityClass?.let { clazz ->
                val intent = Intent(context, clazz).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    extras.forEach { (key, value) ->
                        when (value) {
                            is String -> putExtra(key, value)
                            is Int -> putExtra(key, value) 
                            is Boolean -> putExtra(key, value)
                            is java.io.Serializable -> putExtra(key, value)
                        }
                    }
                }
                context.startActivity(intent)
            }
        }
    }
}