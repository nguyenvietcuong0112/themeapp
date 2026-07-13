package com.app.personalization.data

import java.lang.reflect.Method

annotation class Subscribe

class EventBus {
    private val subscribers = mutableMapOf<Any, List<Method>>()

    companion object {
        private val defaultInstance = EventBus()
        fun getDefault() = defaultInstance
    }

    fun register(subscriber: Any) {
        val methods = subscriber.javaClass.methods.filter {
            it.isAnnotationPresent(Subscribe::class.java) && it.parameterTypes.size == 1
        }
        subscribers[subscriber] = methods
    }

    fun unregister(subscriber: Any) {
        subscribers.remove(subscriber)
    }

    fun post(event: Any) {
        // Run on main thread to update UI safely
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        for ((subscriber, methods) in subscribers) {
            for (method in methods) {
                if (method.parameterTypes[0].isAssignableFrom(event.javaClass)) {
                    mainHandler.post {
                        try {
                            method.invoke(subscriber, event)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}

class CoinUpdatedEvent
class ShortcutEvent
