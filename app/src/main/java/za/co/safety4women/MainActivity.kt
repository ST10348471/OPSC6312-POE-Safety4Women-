package za.co.safety4women

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); window.statusBarColor = android.graphics.Color.rgb(242, 250, 248); window.navigationBarColor = android.graphics.Color.rgb(242, 250, 248); window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR; setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF087E78), onPrimary = Color.White, secondary = Color(0xFF386B66), background = Color(0xFFF2FAF8), surface = Color.White, error = Color(0xFFB42318))) { SafetyApp() } } }

}

enum class Role { PRIMARY, COMPANION }
enum class SafetyLevel { CHECK_IN, ALERT, SOS }
data class Journey(val destination: String, val etaMinutes: Int, val transport: String)
data class Contact(val id: String, val name: String, val phone: String)
data class UiState(val role: Role? = null, val language: String = "en", val screen: String = "onboarding", val journey: Journey? = null, val contacts: List<Contact> = emptyList(), val notice: String? = null, val events: List<String> = emptyList(), val accountName: String = "", val firebaseUid: String? = null, val busy: Boolean = false, val highContrast: Boolean = false, val invitationToken: String? = null, val linkedCompanions: List<CompanionProfile> = emptyList(), val sharedJourneys: List<SharedJourney> = emptyList(), val latestIncidentId: String? = null)

class SafetyViewModel : ViewModel() {
    private val cloud by lazy { FirebaseCloudRepository() }
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    fun restoreSession() {
        if (_state.value.firebaseUid != null) return
        viewModelScope.launch {
            runCatching { cloud.restoreSession() }.onSuccess { account ->
                if (account != null) {
                    _state.value = _state.value.copy(screen = "home", accountName = account.displayName, firebaseUid = account.uid, role = account.role)
                    registerPushToken()
                    loadAccountData(account.role)
                }
            }
        }
    }

    fun select(role: Role) { _state.value = _state.value.copy(role = role, screen = "auth") }
    fun setNotice(message: String) { _state.value = _state.value.copy(notice = message) }

    fun authenticate(name: String, email: String, password: String, register: Boolean) {
        if (_state.value.busy) return
        _state.value = _state.value.copy(busy = true, notice = null)
        viewModelScope.launch {
            try {
                val languageChoice = _state.value.language
                val account = if (register) cloud.register(name, email, password, _state.value.role ?: Role.PRIMARY) else cloud.signIn(email, password)
                _state.value = _state.value.copy(screen = "home", accountName = account.displayName, firebaseUid = account.uid, role = account.role, busy = false, notice = "Signed in with Firebase. Firestore keeps your account data synced.")
                registerPushToken()
                if (register) cloud.saveSettings(language = languageChoice)
                loadAccountData(account.role)
            } catch (error: Exception) {
                _state.value = _state.value.copy(busy = false, notice = firebaseMessage(error))
            }
        }
    }

    fun authenticateGoogle(idToken: String) {
        if (_state.value.busy) return
        _state.value = _state.value.copy(busy = true, notice = null)
        viewModelScope.launch {
            try {
                val account = cloud.signInWithGoogle(idToken, _state.value.role ?: Role.PRIMARY, _state.value.language)
                _state.value = _state.value.copy(screen = "home", accountName = account.displayName, firebaseUid = account.uid, role = account.role, busy = false, notice = "Signed in with Google. Firebase keeps your account data synced.")
                registerPushToken()
                loadAccountData(account.role)
            } catch (error: Exception) {
                _state.value = _state.value.copy(busy = false, notice = firebaseMessage(error))
            }
        }
    }

