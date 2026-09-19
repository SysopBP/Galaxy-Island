package com.ekoehler.expressivecutout.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Exercises bursts, replacements and expiry independently of Android's services. */
class PendingNotificationsTest {
    /** A newer alert must not destroy an older alert still waiting for acknowledgement. */
    @Test fun dismissalRestoresPreviousAlert() {
        val pending = PendingNotifications<String>()
        pending.put("one", "First", null)
        pending.put("two", "Second", null)
        pending.remove("two")
        assertEquals("First", pending.latest(Long.MAX_VALUE))
        pending.remove("one")
        assertNull(pending.latest(0))
    }

    /** Updating one Android key cannot create a duplicate that returns after dismissal. */
    @Test fun updateReplacesSameKey() {
        val pending = PendingNotifications<String>()
        pending.put("one", "Old", null)
        pending.put("one", "New", null)
        assertEquals("New", pending.latest(0))
        pending.remove("one")
        assertNull(pending.latest(0))
    }

    /** Muting an app removes its retained alert without deleting other apps' alerts. */
    @Test fun mutedAppCannotResurface() {
        val pending = PendingNotifications<String>()
        pending.put("one", "allowed", null)
        pending.put("two", "muted", null)
        pending.removeWhere { it == "muted" }
        assertEquals("allowed", pending.latest(0))
    }

    /** Expired and overflowed alerts never resurface after an interrupting event. */
    @Test fun expiryAndCapacityAreEnforced() {
        val pending = PendingNotifications<String>(2)
        pending.put("one", "Dropped", null)
        pending.put("two", "Retained", null)
        pending.put("three", "Temporary", 100)
        assertEquals("Temporary", pending.latest(99))
        assertEquals("Retained", pending.latest(100))
        pending.remove("two")
        assertNull(pending.latest(100))
    }
}
