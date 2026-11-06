package za.co.safety4women

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UiTextTest {
    @Test fun englishCopyRemainsUnchanged() {
        assertEquals("Settings", translateUi("Settings", "en"))
    }

    @Test fun settingsAndEmergencyCopyAreAvailableInIsiZulu() {
        assertEquals("Izilungiselelo", translateUi("Settings", "zu"))
        assertNotEquals("Call emergency services", translateUi("Call emergency services", "zu"))
        assertNotEquals("Your password is never saved on this device. Firebase keeps your signed-in session until you sign out.", translateUi("Your password is never saved on this device. Firebase keeps your signed-in session until you sign out.", "zu"))
    }
}
