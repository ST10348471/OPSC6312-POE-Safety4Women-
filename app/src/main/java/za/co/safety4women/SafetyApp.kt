package za.co.safety4women

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Navy = Color(0xFF123331); private val Teal = Color(0xFF087E78); private val Red = Color(0xFFB42318); private val Ink = Color(0xFF18302E); private val Muted = Color(0xFF647875); private val Canvas = Color(0xFFF2FAF8)

@Composable fun SafetyApp(vm: SafetyViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    Surface(color = if (state.highContrast) Color.White else Canvas, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (state.demoMode) DemoModeBanner(vm)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (state.screen) {
                    "onboarding" -> Onboarding(state, vm)
                    "auth" -> Authentication(state, vm)
                    "home" -> Home(state, vm)
                    "settings" -> Settings(state, vm)
                    "journey" -> JourneySetup(vm)
                    "companions" -> Companions(state, vm)
                    "safety" -> SafetyTiers(state, vm)
                    "incident" -> Incident(vm)
                    else -> Timeline(vm)
                }
            }
        }
    }
}

@Composable private fun DemoModeBanner(vm: SafetyViewModel) {
    Surface(color = Color(0xFFFFF2CC)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, tint = Color(0xFF755400), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("DEMO MODE · SAMPLE DATA", color = Color(0xFF563F00), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text("Temporary only · no API or real account", color = Color(0xFF755400), style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = vm::signOut) { Text("Exit demo", color = Color(0xFF563F00)) }
        }
    }
}

