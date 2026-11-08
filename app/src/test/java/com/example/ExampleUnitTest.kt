package com.example

import com.example.model.AppLanguage
import com.example.model.DeviceSecurityLock
import com.example.model.LanguageDictionary
import com.example.model.P2PConnectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPairingRequiresIdenticalCredentials() {
        val unpaired = P2PConnectionState(
            localUsername = "alice",
            localPassword = "alpha_password_1",
            remoteUsername = "bob",
            remotePassword = "wrong_password",
            isLocalLoggedIn = true,
            isRemoteDeviceOnline = true
        )
        // Calling options remain greyed out (isPairingSuccessful is false)
        assertFalse(unpaired.isPairingSuccessful)

        val paired = P2PConnectionState(
            localUsername = "alice",
            localPassword = "alpha_password_1",
            remoteUsername = "alice",
            remotePassword = "alpha_password_1",
            isLocalLoggedIn = true,
            isRemoteDeviceOnline = true
        )
        // Calling options enabled, persistent notification shown
        assertTrue(paired.isPairingSuccessful)
    }

    @Test
    fun testFifteenDayLockCalculations() {
        val now = System.currentTimeMillis()
        val remaining = DeviceSecurityLock.calculateRemainingMillis(now)
        val expected = DeviceSecurityLock.LOCK_DURATION_MILLIS
        // Should be approximately 15 days in millis (1296000000 ms)
        assertTrue(remaining in (expected - 5000)..expected)

        val formatted = DeviceSecurityLock.formatRemaining(remaining)
        assertTrue(formatted.contains("14 days") || formatted.contains("15 days"))
    }

    @Test
    fun testIndianRegionalLanguagesCoverage() {
        val languages = AppLanguage.values()
        assertEquals(10, languages.size)
        languages.forEach { lang ->
            val strings = LanguageDictionary.getStrings(lang)
            assertTrue(strings.appTitle.isNotBlank())
            assertTrue(strings.pairNotSuccessfully.isNotBlank())
            assertTrue(strings.dropDetectionTitle.isNotBlank())
            assertTrue(strings.angryToneTitle.isNotBlank())
            assertTrue(strings.powerButtonTitle.isNotBlank())
        }
    }
}
