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
data class UiState(val role: Role? = null, val language: String = "en", val screen: String = "onboarding", val journey: Journey? = null, val contacts: List<Contact> = emptyList(), val notice: String? = null, val events: List<String> = emptyList(), val accountName: String = "", val accessToken: String? = null, val busy: Boolean = false, val highContrast: Boolean = false, val invitationToken: String? = null, val linkedCompanions: List<CompanionProfile> = emptyList(), val sharedJourneys: List<SharedJourney> = emptyList(), val latestIncidentId: String? = null, val demoMode: Boolean = false)

class SafetyViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState()); val state = _state.asStateFlow()
    fun select(role: Role) { _state.value = _state.value.copy(role = role, screen = "auth") }
    fun enterDemo() {
        val role = _state.value.role ?: Role.PRIMARY
        val demoPrimary = CompanionProfile("demo-link-primary", "demo-primary", "Amahle Dlamini", "amahle@example.com", true, true)
        val demoCompanion = CompanionProfile("demo-link-companion", "demo-companion", "Lerato Mokoena", "lerato@example.com", true, true)
        val demoJourney = SharedJourney("demo-journey", "demo-primary", "Campus to home", 35, "Ride-hailing", "active")
        _state.value = UiState(
            role = role,
            screen = "home",
            journey = if (role == Role.PRIMARY) Journey("Campus to home", 35, "Ride-hailing") else null,
            contacts = if (role == Role.PRIMARY) listOf(Contact("demo-contact", "Demo trusted person", "0000000000")) else emptyList(),
            events = listOf("Demo journey · Campus to home · 35 min", "Demo check-in · sample only"),
            accountName = if (role == Role.PRIMARY) "Demo Primary" else "Demo Companion",
            highContrast = false,
            linkedCompanions = listOf(if (role == Role.PRIMARY) demoCompanion else demoPrimary),
            sharedJourneys = if (role == Role.COMPANION) listOf(demoJourney) else emptyList(),
            demoMode = true,
            notice = "Demo only: sample data is temporary and no account or server record was created."
        )
    }
    fun authenticate(name: String, email: String, password: String, register: Boolean) {
        if (_state.value.busy) return
        _state.value = _state.value.copy(busy = true, notice = null)
        viewModelScope.launch {
            try {
                val response = if (register) SafetyApi.service.register(RegisterRequest(name.trim(), email.trim(), password, _state.value.role?.name ?: "PRIMARY")) else SafetyApi.service.login(AuthRequest(email.trim(), password))
                _state.value = _state.value.copy(screen = "home", accountName = response.user.displayName, accessToken = response.accessToken, role = if (response.user.role == "COMPANION") Role.COMPANION else Role.PRIMARY, busy = false, notice = "Signed in securely. Your session token stays in memory and will expire after 30 minutes.")
                runCatching { SafetyApi.service.getSettings("Bearer ${response.accessToken}") }.onSuccess { remote -> _state.value = _state.value.copy(language = remote.language, highContrast = remote.highContrast) }
                runCatching { SafetyApi.service.getContacts("Bearer ${response.accessToken}") }.onSuccess { remote -> _state.value = _state.value.copy(contacts = remote.contacts.map { Contact(it.id, it.name, it.phone) }) }
                runCatching { SafetyApi.service.getCompanions("Bearer ${response.accessToken}") }.onSuccess { remote -> _state.value = _state.value.copy(linkedCompanions = remote.companions) }
                if (response.user.role == "PRIMARY") runCatching { SafetyApi.service.getJourneys("Bearer ${response.accessToken}") }.onSuccess { remote ->
                    remote.journeys.firstOrNull()?.let { journey -> _state.value = _state.value.copy(journey = Journey(journey.destination, journey.expectedArrivalMinutes, journey.transportDetails.orEmpty())) }
                }
                if (response.user.role == "COMPANION") runCatching { SafetyApi.service.getSharedJourneys("Bearer ${response.accessToken}") }.onSuccess { remote -> _state.value = _state.value.copy(sharedJourneys = remote.journeys) }
            } catch (error: Exception) {
                _state.value = _state.value.copy(busy = false, notice = apiMessage(error))
            }
        }
    }
    fun language(code: String) {
        _state.value = _state.value.copy(language = code)
        val token = _state.value.accessToken ?: return
        viewModelScope.launch { runCatching { SafetyApi.service.saveSettings("Bearer $token", SettingsPatch(language = code)) }.onFailure { _state.value = _state.value.copy(notice = apiMessage(it)) } }
    }
    fun toggleContrast(enabled: Boolean) {
        _state.value = _state.value.copy(highContrast = enabled)
        val token = _state.value.accessToken ?: return
        viewModelScope.launch { runCatching { SafetyApi.service.saveSettings("Bearer $token", SettingsPatch(highContrast = enabled)) }.onFailure { _state.value = _state.value.copy(notice = apiMessage(it)) } }
    }
    fun createCompanionInvitation(email: String, allowJourneys: Boolean, allowCheckIns: Boolean) {
        if (_state.value.demoMode) {
            _state.value = _state.value.copy(invitationToken = "DEMO-CODE-ONLY-0000000000000000000000000000", notice = "Demo only: this sample code cannot invite a real companion.")
            return
        }
        val token = _state.value.accessToken ?: run { _state.value = _state.value.copy(notice = "Sign in to invite a companion."); return }
        _state.value = _state.value.copy(busy = true, notice = null, invitationToken = null)
        viewModelScope.launch {
            runCatching { SafetyApi.service.invite("Bearer $token", InviteRequest(email.trim(), allowJourneys, allowCheckIns)) }
                .onSuccess { invite -> _state.value = _state.value.copy(busy = false, invitationToken = invite.invitationToken, notice = "Invitation created. Share this one-time code with ${email.trim()} using a channel you trust. It expires in ${invite.expiresInHours} hours; no message was sent by the app.") }
                .onFailure { _state.value = _state.value.copy(busy = false, notice = apiMessage(it)) }
        }
    }
    fun acceptCompanionInvitation(invitation: String) {
        if (_state.value.demoMode) {
            _state.value = _state.value.copy(notice = "Demo only: invitation acceptance is simulated; no companion account was linked.")
            return
        }
        val token = _state.value.accessToken ?: run { _state.value = _state.value.copy(notice = "Sign in with a companion account to accept an invitation."); return }
        _state.value = _state.value.copy(busy = true, notice = null)
        viewModelScope.launch {
            runCatching { SafetyApi.service.acceptInvitation("Bearer $token", AcceptInviteRequest(invitation.trim())) }
                .onSuccess { _state.value = _state.value.copy(busy = false, notice = "Invitation accepted. Companion access is now linked."); refreshCompanions(token) }
                .onFailure { _state.value = _state.value.copy(busy = false, notice = apiMessage(it)) }
        }
    }
    fun revokeCompanion(linkId: String) {
        if (_state.value.demoMode) {
            _state.value = _state.value.copy(linkedCompanions = _state.value.linkedCompanions.filterNot { it.id == linkId }, notice = "Demo only: sample link removed for this session.")
            return
        }
        val token = _state.value.accessToken ?: return
        viewModelScope.launch {
            runCatching { SafetyApi.service.revokeCompanion("Bearer $token", linkId) }
                .onSuccess { _state.value = _state.value.copy(notice = "Companion access revoked."); refreshCompanions(token) }
                .onFailure { _state.value = _state.value.copy(notice = apiMessage(it)) }
        }
    }
    private suspend fun refreshCompanions(token: String) {
        runCatching { SafetyApi.service.getCompanions("Bearer $token") }.onSuccess { _state.value = _state.value.copy(linkedCompanions = it.companions) }
        if (_state.value.role == Role.COMPANION) runCatching { SafetyApi.service.getSharedJourneys("Bearer $token") }.onSuccess { _state.value = _state.value.copy(sharedJourneys = it.journeys) }
    }
    fun signOut() { _state.value = UiState(screen = "onboarding", language = _state.value.language) }
    fun route(screen: String) { _state.value = _state.value.copy(screen = screen, notice = null) }
    fun startJourney(destination: String, minutes: Int, transport: String) {
        if (_state.value.demoMode) {
            _state.value = _state.value.copy(journey = Journey(destination, minutes, transport), screen = "home", notice = "Demo only: journey shown for this session; it was not saved to a server.", events = _state.value.events + "Demo journey · $destination · $minutes min")
            return
        }
        val token = _state.value.accessToken ?: run { _state.value = _state.value.copy(notice = "Sign in before saving a journey."); return }
        _state.value = _state.value.copy(busy = true, notice = null)
        viewModelScope.launch {
            try {
                SafetyApi.service.createJourney("Bearer $token", JourneyRequest(destination, minutes, transport.takeIf { it.isNotBlank() }))
                _state.value = _state.value.copy(journey = Journey(destination, minutes, transport), screen = "home", busy = false, notice = "Journey saved to your account. Live location and companion alerts are not enabled.", events = _state.value.events + "Safe Journey saved · $destination · $minutes min")
            } catch (error: Exception) { _state.value = _state.value.copy(busy = false, notice = apiMessage(error)) }
        }
    }
    fun recordSafety(level: SafetyLevel) {
        if (_state.value.demoMode) {
            val label = level.name.replace('_', ' ')
            _state.value = _state.value.copy(notice = "Demo only: $label shown for this session. Nobody was notified and nothing was sent.", events = _state.value.events + "Demo $label · sample only")
            return
        }
        val token = _state.value.accessToken ?: run { _state.value = _state.value.copy(notice = "Sign in to save this safety record. No one has been notified."); return }
        viewModelScope.launch {
            runCatching { SafetyApi.service.createIncident("Bearer $token", IncidentRequest(journeyId = null, level = level.name)) }
                .onSuccess { response -> _state.value = _state.value.copy(latestIncidentId = response.incident.id, notice = "${level.name.replace('_',' ')} record saved. Nobody was notified.", events = _state.value.events + "${level.name.replace('_',' ')} · saved to account") }
                .onFailure { _state.value = _state.value.copy(notice = apiMessage(it)) }
        }
    }
    fun safety(level: SafetyLevel) {
        recordSafety(level)
        if (level == SafetyLevel.SOS) _state.value = _state.value.copy(screen = "incident")
    }
    fun messageDraft(level: SafetyLevel, contactName: String, failed: Boolean = false) { val kind = if (level == SafetyLevel.CHECK_IN) "Check-in" else "Help"; val message = when { failed -> "Could not open a messaging app. No message was sent."; contactName.isBlank() -> "Message draft could not be opened. No message was sent."; else -> "$kind message draft opened for $contactName. It is not sent until you send it in your messaging app." }; _state.value = _state.value.copy(screen = "home", notice = message, events = _state.value.events + "$kind message prepared${contactName.takeIf { it.isNotBlank() }?.let { " for $it" } ?: ""} · not sent") }
    fun resolve() {
        val snapshot = _state.value
        val incidentId = snapshot.latestIncidentId
        val token = snapshot.accessToken
        if (incidentId != null && token != null) viewModelScope.launch {
            runCatching { SafetyApi.service.resolveIncident("Bearer $token", incidentId) }
                .onSuccess { _state.value = _state.value.copy(screen = "timeline", latestIncidentId = null, notice = "Incident marked resolved.", events = _state.value.events + "Marked safe · incident resolved") }
                .onFailure { _state.value = _state.value.copy(notice = apiMessage(it)) }
        } else _state.value = _state.value.copy(screen = "timeline", notice = "No server incident was available to resolve.", events = _state.value.events + "Marked safe · local session")
    }
    fun removeContact(id: String) {
        if (_state.value.demoMode) {
            _state.value = _state.value.copy(contacts = _state.value.contacts.filterNot { it.id == id }, notice = "Demo only: sample contact removed for this session.")
            return
        }
        val token = _state.value.accessToken ?: return
        viewModelScope.launch {
            runCatching { SafetyApi.service.removeContact("Bearer $token", id) }
                .onSuccess { _state.value = _state.value.copy(contacts = _state.value.contacts.filterNot { it.id == id }, notice = "Trusted contact removed from your account.") }
                .onFailure { _state.value = _state.value.copy(notice = apiMessage(it)) }
        }
    }
    fun addContact(name: String, phone: String, onSaved: () -> Unit = {}) {
        if (_state.value.demoMode) {
            _state.value = _state.value.copy(contacts = _state.value.contacts + Contact("demo-${System.currentTimeMillis()}", name.trim(), phone.trim()), notice = "Demo only: contact added for this session; not saved to an account.")
            onSaved()
            return
        }
        val token = _state.value.accessToken ?: run { _state.value = _state.value.copy(notice = "Sign in to save trusted contacts."); return }
        _state.value = _state.value.copy(busy = true, notice = null)
        viewModelScope.launch {
            runCatching { SafetyApi.service.addContact("Bearer $token", ContactRequest(name.trim(), phone.trim())) }
                .onSuccess { result -> _state.value = _state.value.copy(busy = false, contacts = _state.value.contacts + Contact(result.contact.id, result.contact.name, result.contact.phone), notice = "Trusted contact saved to your account. They have not been invited or notified."); onSaved() }
                .onFailure { _state.value = _state.value.copy(busy = false, notice = apiMessage(it)) }
        }
    }

    private fun apiMessage(error: Throwable): String = when (error) {
        is retrofit2.HttpException -> when (error.code()) { 400 -> "Please check the information and try again."; 401 -> "Email or password is incorrect, or your session expired."; 409 -> "An account with this email already exists."; else -> "The service returned an error (${error.code()}). Try again shortly." }
        is java.net.UnknownHostException, is java.net.ConnectException -> "Can't reach the safety service. Check your connection and API setup, then retry."
        else -> error.message?.takeIf { it.contains("SAFETY_API_BASE_URL") } ?: "Something went wrong. Please try again."
    }
}