    private fun registerPushToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { cloud.saveDeviceToken(it) }
            .addOnFailureListener { android.util.Log.w("Safety4Women", "FCM registration failed (${it.javaClass.simpleName})") }
    }

    private suspend fun loadAccountData(role: Role) {
        runCatching { cloud.settings() }.onSuccess { _state.value = _state.value.copy(language = it.language, highContrast = it.highContrast) }
        runCatching { cloud.getContacts() }.onSuccess { _state.value = _state.value.copy(contacts = it) }
        runCatching { cloud.linkedCompanions() }.onSuccess { _state.value = _state.value.copy(linkedCompanions = it) }
        if (role == Role.PRIMARY) runCatching { cloud.ownJourney() }.onSuccess { _state.value = _state.value.copy(journey = it) }
        if (role == Role.COMPANION) runCatching { cloud.sharedJourneys() }.onSuccess { _state.value = _state.value.copy(sharedJourneys = it) }
    }

    fun language(code: String) {
        _state.value = _state.value.copy(language = code)
        if (_state.value.firebaseUid == null) return
        viewModelScope.launch { runCatching { cloud.saveSettings(language = code) }.onFailure { _state.value = _state.value.copy(notice = firebaseMessage(it)) } }
    }

    fun toggleContrast(enabled: Boolean) {
        _state.value = _state.value.copy(highContrast = enabled)
        if (_state.value.firebaseUid == null) return
        viewModelScope.launch { runCatching { cloud.saveSettings(highContrast = enabled) }.onFailure { _state.value = _state.value.copy(notice = firebaseMessage(it)) } }
    }

    fun createCompanionInvitation(email: String, allowJourneys: Boolean, allowCheckIns: Boolean) {
        if (_state.value.firebaseUid == null) { _state.value = _state.value.copy(notice = "Sign in to invite a companion."); return }
        _state.value = _state.value.copy(busy = true, notice = null, invitationToken = null)
        viewModelScope.launch {
            runCatching { cloud.createInvitation(email.trim(), allowJourneys, allowCheckIns) }
                .onSuccess { invite -> _state.value = _state.value.copy(busy = false, invitationToken = invite.invitationToken, notice = "Invitation created. Share this one-time code with ${email.trim()} privately. It expires in ${invite.expiresInHours} hours; the app sent no message.") }
                .onFailure { _state.value = _state.value.copy(busy = false, notice = firebaseMessage(it)) }
        }
    }

    fun acceptCompanionInvitation(invitation: String) {
        if (_state.value.firebaseUid == null) { _state.value = _state.value.copy(notice = "Sign in with a companion account to accept an invitation."); return }
        _state.value = _state.value.copy(busy = true, notice = null)
        viewModelScope.launch {
            runCatching { cloud.acceptInvitation(invitation) }
                .onSuccess { _state.value = _state.value.copy(busy = false, notice = "Invitation accepted. Companion access is now linked."); refreshCompanions() }
                .onFailure { _state.value = _state.value.copy(busy = false, notice = firebaseMessage(it)) }
        }
    }

    fun revokeCompanion(linkId: String) {
        if (_state.value.firebaseUid == null) return
        viewModelScope.launch {
            runCatching { cloud.revokeCompanion(linkId) }
                .onSuccess { _state.value = _state.value.copy(notice = "Companion access revoked."); refreshCompanions() }
                .onFailure { _state.value = _state.value.copy(notice = firebaseMessage(it)) }
        }
    }

    private suspend fun refreshCompanions() {
        runCatching { cloud.linkedCompanions() }.onSuccess { _state.value = _state.value.copy(linkedCompanions = it) }
        if (_state.value.role == Role.COMPANION) runCatching { cloud.sharedJourneys() }.onSuccess { _state.value = _state.value.copy(sharedJourneys = it) }
    }

    fun signOut() {
        if (_state.value.firebaseUid != null) cloud.signOut()
        _state.value = UiState(screen = "onboarding", language = _state.value.language)
    }

    fun route(screen: String) { _state.value = _state.value.copy(screen = screen, notice = null) }

    fun startJourney(destination: String, minutes: Int, transport: String) {
        if (_state.value.firebaseUid == null) { _state.value = _state.value.copy(notice = "Sign in before saving a journey."); return }
        _state.value = _state.value.copy(journey = Journey(destination, minutes, transport), screen = "home", busy = false, notice = "Journey saved locally. Firebase will sync it when you reconnect.", events = _state.value.events + "Safe journey saved · $destination · $minutes min")
        viewModelScope.launch {
            runCatching { cloud.createJourney(destination, minutes, transport) }
                .onSuccess { _state.value = _state.value.copy(notice = "Journey synced with Firebase.") }
                .onFailure { _state.value = _state.value.copy(notice = firebaseMessage(it)) }
        }
    }

    fun recordSafety(level: SafetyLevel) {
        if (_state.value.firebaseUid == null) { _state.value = _state.value.copy(notice = "Sign in to save this safety record. No one has been notified."); return }
        val id = java.util.UUID.randomUUID().toString()
        _state.value = _state.value.copy(latestIncidentId = id, notice = "${level.name.replace('_', ' ')} saved locally. Firebase will sync it when you reconnect; nobody was notified.", events = _state.value.events + "${level.name.replace('_', ' ')} · saved to Firebase")
        viewModelScope.launch {
            runCatching { cloud.createIncident(level, id) }
                .onSuccess {
                    if (level == SafetyLevel.CHECK_IN || level == SafetyLevel.ALERT || level == SafetyLevel.SOS) {
                        val sent = runCatching { SafetyNotificationApi.sendToConsentingCompanions(id, level) }.getOrDefault(false)
                        _state.value = _state.value.copy(notice = if (sent) "${level.name.replace('_', ' ')} saved. A consenting companion was notified." else "${level.name.replace('_', ' ')} record synced with Firebase. No companion notification was sent; check-in and alert delivery requires the hosted API and their consent.")
                    }
                }
                .onFailure { _state.value = _state.value.copy(notice = firebaseMessage(it)) }
        }
    }

    fun safety(level: SafetyLevel) {
        recordSafety(level)
        if (level == SafetyLevel.SOS) _state.value = _state.value.copy(screen = "incident")
    }

    fun messageDraft(level: SafetyLevel, contactName: String, failed: Boolean = false) {
        val kind = if (level == SafetyLevel.CHECK_IN) "Check-in" else "Help"
        val message = when { failed -> "Could not open a messaging app. No message was sent."; contactName.isBlank() -> "Message draft could not be opened. No message was sent."; else -> "$kind message draft opened for $contactName. It is not sent until you send it in your messaging app." }
        _state.value = _state.value.copy(screen = "home", notice = message, events = _state.value.events + "$kind message prepared${contactName.takeIf { it.isNotBlank() }?.let { " for $it" } ?: ""} · not sent")
    }

    fun resolve() {
        if (_state.value.firebaseUid == null) { _state.value = _state.value.copy(screen = "timeline", notice = "Sign in to resolve an incident."); return }
        val id = _state.value.latestIncidentId
        if (id == null) {
            _state.value = _state.value.copy(screen = "timeline", notice = "No active incident was available to resolve.")
            return
        }
        val previousScreen = _state.value.screen
        _state.value = _state.value.copy(screen = "timeline", latestIncidentId = null, notice = "Resolution saved locally and will sync with Firebase when you reconnect.")
        viewModelScope.launch {
            runCatching { cloud.resolveIncident(id) }
                .onSuccess { _state.value = _state.value.copy(notice = "Incident marked resolved.", events = _state.value.events + "Marked safe · incident resolved") }
                .onFailure { _state.value = _state.value.copy(screen = previousScreen, latestIncidentId = id, notice = firebaseMessage(it)) }
        }
    }

    fun removeContact(id: String) {
        if (_state.value.firebaseUid == null) return
        val previousContacts = _state.value.contacts
        _state.value = _state.value.copy(contacts = previousContacts.filterNot { it.id == id }, notice = "Contact removal saved locally and will sync with Firebase when you reconnect.")
        viewModelScope.launch {
            runCatching { cloud.removeContact(id) }
                .onSuccess { _state.value = _state.value.copy(notice = "Trusted contact removed from Firebase.") }
                .onFailure { _state.value = _state.value.copy(contacts = previousContacts, notice = firebaseMessage(it)) }
        }
    }

    fun addContact(name: String, phone: String, onSaved: () -> Unit = {}) {
        if (_state.value.firebaseUid == null) { _state.value = _state.value.copy(notice = "Sign in to save trusted contacts."); return }
        val id = java.util.UUID.randomUUID().toString()
        val contact = Contact(id, name.trim(), phone.trim())
        _state.value = _state.value.copy(busy = false, contacts = _state.value.contacts + contact, notice = "Trusted contact saved locally. Firebase will sync it when you reconnect.")
        onSaved()
        viewModelScope.launch {
            runCatching { cloud.addContact(name, phone, id) }
                .onSuccess { _state.value = _state.value.copy(notice = "Trusted contact synced with Firebase. They have not been invited or notified.") }
                .onFailure { _state.value = _state.value.copy(notice = firebaseMessage(it)) }
        }
    }

    private fun firebaseMessage(error: Throwable): String {
        // Log only the exception type; account credentials and personal data must stay out of logs.
        android.util.Log.w("Safety4Women", "Firebase operation failed (${error.javaClass.simpleName})")
        val message = error.message.orEmpty()
        return when {
            message.contains("already in use", ignoreCase = true) -> "An account with this email already exists. Sign in instead."
            message.contains("password", ignoreCase = true) || message.contains("credential", ignoreCase = true) -> "Email or password is incorrect, or the password does not meet Firebase's policy."
            message.contains("network", ignoreCase = true) || error is java.net.UnknownHostException -> "Can't reach Firebase. Check your connection and try again."
            message.contains("PERMISSION_DENIED", ignoreCase = true) -> "Firebase denied this action. Check that the deployed Firestore rules permit it."
            else -> message.ifBlank { "Something went wrong. Please try again." }
        }
    }
}
