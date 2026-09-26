package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.IndianLanguage
import com.example.ui.localization.LocalizationManager

enum class UserGuideAudience {
    PUBLIC_COMMUTER,
    SUBURBAN_VENDOR
}

data class GuideStepItem(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val detailInstructions: List<String>,
    val proTip: String,
    val icon: ImageVector,
    val iconTint: Color,
    val badgeBg: Color
)

data class GuideFaqItem(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGuidanceModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    initialAudience: UserGuideAudience = UserGuideAudience.PUBLIC_COMMUTER,
    language: IndianLanguage = IndianLanguage.ENGLISH
) {
    if (!isOpen) return

    var currentAudience by remember { mutableStateOf(initialAudience) }
    var expandedStepIndex by remember { mutableStateOf<Int?>(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(2.dp, Color(0xFF10380B)), RoundedCornerShape(24.dp)),
            color = Color(0xFFFEFDE6) // Parchment
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCE519)) // Marigold
                                .border(BorderStroke(1.5.dp, Color(0xFF10380B)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "User Guidance",
                                tint = Color(0xFF10380B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "RailSathi User Guide",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10380B)
                            )
                            Text(
                                text = "Interactive Handbook for Public & Vendors",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF555555)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8ECE2))
                            .testTag("close_guide_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF10380B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audience Selector Tabs (Public vs Vendor)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE2E9DC))
                        .padding(4.dp)
                ) {
                    // Commuters & Public Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (currentAudience == UserGuideAudience.PUBLIC_COMMUTER) Color(0xFF10380B) else Color.Transparent
                            )
                            .clickable {
                                currentAudience = UserGuideAudience.PUBLIC_COMMUTER
                                expandedStepIndex = 0
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = "Public",
                                tint = if (currentAudience == UserGuideAudience.PUBLIC_COMMUTER) Color(0xFFFCE519) else Color(0xFF10380B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "For Public / Commuters",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentAudience == UserGuideAudience.PUBLIC_COMMUTER) Color.White else Color(0xFF10380B)
                            )
                        }
                    }

                    // Vendors Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (currentAudience == UserGuideAudience.SUBURBAN_VENDOR) Color(0xFF10380B) else Color.Transparent
                            )
                            .clickable {
                                currentAudience = UserGuideAudience.SUBURBAN_VENDOR
                                expandedStepIndex = 0
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Vendors",
                                tint = if (currentAudience == UserGuideAudience.SUBURBAN_VENDOR) Color(0xFFFCE519) else Color(0xFF10380B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "For Train Vendors",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentAudience == UserGuideAudience.SUBURBAN_VENDOR) Color.White else Color(0xFF10380B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Guide Content List
                val steps = if (currentAudience == UserGuideAudience.PUBLIC_COMMUTER) {
                    commuterGuideSteps
                } else {
                    vendorGuideSteps
                }

                val faqs = if (currentAudience == UserGuideAudience.PUBLIC_COMMUTER) {
                    commuterFaqs
                } else {
                    vendorFaqs
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Quick overview card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (currentAudience == UserGuideAudience.PUBLIC_COMMUTER) Color(0xFFE8F0FE) else Color(0xFFFEF7E0)
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (currentAudience == UserGuideAudience.PUBLIC_COMMUTER) Color(0xFF1967D2) else Color(0xFFF29900)
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Tip",
                                    tint = if (currentAudience == UserGuideAudience.PUBLIC_COMMUTER) Color(0xFF1967D2) else Color(0xFFF29900),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (currentAudience == UserGuideAudience.PUBLIC_COMMUTER) {
                                        "Follow these 6 simple steps to track suburban locals, see nearby coach vendors, order chai silently, and budget daily travel."
                                    } else {
                                        "Follow these 6 vendor steps to coordinate coach boarding, prevent overlapping with other hawkers, receive live passenger orders, and track your daily sales."
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFF10380B),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    // Render Step Cards
                    items(steps) { step ->
                        val isExpanded = expandedStepIndex == step.stepNumber

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    expandedStepIndex = if (isExpanded) null else step.stepNumber
                                }
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(
                                if (isExpanded) 1.5.dp else 1.dp,
                                if (isExpanded) Color(0xFF10380B) else Color(0xFFE0E0E0)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(step.badgeBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = step.icon,
                                                contentDescription = step.title,
                                                tint = step.iconTint,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "Step ${step.stepNumber}: ${step.title}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10380B)
                                            )
                                            Text(
                                                text = step.subtitle,
                                                fontSize = 11.sp,
                                                color = Color(0xFF666666)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isExpanded) Color(0xFF10380B) else Color(0xFFE8ECE2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isExpanded) "▲" else "▼",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isExpanded) Color.White else Color(0xFF10380B)
                                        )
                                    }
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFEEEEEE))
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        step.detailInstructions.forEach { instruction ->
                                            Row(verticalAlignment = Alignment.Top) {
                                                Text(
                                                    text = "•",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10380B),
                                                    modifier = Modifier.padding(end = 6.dp)
                                                )
                                                Text(
                                                    text = instruction,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF222222),
                                                    lineHeight = 17.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Pro tip pill
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF2EE98).copy(alpha = 0.5f))
                                            .border(BorderStroke(1.dp, Color(0xFF10380B).copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "💡 Pro Tip: ",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF10380B)
                                            )
                                            Text(
                                                text = step.proTip,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF10380B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Frequently Asked Questions (FAQ)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10380B)
                        )
                    }

                    items(faqs) { faq ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE4E4E4)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Q: ${faq.question}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10380B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = faq.answer,
                                    fontSize = 12.sp,
                                    color = Color(0xFF444444),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        // Emergency & Helpline Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFCE8E6))
                                .border(BorderStroke(1.dp, Color(0xFFD93025)), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Safety",
                                    tint = Color(0xFFD93025),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Official Railway Helpline: 139",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD93025)
                                    )
                                    Text(
                                        text = "Available 24/7 for passenger grievances, medical aid, women safety, and train inquiries.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF444444)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Bottom Got It Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(1440.dp))
                        .background(Color(0xFF10380B))
                        .clickable { onDismiss() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "I Understand • Back to App",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// GUIDE STEP DATA FOR COMMUTERS (PUBLIC)
// -----------------------------------------------------------------------------
val commuterGuideSteps = listOf(
    GuideStepItem(
        stepNumber = 1,
        title = "Detect Station & Select Train",
        subtitle = "Find upcoming suburban locals automatically",
        detailInstructions = listOf(
            "Turn on Location. RailSathi automatically detects if you are near Sealdah, Howrah, Barrackpore, Dum Dum, or other suburban stations.",
            "View live departures on your Home screen under 'Nearby Departure Radar'.",
            "Tap any train to view its platform number, destination, and schedule."
        ),
        proTip = "Works offline via cell tower geofencing even inside railway overpasses and busy terminals!",
        icon = Icons.Default.NearMe,
        iconTint = Color(0xFF1967D2),
        badgeBg = Color(0xFFE8F0FE)
    ),
    GuideStepItem(
        stepNumber = 2,
        title = "Choose Your Coach Position",
        subtitle = "Select where you are seated or standing",
        detailInstructions = listOf(
            "Select your coach code (e.g., GS-1, GS-2, C1, C2, or Ladies coach LD).",
            "This highlights your position in the train layout and lets onboard vendors know where to find you.",
            "You can change your coach anytime if you switch compartments at a halt station."
        ),
        proTip = "Elderly or Divyangjan passengers can toggle 'Senior Mode' from the top bar for high-contrast big text.",
        icon = Icons.Default.DirectionsTransit,
        iconTint = Color(0xFF1E8E3E),
        badgeBg = Color(0xFFE8F8EE)
    ),
    GuideStepItem(
        stepNumber = 3,
        title = "Track Live Journey & Speedometer",
        subtitle = "Real-time halt stations countdown",
        detailInstructions = listOf(
            "Tap 'Start Journey' or 'Board Train'.",
            "Watch your live train speed (km/h), next halt countdown, and distance remaining.",
            "Receive audio alerts and visual indicators when approaching your destination station."
        ),
        proTip = "Enable background tracking so you can minimize the app while continuing to listen to station chimes.",
        icon = Icons.Default.Speed,
        iconTint = Color(0xFFF29900),
        badgeBg = Color(0xFFFEF7E0)
    ),
    GuideStepItem(
        stepNumber = 4,
        title = "Send Silent Hunger Signal",
        subtitle = "Get fresh Chai, Jhalmuri, or Water delivered",
        detailInstructions = listOf(
            "In your active journey, scroll to 'Snacks & Tea Menu'.",
            "Tap '+' on items you want (e.g., Masala Chai ₹10, Kolkata Jhalmuri ₹20, Rail Neer ₹15).",
            "Tap 'Signal Hunger / Request Items'. Onboard licensed vendors in your train receive your coach notification and deliver to your seat!"
        ),
        proTip = "Zero surge pricing! All items are sold at fixed railway prices with cash or UPI on delivery.",
        icon = Icons.Default.Fastfood,
        iconTint = Color(0xFF8430CE),
        badgeBg = Color(0xFFF3E8FD)
    ),
    GuideStepItem(
        stepNumber = 5,
        title = "Track Daily Commute Budget",
        subtitle = "Manage suburban passes and daily tickets",
        detailInstructions = listOf(
            "Open the 'Budget' tab to log your daily suburban train ticket or monthly pass renewal.",
            "Keep an eye on monthly travel expenses and food purchases.",
            "Receive alerts before your monthly suburban railway pass expires."
        ),
        proTip = "Log your monthly season ticket once; RailSathi amortizes your daily commute cost automatically.",
        icon = Icons.Default.Paid,
        iconTint = Color(0xFF5F6368),
        badgeBg = Color(0xFFE8EAED)
    ),
    GuideStepItem(
        stepNumber = 6,
        title = "Rail Madad & Emergency SOS",
        subtitle = "Quick access to railway assistance",
        detailInstructions = listOf(
            "Tap 'Rail Madad' under More Offerings to access direct emergency dials.",
            "Direct hotline to 139 (Rail Madad) and 182 / 112 (Railway Protection Force).",
            "Report lost items, harassment, or medical emergencies immediately to railway authorities."
        ),
        proTip = "Keep your train number and coach code handy when speaking with the RPF helpline.",
        icon = Icons.Default.Handshake,
        iconTint = Color(0xFFD93025),
        badgeBg = Color(0xFFFCE8E6)
    )
)

// -----------------------------------------------------------------------------
// GUIDE STEP DATA FOR SUBURBAN VENDORS
// -----------------------------------------------------------------------------
val vendorGuideSteps = listOf(
    GuideStepItem(
        stepNumber = 1,
        title = "Setup Vendor Profile & Menu",
        subtitle = "Verify your badge and speciality item",
        detailInstructions = listOf(
            "Switch to Vendor role from the top bar or profile tab.",
            "Confirm your Station Badge (e.g. ER-SDAH-104), item name (e.g. Kolkata Jhalmuri or Masala Chai), and base station.",
            "Your profile badge creates trust and lets commuters verify authorized hawkers."
        ),
        proTip = "Display your verified badge so passengers can order with confidence.",
        icon = Icons.Default.Storefront,
        iconTint = Color(0xFF1967D2),
        badgeBg = Color(0xFFE8F0FE)
    ),
    GuideStepItem(
        stepNumber = 2,
        title = "Start Shift & Pick Starting Coach",
        subtitle = "Broadcast your live presence onboard",
        detailInstructions = listOf(
            "When boarding a suburban local, select your train number (e.g. 31821 Sealdah - Krishnanagar Local).",
            "Select your starting coach compartment (e.g. VND-1 or GS-1).",
            "Tap 'Start Shift'. Your live dot immediately appears on the commuter's Coach Radar map."
        ),
        proTip = "Remember to end your shift when you alight at your return station to save battery.",
        icon = Icons.Default.DirectionsTransit,
        iconTint = Color(0xFF1E8E3E),
        badgeBg = Color(0xFFE8F8EE)
    ),
    GuideStepItem(
        stepNumber = 3,
        title = "Anti-Crowding Collision Guard",
        subtitle = "Prevent vendor conflicts in the same coach",
        detailInstructions = listOf(
            "If another vendor selling the same item is already active in Coach GS-2, the radar shows an amber warning.",
            "The app suggests empty coaches (e.g., 'Move to GS-4: 5 hungry passengers waiting!').",
            "This prevents hawkers from fighting over the same compartment and balances earnings."
        ),
        proTip = "Spread out across the train rakes for higher daily earnings and less competition.",
        icon = Icons.Default.WarningAmber,
        iconTint = Color(0xFFF29900),
        badgeBg = Color(0xFFFEF7E0)
    ),
    GuideStepItem(
        stepNumber = 4,
        title = "Receive Live Hunger Demands",
        subtitle = "View passenger orders coach-by-coach",
        detailInstructions = listOf(
            "Incoming passenger requests appear with Coach number, item requested, and time elapsed.",
            "Tap 'Accept' to let the passenger know their hot tea or snack is on the way.",
            "Walk down the platform or train corridor directly to their coach."
        ),
        proTip = "Accept requests quickly — passengers appreciate hot chai delivered before their halt station!",
        icon = Icons.Default.Campaign,
        iconTint = Color(0xFF8430CE),
        badgeBg = Color(0xFFF3E8FD)
    ),
    GuideStepItem(
        stepNumber = 5,
        title = "Deliver & Collect Payment",
        subtitle = "Cash or UPI QR code payment",
        detailInstructions = listOf(
            "Hand over the snack or drink to the passenger at their seat.",
            "Collect cash or show your UPI QR code.",
            "Tap 'Delivered & Collect' in the app to mark the order complete and log the revenue."
        ),
        proTip = "For walk-up passengers on platforms, use the 'Quick Sale' button to record cash sales in one second.",
        icon = Icons.Default.QrCode,
        iconTint = Color(0xFF5F6368),
        badgeBg = Color(0xFFE8EAED)
    ),
    GuideStepItem(
        stepNumber = 6,
        title = "Daily Ledger & Turnover Tracker",
        subtitle = "Track your daily profit and total sales",
        detailInstructions = listOf(
            "Review 'Today's Earnings' and 'Items Sold' directly on your Vendor Dashboard.",
            "Check which trains and coaches gave you the best turnover.",
            "Review your weekly sales history to plan your inventory of tea leaves, puffed rice, or snacks."
        ),
        proTip = "Keep track of daily ingredient expenses in the Ledger to calculate true net daily profit.",
        icon = Icons.Default.Paid,
        iconTint = Color(0xFF137333),
        badgeBg = Color(0xFFE6F4EA)
    )
)

// -----------------------------------------------------------------------------
// FAQS
// -----------------------------------------------------------------------------
val commuterFaqs = listOf(
    GuideFaqItem(
        question = "Can I use RailSathi without an active 4G/5G connection?",
        answer = "Yes! RailSathi uses low-power GPS and offline suburban station coordinates. Station approach alarms and coach radar work smoothly even in dead network zones."
    ),
    GuideFaqItem(
        question = "How do I pay for Chai or Snacks ordered through the app?",
        answer = "When the vendor reaches your coach with your order, you can pay directly with cash or scan their UPI QR code. No online pre-payments or surprise platform fees."
    ),
    GuideFaqItem(
        question = "What if my train is diverted or running late?",
        answer = "RailSathi's GPS location tracker tracks the train's actual physical motion and recalculates estimated arrival times dynamically based on current speed."
    )
)

val vendorFaqs = listOf(
    GuideFaqItem(
        question = "What happens if two vendors board the same coach?",
        answer = "RailSathi's Anti-Crowding Radar displays an alert on both vendors' screens, showing which nearby coaches have passenger demand with no active vendor."
    ),
    GuideFaqItem(
        question = "Do I have to pay commission on sales made through RailSathi?",
        answer = "No. RailSathi is a free utility platform designed to empower suburban railway passengers and local licensed hawkers with fair, transparent coordination."
    ),
    GuideFaqItem(
        question = "How do I record sales from passengers who don't have the app?",
        answer = "Use the 'Quick Manual Sale' button on your vendor dashboard to log any platform or walk-up cash sale in 1 tap, keeping your daily earnings record complete."
    )
)
