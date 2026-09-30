package za.co.safety4women
import org.junit.Assert.assertEquals
import org.junit.Test
class SafetyViewModelTest {
 @Test fun selectingPrimaryUserOpensAccountCreation() { val vm = SafetyViewModel(); vm.select(Role.PRIMARY); assertEquals("auth", vm.state.value.screen) }
 @Test fun journeyRequiresAnAuthenticatedSession() { val vm = SafetyViewModel(); vm.startJourney("Rosebank Mall", 25, "Blue Toyota"); assertEquals(null, vm.state.value.journey); assertEquals("Sign in before saving a journey.", vm.state.value.notice) }
 @Test fun trustedContactsRequireASignedInAccount() { val vm = SafetyViewModel(); vm.addContact("A Friend", "+27123456789"); assertEquals(0, vm.state.value.contacts.size); assertEquals("Sign in to save trusted contacts.", vm.state.value.notice) }
}
