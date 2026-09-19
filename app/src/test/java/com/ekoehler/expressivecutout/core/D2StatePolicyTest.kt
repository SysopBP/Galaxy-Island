package com.ekoehler.expressivecutout.core

import org.junit.Assert.*
import org.junit.Test

/** Missing IPC data must never be interpreted as a PIN unlock. */
class D2StatePolicyTest {
    @Test fun missingProviderStaysPrivate() { assertTrue(D2StatePolicy.isLocked(null, null, null)) }
    @Test fun unknownProtocolStaysPrivate() { assertTrue(D2StatePolicy.isLocked(2, false, true)) }
    @Test fun missingLockedFieldStaysPrivate() { assertTrue(D2StatePolicy.isLocked(1, null, true)) }
    @Test fun stringFalseIsNotAnUnlock() { assertTrue(D2StatePolicy.isLocked(1, "false", true)) }
    @Test fun lockedResponseStaysPrivate() { assertTrue(D2StatePolicy.isLocked(1, true, true)) }
    @Test fun authenticatedUnlockRestoresIsland() { assertFalse(D2StatePolicy.isLocked(1, false, true)) }
    @Test fun disabledCompanionCanReportUnlocked() { assertFalse(D2StatePolicy.isLocked(1, false, false)) }
}
