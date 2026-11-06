package za.co.safety4women

import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private val UiLanguage = compositionLocalOf { "en" }

@Composable
fun ProvideUiLanguage(language: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(UiLanguage provides language, content = content)
}

/** UI copy for English and isiZulu. Safety wording should be reviewed by a fluent isiZulu speaker before public release. */
fun translateUi(text: String, language: String): String {
    if (language != "zu") return text
    val translations = mapOf(
        "Safety 4 Women" to "Ukuphepha Kwabesifazane",
        "Your safety, always." to "Ukuphepha kwakho, ngaso sonke isikhathi.",
        "CHOOSE YOUR LANGUAGE" to "KHETHA ULIMI LWAKHO",
        "HOW WILL YOU USE THE APP?" to "UZOLUSEBENZISA KANJANI ULUHLELO?",
        "I want to stay safe" to "Ngifuna ukuhlala ngiphephile",
        "Plan a journey and access personal safety tools" to "Hlela uhambo futhi usebenzise amathuluzi okuphepha",
        "I want to support someone" to "Ngifuna ukusekela othile",
        "Support someone you trust on their journey" to "Sekela umuntu omethembayo ohambweni lwakhe",
        "Your choices stay in your control" to "Izinqumo zakho zisezandleni zakho",
        "Built for South Africa  ·  🇿🇦" to "Yenzelwe iNingizimu Afrika  ·  🇿🇦",
        "Create your account" to "Dala i-akhawunti yakho", "Sign in" to "Ngena ngemvume",
        "Your account, your control." to "I-akhawunti yakho, izinqumo zakho.", "Welcome back." to "Siyakwamukela futhi.",
        "Your account is protected by Firebase Authentication. Passwords are handled securely by Firebase and are never stored in this app." to "I-akhawunti yakho ivikelwe yi-Firebase Authentication. Amaphasiwedi aphathwa ngokuphephile yi-Firebase futhi awagcinwa kulolu hlelo.",
        "Your account, your control." to "I-akhawunti yakho, izinqumo zakho.", "Welcome back." to "Siyakwamukela futhi.",
        "Your name" to "Igama lakho", "Sign in with Google" to "Ngena nge-Google",
        "Journey and incident records are saved to your account. Check-ins and alerts can notify a consenting companion when push delivery is configured. No live location is shared, and the app never calls emergency services automatically." to "Amarekhodi ohambo nezigameko agcinwa ku-akhawunti yakho. Ukuqinisekisa ukuphepha nezixwayiso kungazisa umngane ovumile uma ukuthumela izaziso kumisiwe. Indawo yakho ayilandeliwe bukhoma futhi uhlelo alushayi izingcingo eziphuthumayo ngokuzenzakalela.",
        "Journey details are shared through accepted companion permission. No live location is shared." to "Imininingwane yohambo yabiwa ngemvume yomngane eyamukelwe. Indawo yakho ayabelwa bukhoma.",
        "Signed in with Firebase. Firestore keeps your account data synced." to "Ungene nge-Firebase. I-Firestore ivumelanisa imininingwane ye-akhawunti yakho.",
        "Signed in with Google. Firebase keeps your account data synced." to "Ungene nge-Google. I-Firebase ivumelanisa imininingwane ye-akhawunti yakho.",
        "Google sign-in needs a Firebase Web client ID. Enable Google in Firebase Authentication and download the updated google-services.json." to "Ukungena nge-Google kudinga i-Web client ID ye-Firebase. Vumela i-Google ku-Firebase Authentication bese ulanda i-google-services.json entsha.",
        "Google sign-in could not complete. Check that Google Authentication is enabled and the Android SHA fingerprints are registered." to "Ukungena nge-Google akuphumelelanga. Hlola ukuthi i-Google Authentication ivuliwe yini nokuthi ama-SHA fingerprint e-Android abhalisiwe.",
        "Invitation accepted. Companion access is now linked." to "Isimemo samukelwe. Ukufinyelela komngane sekuxhunyiwe.",
        "Companion access revoked." to "Imvume yomngane ihoxisiwe.", "Incident marked resolved." to "Isigameko simakwe njengesixazululiwe.",
        "Contact removal saved locally and will sync with Firebase when you reconnect." to "Ukususwa koxhumana naye kugcinwe kudivayisi futhi kuzovumelaniswa ne-Firebase uma uxhuma futhi.",
        "Trusted contact removed from Firebase." to "Umuntu omethembayo ususiwe ku-Firebase.",
        "Trusted contact saved locally. Firebase will sync it when you reconnect." to "Umuntu omethembayo ugcinwe kudivayisi. I-Firebase izomvumelanisa uma uxhuma futhi.",
        "Trusted contact synced with Firebase. They have not been invited or notified." to "Umuntu omethembayo uvumelanisiwe ne-Firebase. Akamenywanga futhi akaziswanga.",
        "Sign in to invite a companion." to "Ngena ngemvume ukuze umeme umngane.",
        "Sign in with a companion account to accept an invitation." to "Ngena nge-akhawunti yomngane ukuze wamukele isimemo.",
        "Sign in before saving a journey." to "Ngena ngemvume ngaphambi kokulondoloza uhambo.",
        "Sign in to save this safety record. No one has been notified." to "Ngena ngemvume ukuze ulondoloze leli rekhodi lokuphepha. Akekho owazisiwe.",
        "Sign in to resolve an incident." to "Ngena ngemvume ukuze uxazulule isigameko.",
        "Sign in to save trusted contacts." to "Ngena ngemvume ukuze ulondoloze abantu obathembayo.",
        "Journey saved locally. Firebase will sync it when you reconnect." to "Uhambo lugcinwe kudivayisi. I-Firebase izoluvumelanisa uma uxhuma futhi.",
        "Journey synced with Firebase." to "Uhambo luvumelanisiwe ne-Firebase.",
        "Resolution saved locally and will sync with Firebase when you reconnect." to "Isixazululo sigcinwe kudivayisi futhi sizovumelaniswa ne-Firebase uma uxhuma futhi.",
        "Google sign-in could not complete. Check that Google Authentication is enabled and the Android SHA fingerprints are registered." to "Ukungena nge-Google akuphumelelanga. Hlola ukuthi i-Google Authentication ivuliwe nokuthi ama-SHA fingerprint e-Android abhalisiwe.",
        "A person who trusts you checked in. Please contact them if appropriate." to "Umuntu okuthembayo uqinisekisile ukuthi uphephile. Mthinte uma kufanele.",
        "A person who trusts you asked for support. Please contact them if appropriate." to "Umuntu okuthembayo ucele ukwesekwa. Mthinte uma kufanele.",
        "A person who trusts you activated SOS. Please contact them if appropriate." to "Umuntu okuthembayo uvule i-SOS. Mthinte uma kufanele.",
        "Your name" to "Igama lakho", "Email" to "I-imeyili", "Password" to "Iphasiwedi",
        "Use at least 10 characters." to "Sebenzisa okungenani izinhlamvu eziyi-10.", "Create account" to "Dala i-akhawunti", "Continue with Google" to "Qhubeka nge-Google",
        "Already have an account? Sign in" to "Usunayo i-akhawunti? Ngena ngemvume", "New here? Create an account" to "Umusha lapha? Dala i-akhawunti",
        "Your password is never saved on this device. Firebase keeps your signed-in session until you sign out." to "Iphasiwedi yakho ayigcinwa kule divayisi. I-Firebase igcina isikhathi sakho sokungena kuze kube uyaphuma.",
        "Back" to "Emuva", "Settings" to "Izilungiselelo", "Make the app yours." to "Yenza uhlelo lube olwakho.",
        "LANGUAGE" to "ULIMI", "High contrast" to "Ukugqama okuphezulu", "Increase contrast for key controls" to "Khulisa umehluko ukuze izinkinobho zibonakale kahle",
        "The isiZulu language option currently translates onboarding only; remaining screens are English." to "Ulimi lwesiZulu selusetshenziswa kuzo zonke izikrini zalolu hlelo.", "Sign out" to "Phuma",
        "Signed in with Firebase. Firestore keeps your account data synced." to "Ungene nge-Firebase. I-Firestore ivumelanisa imininingwane ye-akhawunti yakho.",
        "Signed in with Google. Firebase keeps your account data synced." to "Ungene nge-Google. I-Firebase ivumelanisa imininingwane ye-akhawunti yakho.",
        "Create invitation code" to "Dala ikhodi yesimemo", "Working…" to "Kuyasebenza…", "Accept invitation" to "Yamukela isimemo",
        "No journey is currently shared with your account." to "Alukho uhambo olwabiwe ne-akhawunti yakho okwamanje.",
        "Journey details are shared through accepted companion permission. No live location is shared." to "Imininingwane yohambo yabiwa ngemvume yomngane eyamukelwe. Indawo ayabelwa bukhoma.",
        "Journey and incident records are saved to your account. Check-ins and alerts can notify a consenting companion when push delivery is configured. No live location is shared, and the app never calls emergency services automatically." to "Amarekhodi ohambo nezigameko agcinwa ku-akhawunti yakho. Ukuqinisekisa ukuphepha nezixwayiso kungazisa umngane ovumile uma ukuthumela izaziso kumisiwe. Indawo ayabelwa bukhoma futhi uhlelo alushayi ucingo lwezimo eziphuthumayo ngokuzenzakalela.",
        "Check-ins and alerts open a message draft for someone you choose. Nothing is sent until you review and send it in your messaging app." to "Ukuqinisekisa ukuphepha nezixwayiso kuvula uhlaka lomyalezo lomuntu omkhethayo. Akukho okuthunyelwayo uze uwubuyekeze bese uwuthumela kuhlelo lwakho lwemiyalezo.",
        "Your messaging app will open with a draft. Review it before sending." to "Uhlelo lwakho lwemiyalezo luzovuleka nohlaka. Lubuyekeze ngaphambi kokuluthumela.",
        "Your activity" to "Okwenzekile kuwe", "Recorded for this session. No location or message delivery is implied." to "Kuqoshwe kulesi sikhathi sohlelo. Akusho ukuthi indawo noma umyalezo kwabiwe.",
        "Home" to "Ekhaya", "Safety" to "Ukuphepha", "People" to "Abantu", "Activity" to "Okwenzekile", "Plan a journey" to "Hlela uhambo",
        "Your support space" to "Indawo yakho yokusekela", "Support someone you trust." to "Sekela umuntu omethembayo.",
        "A little more peace of mind, one step at a time." to "Ukuthula kwengqondo okwengeziwe, isinyathelo ngesinyathelo.",
        "Shared journeys" to "Uhambo olwabiwe", "Permission-based view" to "Ukubuka okunemvume", "Your next journey" to "Uhambo lwakho olulandelayo",
        "Journey saved" to "Uhambo lulondoloziwe", "Nothing planned yet" to "Akukho okuhleliwe okwamanje", "Saved to your account" to "Kulondolozwe ku-akhawunti yakho",
        "No journey is currently shared with your account." to "Alukho uhambo olwabiwe ne-akhawunti yakho okwamanje.",
        "Journey details are shared through an accepted companion permission. Live tracking and notifications are not enabled." to "Imininingwane yohambo yabiwa ngemvume yomngane eyamukelwe. Ukulandelela bukhoma nezaziso akuvunyelwe.",
        "Live tracking and companion alerts are not enabled." to "Ukulandelela bukhoma nezaziso zomngane akuvunyelwe.",
        "Plan where you’re going and when you expect to arrive." to "Hlela ukuthi uya kuphi nokuthi ulindele ukufika nini.",
        "Your safety tools" to "Amathuluzi akho okuphepha", "Check-in & SOS" to "Ukuqinisekisa ukuphepha ne-SOS", "Trusted people" to "Abantu obathembayo",
        "Journey and incident records are saved to your account. No live location, push alerts, or emergency calls are sent by this app." to "Amarekhodi ohambo nezigameko alondolozwa ku-akhawunti yakho. Lolu hlelo aluthumeli indawo bukhoma, izaziso, noma izingcingo eziphuthumayo.",
        "Plan a safe journey" to "Hlela uhambo oluphephile", "Make a plan before you set off." to "Hlela ngaphambi kokuba uhambe.",
        "Your plan is saved to your account. It does not share live location or send notifications." to "Uhlelo lwakho lulondolozwa ku-akhawunti yakho. Alwabelani ngendawo bukhoma futhi aluthumeli izaziso.",
        "Where are you going?" to "Uyaphi?", "Expected arrival (minutes)" to "Isikhathi okulindele ukufika ngaso (imizuzu)",
        "Transport notes (optional)" to "Imininingwane yezokuthutha (ongakukhetha)", "Saved with your account journey." to "Kugcinwa kanye nohambo ku-akhawunti yakho.", "Save journey plan" to "Londoloza uhlelo lohambo",
        "Trusted people" to "Abantu obathembayo", "Keep the people you trust close." to "Gcina abantu obathembayo beseduze.",
        "Companion access requires an explicit invitation and the other person’s acceptance. You can revoke access at any time." to "Umngane udinga isimemo esicacile nokwamukelwa omunye umuntu. Ungayihoxisa imvume noma nini.",
        "Invite a companion" to "Mema umngane", "Companion’s account email" to "I-imeyili ye-akhawunti yomngane", "Share safe journey details" to "Yabelana ngemininingwane yohambo oluphephile",
        "Share check-in and alert records" to "Yabelana ngamarekhodi okuqinisekisa ukuphepha nezixwayiso", "Create invitation code" to "Dala ikhodi yesimemo",
        "Working…" to "Kuyasebenza…", "Share this one-time code privately" to "Yabelana ngale khodi yesikhathi esisodwa ngasese", "Copy invitation code" to "Kopisha ikhodi yesimemo",
        "Accept an invitation addressed to your signed-in account email." to "Yamukela isimemo esithunyelwe ekhelini le-imeyili le-akhawunti ongene ngayo.", "Invitation code" to "Ikhodi yesimemo", "Accept invitation" to "Yamukela isimemo",
        "Trusted phone contacts are stored in your account. Inviting a companion creates a separate consent link; no invitation message is sent automatically." to "Izinombolo zabantu obathembayo zigcinwa ku-akhawunti yakho. Ukumema umngane kudala imvume ehlukile; awuthunyelwa umyalezo wesimemo ngokuzenzakalela.",
        "No trusted people added" to "Akekho umuntu omethembayo owengeziwe", "Add someone you may want to contact. Their details are saved to your Firebase account." to "Faka umuntu ongase ufune ukumthinta. Imininingwane yakhe igcinwa ku-akhawunti yakho ye-Firebase.",
        "Add a trusted person" to "Engeza umuntu omethembayo", "Name" to "Igama", "Phone number" to "Inombolo yocingo", "Enter a valid phone number." to "Faka inombolo yocingo evumelekile.", "Save trusted contact" to "Londoloza umuntu omethembayo", "Remove" to "Susa", "Revoke" to "Hoxisa imvume",
        "Safety tools" to "Amathuluzi okuphepha", "Choose what you need right now." to "Khetha okudingayo njengamanje.",
        "Check-ins and alerts open a message draft for someone you choose. Nothing is sent until you review and send it in your messaging app." to "Ukuqinisekisa ukuphepha nezixwayiso kuvula uhlaka lomyalezo lomuntu omkhethayo. Akukho okuthunyelwayo uze uwubuyekeze futhi uwuthumele kuhlelo lwakho lwemiyalezo.",
        "Recording a check-in or alert can send a push notification to a consenting companion when delivery is configured. A text message is only drafted; review and send it yourself." to "Ukuqopha ukuqinisekisa ukuphepha noma isexwayiso kungathumela isaziso kumngane ovumile uma ukuthumela kumisiwe. Umlayezo wombhalo ulungiswa njengohlaka kuphela; wubuyekeze bese uzithumelela wona.",
        "IsiZulu translations cover the app interface. Please report any wording that is unclear." to "Ukuhumusha kwesiZulu kuhlanganisa zonke izikrini zohlelo. Sicela ubike noma yimaphi amagama angaqondakali.",
        "Check in" to "Qinisekisa ukuphepha", "Record a check-in and optionally draft a message." to "Rekhoda ukuqinisekisa ukuphepha, futhi uma uthanda lungisa uhlaka lomyalezo.",
        "Ask for help" to "Cela usizo", "Record an alert and optionally draft a help message." to "Rekhoda isexwayiso, futhi uma uthanda lungisa uhlaka lomyalezo wokucela usizo.",
        "SOS" to "SOS", "Open emergency call options." to "Vula izinketho zezingcingo eziphuthumayo.",
        "If you are in immediate danger in South Africa, use the emergency call options. This app does not contact emergency services automatically." to "Uma usengozini esheshayo eNingizimu Afrika, sebenzisa izinketho zezingcingo eziphuthumayo. Lolu hlelo aluthinti abezimo eziphuthumayo ngokuzenzakalela.",
        "Choose someone to check in with" to "Khetha umuntu ozomqinisekisa ukuthi uphephile", "Choose someone to ask for help" to "Khetha umuntu ozocela kuye usizo",
        "Your messaging app will open with a draft. Review it before sending." to "Uhlelo lwakho lwemiyalezo luzovuleka nohlaka. Lubuyekeze ngaphambi kokuluthumela.", "Cancel" to "Khansela", "Add a trusted person first" to "Qala ngokwengeza umuntu omethembayo",
        "A message draft needs a saved phone number. Add someone you trust, then choose them from the list." to "Uhlaka lomyalezo ludinga inombolo yocingo elondoloziwe. Engeza umuntu omethembayo bese umkhetha ohlwini.", "Add trusted person" to "Engeza umuntu omethembayo",
        "Emergency options" to "Izinketho zezimo eziphuthumayo", "You’re in control." to "Nguwe olawulayo.",
        "This screen does not place a call or share your location. Choose a number below to open your phone app." to "Lesi sikrini asishayi ucingo futhi asabelani ngendawo yakho. Khetha inombolo ngezansi ukuze uvule uhlelo lwefoni.",
        "Call emergency services" to "Shayela abezimo eziphuthumayo", "Call SAPS" to "Shayela i-SAPS", "GBV Command Centre" to "Isikhungo Sokulwa Nodlame Olusekelwe Kubulili",
        "These verified South African numbers open in your phone app. Confirm the call there." to "Lezi izinombolo zaseNingizimu Afrika eziqinisekisiwe. Ucingo luzovuleka kuhlelo lwefoni; luqinisekise khona.", "I’m safe — close SOS" to "Ngiphephile — vala i-SOS",
        "Your activity" to "Okwenzekile kuwe", "Recorded for this session. No location or message delivery is implied." to "Kuqoshwe kulesi sikhathi sohlelo. Akusho ukuthi indawo noma umyalezo kwabiwe.",
        "Nothing recorded yet" to "Akukho okuqoshiwe okwamanje", "Journey plans and safety check-ins will appear here." to "Izinhlelo zohambo nokuqinisekisa ukuphepha kuzovela lapha.", "Return to home" to "Buyela ekhasini lasekhaya",
        "No active incident was available to resolve." to "Asikho isigameko esisebenzayo esingaxazululwa.",
        "Invitation accepted. Companion access is now linked." to "Isimemo samukelwe. Ukufinyelela komngane sekuxhunyiwe.",
        "Invitation created. Share this one-time code with" to "Isimemo sidaliwe. Yabelana ngale khodi yesikhathi esisodwa no",
        "Firebase denied this action. Check that the deployed Firestore rules permit it." to "I-Firebase yenqabile lesi senzo. Hlola ukuthi imithetho ye-Firestore evunyelwe iyasivumela.",
        "Sign in to invite a companion." to "Ngena ngemvume ukuze umeme umngane.", "Sign in with a companion account to accept an invitation." to "Ngena nge-akhawunti yomngane ukuze wamukele isimemo.",
        "Sign in before saving a journey." to "Ngena ngemvume ngaphambi kokulondoloza uhambo.", "Sign in to save this safety record. No one has been notified." to "Ngena ngemvume ukuze ulondoloze leli rekhodi lokuphepha. Akekho owazisiwe.",
        "Sign in to resolve an incident." to "Ngena ngemvume ukuze uxazulule isigameko.", "Sign in to save trusted contacts." to "Ngena ngemvume ukuze ulondoloze abantu obathembayo.",
        "An account with this email already exists. Sign in instead." to "I-akhawunti enale imeyili isikhona. Ngena ngemvume esikhundleni salokho.",
        "Email or password is incorrect, or the password does not meet Firebase's policy." to "I-imeyili noma iphasiwedi ayilungile, noma iphasiwedi ayihambisani nemithetho ye-Firebase.",
        "Could not open a messaging app. No message was sent." to "Akukwazanga ukuvula uhlelo lwemiyalezo. Akukho myalezo othunyelwe.",
        "Message draft could not be opened. No message was sent." to "Uhlaka lomyalezo alukwazanga ukuvulwa. Akukho myalezo othunyelwe.",
        "No active incident was available to resolve." to "Asikho isigameko esisebenzayo esingaxazululwa.", "Marked safe · incident resolved" to "Kuphephile · isigameko sixazululiwe",
        "Can't reach Firebase. Check your connection and try again." to "Ayikwazi ukufinyelela ku-Firebase. Hlola uxhumano lwakho bese uzama futhi.",
        "Something went wrong. Please try again." to "Kukhona okungahambanga kahle. Sicela uzame futhi."
    )
    translations[text]?.let { return it }
    return when {
        text.startsWith("Hello, ") -> "Sawubona, ${text.removePrefix("Hello, ")}"
        text.startsWith("Signed in as ") -> "Ungene njengo-${text.removePrefix("Signed in as ").substringBefore(". Settings")}. Izilungiselelo zigcinwa ku-akhawunti yakho ye-Firebase."
        text.startsWith("Linked to ") -> "Kuxhunywe ku-${text.removePrefix("Linked to ")}"
        text.startsWith("Safe journey saved · ") -> text.replace("Safe journey saved · ", "Uhambo oluphephile lulondoloziwe · ").replace(" min", " imizuzu")
        text.startsWith("Journey synced with Firebase") -> "Uhambo luvumelanisiwe ne-Firebase."
        text.startsWith("Journey saved locally. Firebase will sync it") -> "Uhambo lugcinwe kudivayisi. I-Firebase izoluvumelanisa uma uxhuma futhi."
        text.startsWith("Resolution saved locally") -> "Isixazululo sigcinwe kudivayisi futhi sizovumelaniswa ne-Firebase uma uxhuma futhi."
        text.startsWith("Trusted contact saved locally") -> "Umuntu omethembayo ugcinwe kudivayisi. I-Firebase izomvumelanisa uma uxhuma futhi."
        text.startsWith("Trusted contact synced with Firebase") -> "Umuntu omethembayo uvumelanisiwe ne-Firebase. Akamenywanga futhi akaziswanga."
        text.startsWith("Trusted contact removed from Firebase") -> "Umuntu omethembayo ususiwe ku-Firebase."
        text.startsWith("Contact removal saved locally") -> "Ukususwa koxhumana naye kugcinwe kudivayisi futhi kuzovumelaniswa ne-Firebase uma uxhuma futhi."
        text.startsWith("Invitation created. Share this one-time code with ") -> text.replace("Invitation created. Share this one-time code with ", "Isimemo sidaliwe. Yabelana ngale khodi yesikhathi esisodwa no").replace(" privately. It expires in ", " ngasese. Iphelelwa yisikhathi emahoreni angu-").replace(" hours; the app sent no message.", "; uhlelo aluthumelanga myalezo.")
        text.startsWith("Check-in message draft opened for ") -> text.replace("Check-in message draft opened for ", "Uhlaka lomyalezo wokuqinisekisa ukuphepha luvulekele ").replace(". It is not sent until you send it in your messaging app.", ". Aluthunyelwa uze uluthumele kuhlelo lwakho lwemiyalezo.")
        text.startsWith("Help message draft opened for ") -> text.replace("Help message draft opened for ", "Uhlaka lomyalezo wokucela usizo luvulekele ").replace(". It is not sent until you send it in your messaging app.", ". Aluthunyelwa uze uluthumele kuhlelo lwakho lwemiyalezo.")
        text.startsWith("Check-in message prepared") -> text.replace("Check-in message prepared", "Umyalezo wokuqinisekisa ukuphepha ulungisiwe").replace(" · not sent", " · awuthunyelwanga")
        text.startsWith("Help message prepared") -> text.replace("Help message prepared", "Umyalezo wokucela usizo ulungisiwe").replace(" · not sent", " · awuthunyelwanga")
        text.startsWith("CHECK IN saved locally") -> text.replace("CHECK IN saved locally. Firebase will sync it when you reconnect; nobody was notified.", "Ukuqinisekisa ukuphepha kulondolozwe kudivayisi. I-Firebase izokuvumelanisa uma uxhuma futhi; akekho owazisiwe.")
        text.startsWith("ALERT saved locally") -> text.replace("ALERT saved locally. Firebase will sync it when you reconnect; nobody was notified.", "Isexwayiso silondolozwe kudivayisi. I-Firebase izosivumelanisa uma uxhuma futhi; akekho owazisiwe.")
        text.startsWith("SOS saved locally") -> text.replace("SOS saved locally. Firebase will sync it when you reconnect; nobody was notified.", "I-SOS ilondolozwe kudivayisi. I-Firebase izoyivumelanisa uma uxhuma futhi; akekho owazisiwe.")
        text.startsWith("Check-in · saved to Firebase") -> "Ukuqinisekisa ukuphepha · kulondolozwe ku-Firebase"
        text.startsWith("ALERT · saved to Firebase") -> "ISEXWAYISO · kulondolozwe ku-Firebase"
        text.startsWith("SOS · saved to Firebase") -> "SOS · kulondolozwe ku-Firebase"
        text.startsWith("CHECK IN record synced") -> text.replace("CHECK IN record synced with Firebase. No companion notification was sent; check-in and alert delivery requires the hosted API and their consent.", "Ukuqinisekisa ukuphepha kuvumelanisiwe ne-Firebase. Akukho mngane owazisiwe; kudingeka i-API esingathwa kanye nemvume yakhe.")
        text.startsWith("ALERT record synced") -> text.replace("ALERT record synced with Firebase. No companion notification was sent; check-in and alert delivery requires the hosted API and their consent.", "Isexwayiso sivumelanisiwe ne-Firebase. Akukho mngane owazisiwe; kudingeka i-API esingathwa kanye nemvume yakhe.")
        text.startsWith("SOS record synced") -> text.replace("SOS record synced with Firebase. No companion notification was sent; check-in and alert delivery requires the hosted API and their consent.", "I-SOS ivumelanisiwe ne-Firebase. Akukho mngane owazisiwe; kudingeka i-API esingathwa kanye nemvume yakhe.")
        text.startsWith("CHECK IN saved.") -> "Ukuqinisekisa ukuphepha kulondoloziwe. Umngane ovumile wazisiwe."
        text.startsWith("ALERT saved.") -> "Isexwayiso silondoloziwe. Umngane ovumile wazisiwe."
        text.startsWith("SOS saved.") -> "I-SOS ilondoloziwe. Umngane ovumile wazisiwe."
        text.startsWith("Create a journey") -> text
        text.startsWith("Hi ") && text.contains("checking in") -> text.replace("Hi ", "Sawubona ").replace(", I’m checking in to let you know I’m okay. Please reply when you can.", ", ngiqinisekisa ukuthi ngiphephile. Ngicela uphendule uma ukwazi.")
        text.startsWith("Hi ") && text.contains("I need support") -> text.replace("Hi ", "Sawubona ").replace(", I need support. Please call me when you can.", ", ngidinga usizo. Ngicela ungishayele uma ukwazi.")
        text.startsWith("Check-in message draft opened for ") -> text.replace("Check-in message draft opened for ", "Uhlaka lomyalezo wokuqinisekisa ukuphepha luvulekele ").replace(". It is not sent until you send it in your messaging app.", ". Aluthunyelwa uze uluthumele kuhlelo lwakho lwemiyalezo.")
        text.startsWith("Help message draft opened for ") -> text.replace("Help message draft opened for ", "Uhlaka lomyalezo wokucela usizo luvulekele ").replace(". It is not sent until you send it in your messaging app.", ". Aluthunyelwa uze uluthumele kuhlelo lwakho lwemiyalezo.")
        text.startsWith("Invitation created. Share this one-time code with ") -> text.replace("Invitation created. Share this one-time code with ", "Isimemo sidaliwe. Yabelana ngale khodi yesikhathi esisodwa no").replace(" privately. It expires in ", " ngasese. Iphelelwa yisikhathi emahoreni angu-").replace(" hours; the app sent no message.", "; uhlelo aluthumelanga myalezo.")
        text.startsWith("Check-in · saved to Firebase") -> "Ukuqinisekisa ukuphepha · kulondolozwe ku-Firebase"
        text.startsWith("ALERT · saved to Firebase") -> "ISEXWAYISO · kulondolozwe ku-Firebase"
        text.startsWith("SOS · saved to Firebase") -> "SOS · kulondolozwe ku-Firebase"
        text.startsWith("Check-in saved.") -> text.replace("Check-in saved. A consenting companion was notified.", "Ukuqinisekisa ukuphepha kulondoloziwe. Umngane ovumile wazisiwe.").replace("Check-in record synced with Firebase. No companion notification was sent; check-in and alert delivery requires the hosted API and their consent.", "Ukuqinisekisa ukuphepha kuvumelanisiwe ne-Firebase. Akukho mngane owazisiwe; kudingeka i-API esingathwa kanye nemvume yakhe.")
        text.startsWith("ALERT saved.") -> text.replace("ALERT saved. A consenting companion was notified.", "Isexwayiso silondoloziwe. Umngane ovumile wazisiwe.").replace("ALERT record synced with Firebase. No companion notification was sent; check-in and alert delivery requires the hosted API and their consent.", "Isexwayiso sivumelanisiwe ne-Firebase. Akukho mngane owazisiwe; kudingeka i-API esingathwa kanye nemvume yakhe.")
        text.startsWith("SOS saved.") -> text.replace("SOS saved. A consenting companion was notified.", "I-SOS ilondoloziwe. Umngane ovumile wazisiwe.").replace("SOS record synced with Firebase. No companion notification was sent; check-in and alert delivery requires the hosted API and their consent.", "I-SOS ivumelanisiwe ne-Firebase. Akukho mngane owazisiwe; kudingeka i-API esingathwa kanye nemvume yakhe.")
        text.startsWith("Journeys: ") -> text.replace("Journeys: ", "Uhambo: ").replace("allowed", "kuvunyelwe").replace("off", "kuvaliwe").replace("Check-ins: ", " · Ukuqinisekisa ukuphepha: ")
        text.contains(" · saved to Firebase") -> text.replace(" · saved to Firebase", " · kulondolozwe ku-Firebase")
        text.contains("Expected arrival in ") -> text.replace("Expected arrival in ", "Kulindeleke ukufika ngemva kwemizuzu ").replace(" min", " imizuzu").replace("No transport notes", "Ayikho imininingwane yezokuthutha")
        text.contains(" · ") && text.contains(" min") -> text.replace(" min · ", " imizuzu · ")
        text == "112 · mobile" -> "112 · umakhalekhukhwini"
        text == "0800 428 428 · 24/7" -> "0800 428 428 · amahora angu-24"
        else -> text
    }
}

/** A scoped Compose Text wrapper localizes all visible app copy without changing user-provided data. */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (androidx.compose.ui.text.TextLayoutResult) -> Unit = {},
    style: TextStyle = androidx.compose.material3.LocalTextStyle.current
) {
    MaterialText(
        text = translateUi(text, UiLanguage.current), modifier = modifier, color = color,
        fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = fontFamily,
        letterSpacing = letterSpacing, textDecoration = textDecoration, textAlign = textAlign,
        lineHeight = lineHeight, overflow = overflow, softWrap = softWrap, maxLines = maxLines,
        minLines = minLines, onTextLayout = onTextLayout, style = style
    )
}
