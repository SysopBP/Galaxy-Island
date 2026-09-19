package com.ekoehler.expressivecutout.core

/** Treats unknown, malformed, and outdated bridge responses as locked. */
object D2StatePolicy {
    /** Only the current typed protocol can report an unlocked D2 state. */
    fun isLocked(protocol: Any?, locked: Any?, ready: Any?): Boolean =
        protocol != 1 || locked !is Boolean || ready !is Boolean || locked
}
