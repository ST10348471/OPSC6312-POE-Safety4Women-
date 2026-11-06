package za.co.safety4women

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

/** Firebase is the app's account and online data store. Firestore's Android SDK also
 * queues document writes while offline and synchronizes them after connectivity returns. */
class FirebaseCloudRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    data class Account(val uid: String, val displayName: String, val email: String, val role: Role)

    private fun uid(): String = auth.currentUser?.uid ?: error("Sign in to continue.")
    private fun userDoc(userId: String = uid()) = db.collection("users").document(userId)

    suspend fun restoreSession(): Account? {
        val user = auth.currentUser ?: return null
        val profile = userDoc(user.uid).get().await()
        if (!profile.exists()) return null
        val role = runCatching { Role.valueOf(profile.getString("role") ?: Role.PRIMARY.name) }.getOrDefault(Role.PRIMARY)
        return Account(user.uid, profile.getString("displayName") ?: user.displayName.orEmpty(), user.email.orEmpty(), role)
    }

    fun signOut() { auth.signOut() }

    suspend fun register(name: String, email: String, password: String, role: Role): Account {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: error("Firebase did not return the new account.")
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()).await()
        val profile = mapOf(
            "uid" to user.uid,
            "displayName" to name.trim(),
            "email" to email.trim().lowercase(),
            "role" to role.name,
            "language" to "en",
            "highContrast" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        userDoc(user.uid).set(profile).await()
        return Account(user.uid, name.trim(), email.trim().lowercase(), role)
    }

    suspend fun signIn(email: String, password: String): Account {
        val user = auth.signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Firebase did not return the signed-in account.")
        val snapshot = userDoc(user.uid).get().await()
        if (!snapshot.exists()) {
            // Repair a profile document if account creation was interrupted after Auth succeeded.
            val name = user.displayName?.takeIf(String::isNotBlank) ?: email.substringBefore('@')
            userDoc(user.uid).set(
                mapOf("uid" to user.uid, "displayName" to name, "email" to email.trim().lowercase(), "role" to Role.PRIMARY.name, "language" to "en", "highContrast" to false),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            return Account(user.uid, name, email.trim().lowercase(), Role.PRIMARY)
        }
        val role = runCatching { Role.valueOf(snapshot.getString("role") ?: Role.PRIMARY.name) }.getOrDefault(Role.PRIMARY)
        return Account(user.uid, snapshot.getString("displayName") ?: user.displayName.orEmpty(), user.email.orEmpty(), role)
    }

    suspend fun signInWithGoogle(idToken: String, selectedRole: Role, language: String): Account {
        val user = auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user
            ?: error("Firebase did not return the signed-in account.")
        val profileRef = userDoc(user.uid)
        val profile = profileRef.get().await()
        if (!profile.exists()) {
            profileRef.set(mapOf(
                "uid" to user.uid, "displayName" to (user.displayName ?: user.email.orEmpty().substringBefore('@')),
                "email" to user.email.orEmpty().lowercase(), "role" to selectedRole.name,
                "language" to language, "highContrast" to false, "createdAt" to FieldValue.serverTimestamp()
            )).await()
            return Account(user.uid, user.displayName.orEmpty(), user.email.orEmpty(), selectedRole)
        }
        val role = runCatching { Role.valueOf(profile.getString("role") ?: Role.PRIMARY.name) }.getOrDefault(Role.PRIMARY)
        return Account(user.uid, profile.getString("displayName") ?: user.displayName.orEmpty(), user.email.orEmpty(), role)
    }

    suspend fun settings(): SettingsDto {
        val data = userDoc().get().await()
        return SettingsDto(data.getString("language") ?: "en", data.getBoolean("highContrast") ?: false)
    }

    suspend fun saveSettings(language: String? = null, highContrast: Boolean? = null) {
        val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
        language?.let { updates["language"] = it }
        highContrast?.let { updates["highContrast"] = it }
        userDoc().set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    /** Device tokens are private to their signed-in owner and are never exposed to other app users. */
    fun saveDeviceToken(token: String) {
        val tokenId = java.security.MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray()).joinToString("") { "%02x".format(it) }
        userDoc().collection("deviceTokens").document(tokenId).set(mapOf(
            "token" to token, "updatedAt" to FieldValue.serverTimestamp(), "platform" to "android"
        ), com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { android.util.Log.w("Safety4Women", "Could not register FCM token (${it.javaClass.simpleName})") }
    }

    private fun contacts() = userDoc().collection("contacts")
    suspend fun getContacts(): List<Contact> = contacts().get().await().documents.mapNotNull { doc ->
        val name = doc.getString("name") ?: return@mapNotNull null
        val phone = doc.getString("phone") ?: return@mapNotNull null
        Contact(doc.id, name, phone)
    }

    suspend fun addContact(name: String, phone: String, id: String = java.util.UUID.randomUUID().toString()): Contact {
        val ref = contacts().document(id)
        ref.set(mapOf("name" to name.trim(), "phone" to phone.trim(), "createdAt" to FieldValue.serverTimestamp())).await()
        return Contact(ref.id, name.trim(), phone.trim())
    }

    suspend fun removeContact(id: String) { contacts().document(id).delete().await() }

    private fun journeys(owner: String = uid()) = userDoc(owner).collection("journeys")
    suspend fun createJourney(destination: String, minutes: Int, transport: String) {
        journeys().add(mapOf(
            "ownerUid" to uid(), "destination" to destination.trim(), "expectedArrivalMinutes" to minutes,
            "transportDetails" to transport.trim(), "status" to "ACTIVE", "createdAt" to FieldValue.serverTimestamp()
        )).await()
    }

    suspend fun ownJourney(): Journey? {
        val doc = journeys().whereEqualTo("status", "ACTIVE").limit(1).get().await().documents.firstOrNull() ?: return null
        return Journey(doc.getString("destination") ?: return null, (doc.getLong("expectedArrivalMinutes") ?: 0L).toInt(), doc.getString("transportDetails").orEmpty())
    }

    private fun incidents(owner: String = uid()) = userDoc(owner).collection("incidents")
    suspend fun createIncident(level: SafetyLevel, id: String = java.util.UUID.randomUUID().toString()): String {
        val ref = incidents().document(id)
        ref.set(mapOf("ownerUid" to uid(), "level" to level.name, "status" to "ACTIVE", "createdAt" to FieldValue.serverTimestamp())).await()
        return ref.id
    }

    suspend fun resolveIncident(id: String) { incidents().document(id).update("status", "RESOLVED", "resolvedAt", FieldValue.serverTimestamp()).await() }

    suspend fun createInvitation(email: String, allowJourneys: Boolean, allowCheckIns: Boolean): InviteResponse {
        val profile = userDoc().get().await()
        check(profile.getString("role") == Role.PRIMARY.name) { "Only a primary account can invite a companion." }
        val code = newInviteCode()
        db.collection("companionInvitations").document(code).set(mapOf(
            "inviterUid" to uid(), "inviterName" to (profile.getString("displayName") ?: "Safety 4 Women user"),
            "inviteeEmail" to email.trim().lowercase(), "allowJourneys" to allowJourneys,
            "allowCheckIns" to allowCheckIns, "status" to "PENDING",
            "createdAt" to FieldValue.serverTimestamp(), "expiresAt" to Timestamp(java.util.Date(System.currentTimeMillis() + 72L * 60 * 60 * 1000))
        )).await()
        return InviteResponse(code, 72, "manual_share")
    }

    suspend fun acceptInvitation(codeInput: String) {
        val user = auth.currentUser ?: error("Sign in with a companion account to accept an invitation.")
        val code = codeInput.trim()
        val inviteRef = db.collection("companionInvitations").document(code)
        db.runTransaction { transaction ->
            val profile = transaction.get(userDoc(user.uid))
            val invite = transaction.get(inviteRef)
            check(profile.getString("role") == Role.COMPANION.name) { "Sign in with a companion account to accept this invitation." }
            check(invite.getString("status") == "PENDING") { "This invitation is no longer available." }
            check(invite.getString("inviteeEmail") == user.email?.lowercase()) { "Sign in with the email address the invitation was sent to." }
            check((invite.getTimestamp("expiresAt")?.toDate()?.time ?: 0L) > System.currentTimeMillis()) { "This invitation has expired." }
            val inviterUid = invite.getString("inviterUid") ?: error("Invitation is missing its owner.")
            val linkRef = db.collection("companionLinks").document("${inviterUid}_${user.uid}")
            transaction.update(inviteRef, mapOf("status" to "ACCEPTED", "acceptedByUid" to user.uid, "acceptedAt" to FieldValue.serverTimestamp()))
            transaction.set(linkRef, mapOf(
                "primaryUid" to inviterUid, "companionUid" to user.uid,
                "allowJourneys" to (invite.getBoolean("allowJourneys") ?: false),
                "allowCheckIns" to (invite.getBoolean("allowCheckIns") ?: false),
                "invitationCode" to code, "status" to "ACTIVE", "createdAt" to FieldValue.serverTimestamp()
            ))
        }.await()
    }

    suspend fun linkedCompanions(): List<CompanionProfile> {
        val uid = uid()
        val role = userDoc().get().await().getString("role")
        val query = if (role == Role.COMPANION.name) {
            db.collection("companionLinks").whereEqualTo("companionUid", uid).whereEqualTo("status", "ACTIVE")
        } else {
            db.collection("companionLinks").whereEqualTo("primaryUid", uid).whereEqualTo("status", "ACTIVE")
        }
        return query.get().await().documents.mapNotNull { link ->
            val otherUid = (if (role == Role.COMPANION.name) link.getString("primaryUid") else link.getString("companionUid")) ?: return@mapNotNull null
            val profile = userDoc(otherUid).get().await()
            CompanionProfile(link.id, otherUid, profile.getString("displayName") ?: "Companion", profile.getString("email") ?: "", link.getBoolean("allowJourneys") ?: false, link.getBoolean("allowCheckIns") ?: false)
        }
    }

    suspend fun sharedJourneys(): List<SharedJourney> {
        val companionUid = uid()
        val links = db.collection("companionLinks").whereEqualTo("companionUid", companionUid).whereEqualTo("status", "ACTIVE").whereEqualTo("allowJourneys", true).get().await()
        return links.documents.flatMap { link ->
            val primaryUid = link.getString("primaryUid") ?: return@flatMap emptyList()
            journeys(primaryUid).whereEqualTo("status", "ACTIVE").get().await().documents.mapNotNull { doc ->
                val destination = doc.getString("destination") ?: return@mapNotNull null
                SharedJourney(doc.id, primaryUid, destination, (doc.getLong("expectedArrivalMinutes") ?: 0L).toInt(), doc.getString("transportDetails"), doc.getString("status") ?: "ACTIVE")
            }
        }
    }

    suspend fun revokeCompanion(linkId: String) {
        val ref = db.collection("companionLinks").document(linkId)
        val link = ref.get().await()
        check(link.getString("primaryUid") == uid()) { "Only the primary account can revoke this companion." }
        ref.update("status", "REVOKED", "revokedAt", FieldValue.serverTimestamp()).await()
    }

    private fun newInviteCode(): String {
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
