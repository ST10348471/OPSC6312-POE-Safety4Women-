package za.co.safety4women

data class SettingsDto(val language: String, val highContrast: Boolean)
data class InviteResponse(val invitationToken: String, val expiresInHours: Int, val delivery: String)
data class CompanionProfile(val id: String, val userId: String, val displayName: String, val email: String, val allowJourneys: Boolean, val allowCheckIns: Boolean)
data class SharedJourney(val id: String, val userId: String, val destination: String, val expectedArrivalMinutes: Int, val transportDetails: String?, val status: String)
