package za.co.safety4women

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

data class AuthUser(val id: String, val email: String, @SerializedName("display_name") val displayName: String, val role: String)
data class AuthRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String, val role: String)
data class AuthResponse(val accessToken: String, val user: AuthUser)
data class JourneyRequest(val destination: String, val expectedArrivalMinutes: Int, val transportDetails: String?)
data class ApiJourney(val id: String, val destination: String, @SerializedName("expected_arrival_minutes") val expectedArrivalMinutes: Int, @SerializedName("transport_details") val transportDetails: String?, val status: String)
data class JourneyResponse(val journey: ApiJourney)
data class IncidentRequest(val journeyId: String? = null, val level: String, val latitude: Double? = null, val longitude: Double? = null)
data class ApiIncident(val id: String, val status: String)
data class IncidentResponse(val incident: ApiIncident, val delivery: String? = null)
data class SettingsDto(val language: String, @SerializedName("high_contrast") val highContrast: Boolean)
data class SettingsPatch(val language: String? = null, val highContrast: Boolean? = null)
data class ContactDto(val id: String, val name: String, val phone: String)
data class ContactListResponse(val contacts: List<ContactDto>)
data class ContactResponse(val contact: ContactDto)
data class ContactRequest(val name: String, val phone: String, val allowCheckIns: Boolean = true, val allowHelpMessages: Boolean = true)
data class InviteRequest(val email: String, val allowJourneys: Boolean, val allowCheckIns: Boolean)
data class InviteResponse(val invitationToken: String, val expiresInHours: Int, val delivery: String)
data class AcceptInviteRequest(val invitationToken: String)
data class CompanionProfile(val id: String, @SerializedName("user_id") val userId: String, @SerializedName("display_name") val displayName: String, val email: String, @SerializedName("allow_journeys") val allowJourneys: Boolean, @SerializedName("allow_check_ins") val allowCheckIns: Boolean)
data class CompanionListResponse(val companions: List<CompanionProfile>)
data class JourneyListResponse(val journeys: List<ApiJourney>)
data class SharedJourney(val id: String, @SerializedName("user_id") val userId: String, val destination: String, @SerializedName("expected_arrival_minutes") val expectedArrivalMinutes: Int, @SerializedName("transport_details") val transportDetails: String?, val status: String)
data class SharedJourneyListResponse(val journeys: List<SharedJourney>)

interface SafetyApiService {
    @POST("v1/auth/register") suspend fun register(@Body body: RegisterRequest): AuthResponse
    @POST("v1/auth/login") suspend fun login(@Body body: AuthRequest): AuthResponse
    @POST("v1/journeys") suspend fun createJourney(@Header("Authorization") bearer: String, @Body body: JourneyRequest): JourneyResponse
    @POST("v1/incidents") suspend fun createIncident(@Header("Authorization") bearer: String, @Body body: IncidentRequest): IncidentResponse
    @retrofit2.http.PATCH("v1/incidents/{id}/resolve") suspend fun resolveIncident(@Header("Authorization") bearer: String, @Path("id") id: String): Map<String, Any?>
    @GET("v1/settings") suspend fun getSettings(@Header("Authorization") bearer: String): SettingsDto
    @PATCH("v1/settings") suspend fun saveSettings(@Header("Authorization") bearer: String, @Body body: SettingsPatch): SettingsDto
    @GET("v1/contacts") suspend fun getContacts(@Header("Authorization") bearer: String): ContactListResponse
    @POST("v1/contacts") suspend fun addContact(@Header("Authorization") bearer: String, @Body body: ContactRequest): ContactResponse
    @retrofit2.http.DELETE("v1/contacts/{id}") suspend fun removeContact(@Header("Authorization") bearer: String, @Path("id") id: String)
    @GET("v1/journeys") suspend fun getJourneys(@Header("Authorization") bearer: String): JourneyListResponse
    @GET("v1/companions") suspend fun getCompanions(@Header("Authorization") bearer: String): CompanionListResponse
    @GET("v1/companions/journeys") suspend fun getSharedJourneys(@Header("Authorization") bearer: String): SharedJourneyListResponse
    @POST("v1/companions/invitations") suspend fun invite(@Header("Authorization") bearer: String, @Body body: InviteRequest): InviteResponse
    @POST("v1/companions/accept") suspend fun acceptInvitation(@Header("Authorization") bearer: String, @Body body: AcceptInviteRequest): Map<String, Any?>
    @retrofit2.http.DELETE("v1/companions/{id}") suspend fun revokeCompanion(@Header("Authorization") bearer: String, @Path("id") id: String)
}

object SafetyApi {
    val service: SafetyApiService by lazy {
        val base = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
        require(base.startsWith("https://")) { "Configure SAFETY_API_BASE_URL with your hosted HTTPS API URL." }
        val client = OkHttpClient.Builder().build()
        Retrofit.Builder().baseUrl(base).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(SafetyApiService::class.java)
    }
}