@Composable private fun Authentication(state: UiState, vm: SafetyViewModel) {
    var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; var registering by remember { mutableStateOf(true) }
    val validEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val valid = validEmail && password.length >= (if (registering) 10 else 1) && (!registering || name.trim().length >= 2)
    Scaffold(topBar = { AppHeader(if (registering) "Create your account" else "Sign in") { vm.route("onboarding") } }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(horizontal = 24.dp, vertical = 28.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.PersonOutline, null, tint = Teal, modifier = Modifier.size(40.dp)); Text(if (registering) "Your account, your control." else "Welcome back.", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
            Text("Account requests use the hosted Safety 4 Women service. Passwords are sent over HTTPS and stored as one-way bcrypt hashes.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = vm::enterDemo, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Visibility, null, tint = Teal)
                Spacer(Modifier.width(8.dp))
                Text("Explore the app in demo mode", color = Teal, fontWeight = FontWeight.SemiBold)
            }
            Text("Opens the screens with sample data. No account is created, and changes are temporary.", color = Muted, style = MaterialTheme.typography.bodySmall)
            if (registering) OutlinedTextField(name, { name = it }, label = { Text("Your name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), isError = name.isNotBlank() && name.trim().length < 2)
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, modifier = Modifier.fillMaxWidth(), isError = email.isNotBlank() && !validEmail)
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password), visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth(), supportingText = { if (registering) Text("Use at least 10 characters.") })
            state.notice?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Button(onClick = { vm.authenticate(name, email, password, registering) }, enabled = valid && !state.busy, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Teal)) { if (state.busy) CircularProgressIndicator(Modifier.size(21.dp), color = Color.White, strokeWidth = 2.dp) else Text(if (registering) "Create account" else "Sign in", fontWeight = FontWeight.SemiBold) }
            TextButton(onClick = { registering = !registering }) { Text(if (registering) "Already have an account? Sign in" else "New here? Create an account", color = Teal) }
            Text("Your password is never saved on this device. Sign in again after the in-memory session ends.", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AppHeader(title: String, back: (() -> Unit)? = null, settingsClick: (() -> Unit)? = null) {
    TopAppBar(title = { Text(title, fontWeight = FontWeight.Bold, color = Ink) }, navigationIcon = { if (back != null) IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Back", tint = Ink) } }, actions = { if (settingsClick != null) IconButton(onClick = settingsClick) { Icon(Icons.Default.Settings, "Settings", tint = Ink) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Canvas, titleContentColor = Ink, navigationIconContentColor = Ink))
}

@Composable private fun Settings(state: UiState, vm: SafetyViewModel) = Scaffold(topBar = { AppHeader("Settings") { vm.route("home") } }) { p ->
    Column(Modifier.padding(p).padding(22.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Make the app yours.", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
        Text(if (state.demoMode) "Sample profile. Changes stay in this demo session." else "Signed in as ${state.accountName}. Settings are saved to your account when the service is available.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Text("LANGUAGE", style = MaterialTheme.typography.labelMedium, color = Muted, letterSpacing = 1.sp)
        Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { LanguageChoice("English", state.language == "en") { vm.language("en") }; LanguageChoice("isiZulu", state.language == "zu") { vm.language("zu") } }
        ElevatedCard(shape = RoundedCornerShape(16.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("High contrast", color = Ink, fontWeight = FontWeight.SemiBold); Text("Increase contrast for key controls", color = Muted, style = MaterialTheme.typography.bodySmall) }
                Switch(checked = state.highContrast, onCheckedChange = vm::toggleContrast)
            }
        }
        Text("The isiZulu language option currently translates onboarding only; remaining screens are English.", color = Muted, style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = vm::signOut, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) { Text("Sign out", color = Teal) }
    }
}
@Composable private fun Onboarding(state: UiState, vm: SafetyViewModel) = Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Spacer(Modifier.height(24.dp))
    Box(Modifier.size(68.dp).background(Color(0xFFDDF3EF), RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Shield, null, tint = Teal, modifier = Modifier.size(34.dp)) }
    Spacer(Modifier.height(18.dp)); Text("Safety 4 Women", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Navy)
    Text(if (state.language == "en") "Your safety, always." else "Ukuphepha kwakho, njalo.", style = MaterialTheme.typography.bodyLarge, color = Muted)
    Spacer(Modifier.height(28.dp)); Text(if (state.language == "en") "CHOOSE YOUR LANGUAGE" else "KHETHA ULIMI", style = MaterialTheme.typography.labelMedium, color = Muted, letterSpacing = 1.4.sp)
    Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { LanguageChoice("English", state.language == "en") { vm.language("en") }; LanguageChoice("isiZulu", state.language == "zu") { vm.language("zu") } }
    Spacer(Modifier.height(28.dp)); Text(if (state.language == "en") "HOW WILL YOU USE THE APP?" else "UZOLUSEBENZISA KANJANI UHLELO?", style = MaterialTheme.typography.labelMedium, color = Muted, letterSpacing = 1.1.sp, modifier = Modifier.align(Alignment.Start))
    Spacer(Modifier.height(12.dp)); RoleCard(if (state.language == "zu") "Ngifuna ukuphepha" else "I want to stay safe", if (state.language == "zu") "Hlela uhambo futhi usebenzise amathuluzi okuphepha" else "Plan a journey and access personal safety tools", Icons.Default.Person, { vm.select(Role.PRIMARY) })
    Spacer(Modifier.height(12.dp)); RoleCard(if (state.language == "zu") "Ngifuna ukuvikela omunye" else "I want to support someone", if (state.language == "zu") "Buka uhambo lomuntu omethembayo" else "Support someone you trust on their journey", Icons.Default.Favorite, { vm.select(Role.COMPANION) })
    Spacer(Modifier.height(24.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Lock, null, tint = Teal, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(7.dp)); Text("Your choices stay in your control", color = Muted, style = MaterialTheme.typography.bodySmall) }
    if (state.language == "zu") Text("Le demo ihumusha isikrini sokuqala kuphela; ezinye izikrini ziseNgisini.", color = Muted, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    Spacer(Modifier.height(8.dp)); Text("Built for South Africa  ·  🇿🇦", color = Muted, style = MaterialTheme.typography.labelSmall)
}
@Composable private fun RowScope.LanguageChoice(label: String, selected: Boolean, click: () -> Unit) { Surface(onClick = click, modifier = Modifier.weight(1f).height(44.dp), color = if (selected) Teal else Color.Transparent, shape = RoundedCornerShape(12.dp)) { Box(contentAlignment = Alignment.Center) { Text(label, color = if (selected) Color.White else Ink, fontWeight = FontWeight.SemiBold) } } }
@Composable private fun RoleCard(title: String, body: String, icon: androidx.compose.ui.graphics.vector.ImageVector, click: () -> Unit) = ElevatedCard(onClick = click, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)) { Row(Modifier.padding(horizontal = 18.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(Color(0xFFE4F5F1), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Teal, modifier = Modifier.size(25.dp)) }; Spacer(Modifier.width(15.dp)); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, fontWeight = FontWeight.Bold, color = Ink, style = MaterialTheme.typography.titleMedium); Text(body, color = Muted, style = MaterialTheme.typography.bodySmall) }; Icon(Icons.Default.ChevronRight, null, tint = Teal) } }

@Composable private fun Home(state: UiState, vm: SafetyViewModel) = Scaffold(topBar = { AppHeader("Safety 4 Women", settingsClick = { vm.route("settings") }) }, bottomBar = { NavigationBar(containerColor = Color.White) { NavigationBarItem(selected = true, onClick = { vm.route("home") }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") }); NavigationBarItem(selected = false, onClick = { vm.route("safety") }, icon = { Icon(Icons.Default.Shield, null) }, label = { Text("Safety") }); NavigationBarItem(selected = false, onClick = { vm.route("companions") }, icon = { Icon(Icons.Default.Group, null) }, label = { Text("People") }); NavigationBarItem(selected = false, onClick = { vm.route("timeline") }, icon = { Icon(Icons.Default.History, null) }, label = { Text("Activity") }) } }, floatingActionButton = { if (state.role != Role.COMPANION) ExtendedFloatingActionButton(onClick = { vm.route("journey") }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Plan a journey") }, containerColor = Teal, contentColor = Color.White) }) { padding ->
    Column(Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(if (state.role == Role.COMPANION) "Your support space" else "Hello, ${state.accountName.ifBlank { "there" }}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Ink)
        Text(if (state.role == Role.COMPANION) "Support someone you trust." else "A little more peace of mind, one step at a time.", style = MaterialTheme.typography.bodyMedium, color = Muted)
        state.notice?.let { Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F3F0)), shape = RoundedCornerShape(16.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) { Icon(Icons.Default.Info, null, tint = Teal, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(10.dp)); Text(it, color = Ink, style = MaterialTheme.typography.bodySmall) } } }
        ElevatedCard(shape = RoundedCornerShape(22.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(38.dp).background(Color(0xFFE4F5F1), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Map, null, tint = Teal) }; Spacer(Modifier.width(11.dp)); Column { Text(if (state.role == Role.COMPANION) "Shared journeys" else if (state.journey == null) "Your next journey" else if (state.demoMode) "Demo journey" else "Journey saved", fontWeight = FontWeight.Bold, color = Ink); Text(if (state.role == Role.COMPANION) "Permission-based view" else if (state.journey == null) "Nothing planned yet" else if (state.demoMode) "Sample · this session only" else "Saved to your account", color = Muted, style = MaterialTheme.typography.bodySmall) } };
            if (state.role == Role.COMPANION) {
                if (state.sharedJourneys.isEmpty()) Text("No journey is currently shared with your account.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                state.sharedJourneys.forEach { shared -> Text("${shared.destination}\nExpected arrival in ${shared.expectedArrivalMinutes} min${shared.transportDetails?.let { " · $it" } ?: ""}", color = Ink, style = MaterialTheme.typography.bodyMedium) }
                if (state.sharedJourneys.isNotEmpty()) Text(if (state.demoMode) "Demo only: this sample journey is not shared by a real person." else "Journey details are shared through an accepted companion permission. Live tracking and notifications are not enabled.", color = Muted, style = MaterialTheme.typography.labelSmall)
            } else if (state.journey != null) {
                Text("${state.journey.destination}\n${state.journey.etaMinutes} min · ${state.journey.transport}", color = Ink, style = MaterialTheme.typography.bodyMedium)
                Text("Live tracking and companion alerts are not enabled.", color = Muted, style = MaterialTheme.typography.labelSmall)
            } else Text("Plan where you’re going and when you expect to arrive.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        } }
        Text("Your safety tools", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Ink)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { ActionTile("Check-in & SOS", Icons.Default.Shield, { vm.route("safety") }, Modifier.weight(1f)); ActionTile("Trusted people", Icons.Default.Group, { vm.route("companions") }, Modifier.weight(1f)) }
        Text(if (state.demoMode) "Demo data stays in this session. No one is notified; live location is not enabled." else "Journey and incident records are saved to your account. No live location, push alerts, or emergency calls are sent by this app.", style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}
@Composable
fun ActionTile(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, click: () -> Unit, modifier: Modifier) = ElevatedCard(onClick = click, modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) { Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Icon(icon, null, tint = Teal, modifier = Modifier.size(25.dp)); Text(text, fontWeight = FontWeight.SemiBold, color = Ink, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium) } }

@Composable
fun JourneySetup(vm: SafetyViewModel) { var destination by remember { mutableStateOf("") }; var minutes by remember { mutableStateOf("30") }; var transport by remember { mutableStateOf("") }; val demoMode = vm.state.collectAsState().value.demoMode; Scaffold(topBar = { AppHeader("Plan a safe journey") { vm.route("home") } }) { p -> Column(Modifier.padding(p).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Make a plan before you set off.", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold); Text(if (demoMode) "Demo only: this sample plan stays in memory for this session." else "Your plan is saved to your account. It does not share live location or send notifications.", style = MaterialTheme.typography.bodyMedium, color = Muted); OutlinedTextField(destination, { destination = it }, label = { Text("Where are you going?") }, leadingIcon = { Icon(Icons.Default.LocationOn, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true, isError = destination.isNotBlank() && destination.trim().length < 2); OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit).take(3) }, label = { Text("Expected arrival (minutes)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true, isError = (minutes.toIntOrNull() ?: 0) !in 1..360); OutlinedTextField(transport, { transport = it }, label = { Text("Transport notes (optional)") }, modifier = Modifier.fillMaxWidth(), supportingText = { Text(if (demoMode) "Sample only; not sent to an account." else "Saved with your account journey.") }); Button(onClick = { if (destination.trim().length >= 2 && (minutes.toIntOrNull() ?: 0) in 1..360) vm.startJourney(destination.trim(), minutes.toInt(), transport.ifBlank { "No transport notes" }) }, enabled = destination.trim().length >= 2 && (minutes.toIntOrNull() ?: 0) in 1..360 && !vm.state.value.busy, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Teal)) { if (vm.state.value.busy) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Text(if (demoMode) "Preview journey" else "Save journey plan") } } } }

@Composable
fun Companions(state: UiState, vm: SafetyViewModel) {
    var name by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }; var inviteEmail by remember { mutableStateOf("") }; var inviteCode by remember { mutableStateOf("") }
    var allowJourneyShare by remember { mutableStateOf(true) }; var allowCheckInShare by remember { mutableStateOf(true) }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    Scaffold(topBar = { AppHeader("Trusted people") { vm.route("home") } }) { p -> Column(Modifier.padding(p).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Keep the people you trust close.", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
            if (state.demoMode) Text("Demo only: companion links and invitation codes are samples. No real person is connected.", color = Muted, style = MaterialTheme.typography.bodySmall)
        if (state.role == Role.PRIMARY) {
            Text("Companion access requires an explicit invitation and the other person’s acceptance. You can revoke access at any time.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            state.linkedCompanions.forEach { companion ->
                ElevatedCard(shape = RoundedCornerShape(18.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(companion.displayName, color = Ink, fontWeight = FontWeight.Bold); Text(companion.email, color = Muted, style = MaterialTheme.typography.bodySmall); Text("Journeys: ${if (companion.allowJourneys) "allowed" else "off"} · Check-ins: ${if (companion.allowCheckIns) "allowed" else "off"}", color = Muted, style = MaterialTheme.typography.labelSmall) }
                        TextButton(onClick = { vm.revokeCompanion(companion.id) }) { Text("Revoke", color = Red) }
                    }
                }
            }
            Text("Invite a companion", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
            OutlinedTextField(inviteEmail, { inviteEmail = it }, label = { Text("Companion’s account email") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Share safe journey details", Modifier.weight(1f), color = Ink); Switch(allowJourneyShare, { allowJourneyShare = it }) }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Share check-in and alert records", Modifier.weight(1f), color = Ink); Switch(allowCheckInShare, { allowCheckInShare = it }) }
            Button(onClick = { vm.createCompanionInvitation(inviteEmail, allowJourneyShare, allowCheckInShare) }, enabled = android.util.Patterns.EMAIL_ADDRESS.matcher(inviteEmail.trim()).matches() && (allowJourneyShare || allowCheckInShare) && !state.busy, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Teal)) { Text(if (state.busy) "Working…" else "Create invitation code") }
            state.invitationToken?.let { code ->
                ElevatedCard(shape = RoundedCornerShape(14.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Share this one-time code privately", color = Ink, fontWeight = FontWeight.SemiBold); Text(code, color = Teal, style = MaterialTheme.typography.bodySmall); TextButton(onClick = { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(code)) }) { Text("Copy invitation code", color = Teal) } }
                }
            }
        } else {
            Text("Accept an invitation addressed to your signed-in account email.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(inviteCode, { inviteCode = it }, label = { Text("Invitation code") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { vm.acceptCompanionInvitation(inviteCode) }, enabled = inviteCode.length >= 32 && !state.busy, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Teal)) { Text(if (state.busy) "Working…" else "Accept invitation") }
            state.linkedCompanions.forEach { person -> Text("Linked to ${person.displayName} (${person.email})", color = Ink) }
        }
        state.notice?.let { Text(it, color = Muted, style = MaterialTheme.typography.bodySmall) }
        Text(if (state.demoMode) "Sample contacts are temporary. SMS opens a draft only; no message is sent by the demo." else "Trusted phone contacts are stored in your account. Inviting a companion creates a separate consent link; no invitation message is sent automatically.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        if (state.contacts.isEmpty()) EmptyState("No trusted people added", "Add someone you may want to contact. Their details stay in this session.") else state.contacts.forEach { c -> ElevatedCard(shape = RoundedCornerShape(18.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccountCircle, null, tint = Teal, modifier = Modifier.size(42.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(c.name, fontWeight = FontWeight.Bold, color = Ink); Text(c.phone, color = Muted) }; IconButton(onClick = { vm.removeContact(c.id) }) { Icon(Icons.Default.Delete, "Remove", tint = Red) } } } }
        Text("Add a trusted person", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.Bold)
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it }, label = { Text("Phone number") }, singleLine = true, isError = phone.isNotBlank() && phone.count(Char::isDigit) < 7, supportingText = { if (phone.isNotBlank() && phone.count(Char::isDigit) < 7) Text("Enter a valid phone number.") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.addContact(name, phone) { name = ""; phone = "" } }, enabled = name.trim().length >= 2 && phone.count(Char::isDigit) >= 7 && !state.busy, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Teal)) { Text("Save trusted contact") }
    } }
}
@Composable
fun EmptyState(title: String, body: String) = Card { Column(Modifier.padding(24.dp)) { Text(title, fontWeight = FontWeight.Bold); Text(body) } }
@Composable
fun SafetyTiers(state: UiState, vm: SafetyViewModel) {
    var messageLevel by remember { mutableStateOf<SafetyLevel?>(null) }
    var showNoContacts by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    Scaffold(topBar = { AppHeader("Safety tools") { vm.route("home") } }) { p -> Column(Modifier.padding(p).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Choose what you need right now.", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.Bold)
        Text("Check-ins and alerts open a message draft for someone you choose. Nothing is sent until you review and send it in your messaging app.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        SafetyButton("Check in", "Record a check-in and optionally draft a message.", Color(0xFF16794A)) { vm.recordSafety(SafetyLevel.CHECK_IN); if (state.contacts.any { it.phone.isNotBlank() }) messageLevel = SafetyLevel.CHECK_IN else showNoContacts = true }
        SafetyButton("Ask for help", "Record an alert and optionally draft a help message.", Color(0xFFB54708)) { vm.recordSafety(SafetyLevel.ALERT); if (state.contacts.any { it.phone.isNotBlank() }) messageLevel = SafetyLevel.ALERT else showNoContacts = true }
        SafetyButton("SOS", "Open emergency call options.", Red) { vm.safety(SafetyLevel.SOS) }
        Text("If you are in immediate danger in South Africa, use the emergency call options. This app does not contact emergency services automatically.", style = MaterialTheme.typography.bodySmall, color = Muted)
    } }
    messageLevel?.let { level ->
        AlertDialog(onDismissRequest = { messageLevel = null }, title = { Text(if (level == SafetyLevel.CHECK_IN) "Choose someone to check in with" else "Choose someone to ask for help") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Your messaging app will open with a draft. Review it before sending.", color = Muted); state.contacts.filter { it.phone.isNotBlank() }.forEach { contact -> TextButton(onClick = { val body = if (level == SafetyLevel.CHECK_IN) "Hi ${contact.name}, I’m checking in to let you know I’m okay. Please reply when you can." else "Hi ${contact.name}, I need support. Please call me when you can."; try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:${android.net.Uri.encode(contact.phone)}")).putExtra("sms_body", body)); vm.messageDraft(level, contact.name) } catch (_: android.content.ActivityNotFoundException) { vm.messageDraft(level, contact.name, failed = true) }; messageLevel = null }, modifier = Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Person, null, tint = Teal); Spacer(Modifier.width(10.dp)); Column(horizontalAlignment = Alignment.Start) { Text(contact.name, color = Ink, fontWeight = FontWeight.SemiBold); Text(contact.phone, color = Muted, style = MaterialTheme.typography.bodySmall) } } } } } }, confirmButton = { TextButton(onClick = { messageLevel = null }) { Text("Cancel") } })
    }
    if (showNoContacts) AlertDialog(onDismissRequest = { showNoContacts = false }, title = { Text("Add a trusted person first") }, text = { Text("A message draft needs a saved phone number. Add someone you trust, then choose them from the list.") }, confirmButton = { TextButton(onClick = { showNoContacts = false; vm.route("companions") }) { Text("Add trusted person") } }, dismissButton = { TextButton(onClick = { showNoContacts = false }) { Text("Cancel") } })
}
@Composable
fun SafetyButton(title: String, body: String, color: Color, click: () -> Unit) = Button(onClick = click, modifier = Modifier.fillMaxWidth().heightIn(min = 86.dp), colors = ButtonDefaults.buttonColors(containerColor = color), shape = RoundedCornerShape(16.dp)) { Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(body, style = MaterialTheme.typography.bodySmall) } }
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Incident(vm: SafetyViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Scaffold(containerColor = Color(0xFF25191B), topBar = { TopAppBar(title = { Text("Emergency options", color = Color.White, fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = { vm.route("home") }) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF25191B))) }) { p -> Column(Modifier.fillMaxSize().padding(p).padding(22.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        Box(Modifier.size(60.dp).background(Color(0xFF4B292D), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Warning, null, tint = Color(0xFFFFB4AB), modifier = Modifier.size(30.dp)) }
        Text("You’re in control.", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Text("This screen does not place a call or share your location. Choose a number below to open your phone app.", color = Color(0xFFE6D6D8), style = MaterialTheme.typography.bodyMedium)
        EmergencyCall("Call emergency services", "112 · mobile", "112", context)
        EmergencyCall("Call SAPS", "10111", "10111", context)
        EmergencyCall("GBV Command Centre", "0800 428 428 · 24/7", "0800428428", context)
        Text("These verified South African numbers open in your phone app. Confirm the call there.", color = Color(0xFFE6D6D8), style = MaterialTheme.typography.bodySmall)
        Button(onClick = vm::resolve, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16794A)), shape = RoundedCornerShape(14.dp)) { Text("I’m safe — close SOS") }
    } }
}
@Composable private fun EmergencyCall(title: String, number: String, dial: String, context: android.content.Context) { Button(onClick = { val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$dial")); context.startActivity(intent) }, modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF44282C)), shape = RoundedCornerShape(15.dp)) { Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(title, color = Color.White, fontWeight = FontWeight.SemiBold); Text(number, color = Color(0xFFE6D6D8), style = MaterialTheme.typography.bodySmall) }; Icon(Icons.Default.Call, null, tint = Color.White) } }
@Composable
fun Timeline(vm: SafetyViewModel) = Scaffold(topBar = { AppHeader("Activity") { vm.route("home") } }) { p -> Column(Modifier.padding(p).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) { Text("Your activity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Ink); Text("Recorded for this session. No location or message delivery is implied.", style = MaterialTheme.typography.bodySmall, color = Muted); if (vm.state.value.events.isEmpty()) EmptyState("Nothing recorded yet", "Journey plans and safety check-ins will appear here.") else vm.state.value.events.reversed().forEach { item -> ElevatedCard(shape = RoundedCornerShape(14.dp), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.History, null, tint = Teal); Spacer(Modifier.width(12.dp)); Text(item, color = Ink) } } }; Button(onClick = { vm.route("home") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Teal)) { Text("Return to home") } } }
