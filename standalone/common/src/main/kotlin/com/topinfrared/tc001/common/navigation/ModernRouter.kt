package com.topinfrared.tc001.common.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

object ModernRouter {

    fun build(path: String): NavigationBuilder {
        return NavigationBuilder(path)
    }
}

class NavigationBuilder(private val path: String) {
    
    private val parameters = mutableMapOf<String, Any?>()
    private var flags: Int? = null
    private var requestCode: Int? = null

    fun withString(key: String, value: String?): NavigationBuilder {
        parameters[key] = value
        return this
    }

    fun withInt(key: String, value: Int): NavigationBuilder {
        parameters[key] = value
        return this
    }

    fun withBoolean(key: String, value: Boolean): NavigationBuilder {
        parameters[key] = value
        return this
    }

    fun withLong(key: String, value: Long): NavigationBuilder {
        parameters[key] = value
        return this
    }

    fun withFloat(key: String, value: Float): NavigationBuilder {
        parameters[key] = value
        return this
    }

    fun withDouble(key: String, value: Double): NavigationBuilder {
        parameters[key] = value
        return this
    }

    fun withSerializable(key: String, value: java.io.Serializable?): NavigationBuilder {
        parameters[key] = value
        return this
    }

    fun withParcelable(key: String, value: android.os.Parcelable?): NavigationBuilder {
        parameters[key] = value
        return this
    }

    fun withBundle(key: String, value: android.os.Bundle?): NavigationBuilder {
        parameters[key] = value
        return this
    }

    fun withFlags(flags: Int): NavigationBuilder {
        this.flags = flags
        return this
    }

    fun withRequestCode(requestCode: Int): NavigationBuilder {
        this.requestCode = requestCode
        return this
    }

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

    fun navigation(context: Context?) {
        context?.let { ctx ->
            val intent = createIntent(ctx)
            ctx.startActivity(intent)
        }
    }

    private fun createIntent(context: Context): Intent {
        val activityClass = RouteRegistry.getActivityClass(path)
            ?: throw IllegalArgumentException("No activity registered for route: $path")
        
        val intent = Intent(context, activityClass)
        
        flags?.let { intent.flags = it }
        
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

object RouteRegistry {
    
    private val routes = mutableMapOf<String, Class<out Activity>>()

    fun register(path: String, activityClass: Class<out Activity>) {
        routes[path] = activityClass
    }

    fun getActivityClass(path: String): Class<out Activity>? {
        return routes[path]
    }

    fun initializeStandaloneRoutes() {
    }
}