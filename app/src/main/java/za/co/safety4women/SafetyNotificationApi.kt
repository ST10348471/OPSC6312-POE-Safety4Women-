package za.co.safety4women

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/** Sends a consent-checked alert through the hosted API; secrets and FCM credentials stay server-side. */
object SafetyNotificationApi {
    suspend fun sendToConsentingCompanions(incidentId: String, level: SafetyLevel): Boolean {
        val baseUrl = BuildConfig.SAFETY_API_BASE_URL.trimEnd('/')
        if (baseUrl.isBlank()) return false
        val user = FirebaseAuth.getInstance().currentUser ?: return false
        val idToken = user.getIdToken(false).await().token ?: return false
        return withContext(Dispatchers.IO) {
            val connection = (URL("$baseUrl/v1/notifications/companion-alert").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Authorization", "Bearer $idToken")
                setRequestProperty("Content-Type", "application/json")
            }
            try {
                val body = "{\"incidentId\":\"${incidentId.replace("\"", "")}\",\"level\":\"${level.name}\"}"
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                if (connection.responseCode !in 200..299) false
                else connection.inputStream.bufferedReader().use { org.json.JSONObject(it.readText()).optBoolean("notified", false) }
            } catch (error: Exception) {
                android.util.Log.w("Safety4Women", "Companion push request failed (${error.javaClass.simpleName})")
                false
            } finally { connection.disconnect() }
        }
    }
}
