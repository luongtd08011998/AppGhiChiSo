package com.example.appghichiso.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * In-memory singleton — lives as long as the process lives.
 *
 * Behaviour:
 *  - App cold-start (process killed / clear task) → [isActive] = false → LoginScreen
 *  - App warm-start (resumed from background)    → [isActive] = true  → skip login
 *  - Explicit logout                             → [deactivate] resets state
 *  - 401 mid-session                             → [emitUnauthorized] triggers navigation
 */
class SessionManager {

    var isActive: Boolean = false
        private set

    var email: String = ""
        private set

    var password: String = ""
        private set

    var billingMonth: Int = 0
        private set

    var billingYear: Int = 0
        private set

    /** Emits Unit whenever a 401 Unauthorized is received from any API call. */
    private val _unauthorizedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorizedEvent: SharedFlow<Unit> = _unauthorizedEvent

    /** Called after successful login. Sets credentials and marks session active. */
    fun activate(email: String, password: String, month: Int, year: Int) {
        this.email = email
        this.password = password
        this.billingMonth = month
        this.billingYear = year
        this.isActive = true
    }

    /** Called on explicit logout or when a 401 is received. Clears all in-memory state. */
    fun deactivate() {
        isActive = false
        email = ""
        password = ""
        billingMonth = 0
        billingYear = 0
    }

    /** Non-suspending emit — safe to call from Ktor response validator. */
    fun emitUnauthorized() {
        _unauthorizedEvent.tryEmit(Unit)
    }
}

