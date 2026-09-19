package com.ekoehler.expressivecutout.core

/** Bounded, in-memory pending alerts; replacing a key updates it without creating duplicates. */
internal class PendingNotifications<T>(private val capacity: Int = 32) {
    /** Values carry a monotonic expiry, or null for explicit-dismissal mode. */
    private val entries = linkedMapOf<String, Pair<T, Long?>>()

    /** Keeps the newest alerts while bounding memory during notification storms. */
    fun put(key: String, value: T, expiresAt: Long?) {
        entries.remove(key)
        entries[key] = value to expiresAt
        while (entries.size > capacity) entries.remove(entries.keys.first())
    }

    /** Forgets alerts acted on in either the island or the notification shade. */
    fun remove(key: String) { entries.remove(key) }

    /** Drops alerts from apps disabled while their notifications are still pending. */
    fun removeWhere(predicate: (T) -> Boolean) { entries.entries.removeAll { predicate(it.value.first) } }

    /** Drops expired entries before selecting the most recently received alert. */
    fun latest(now: Long): T? {
        entries.entries.removeAll { (_, entry) -> entry.second?.let { it <= now } == true }
        return entries.values.lastOrNull()?.first
    }

    /** Removes retained alerts when the user turns the feature off. */
    fun clear() { entries.clear() }
}
