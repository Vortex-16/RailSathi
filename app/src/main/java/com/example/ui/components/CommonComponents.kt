package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.UserLocationInfo
import com.example.data.model.IndianLanguage
import com.example.data.model.JourneySession
import com.example.data.model.UserRole
import com.example.data.repository.CollisionCheckResult
import com.example.data.repository.TrainRouteDetails
import com.example.ui.localization.LocalizationManager
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.ForestInk
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.Marigold
import com.example.ui.theme.NatureGreen
import com.example.ui.theme.NatureGreenLight
import com.example.ui.theme.Parchment
import com.example.ui.theme.RailNavy
import com.example.ui.theme.SageWash
import com.example.ui.theme.SunlitCream
import com.example.ui.theme.TerracottaAmber
import com.example.ui.theme.VividFern
import com.example.ui.theme.WarmSandBackground
import com.example.ui.theme.WarmSurface
import com.example.ui.viewmodel.AppNavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RailAppTopBar(
    role: UserRole,
    language: IndianLanguage,
    isSeniorMode: Boolean,
    journeySession: JourneySession?,
    route: TrainRouteDetails?,
    locationInfo: UserLocationInfo,
    confidenceScore: Int = 100,
    etaSeconds: Int = 45,
    onLanguageChange: (IndianLanguage) -> Unit,
    onToggleSeniorMode: (Boolean) -> Unit,
    onEndJourney: () -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenHelp: () -> Unit = {}
) {
    var showLangMenu by remember { mutableStateOf(false) }

    Surface(
        color = SunlitCream,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(BorderStroke(1.5.dp, ForestInk))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Language Switcher circular pill (A/अ)
                Box {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SageWash)
                            .border(BorderStroke(1.2.dp, ForestInk), CircleShape)
                            .clickable { showLangMenu = true }
                            .padding(8.dp)
                            .testTag("lang_select_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "A/अ",
                                color = ForestInk,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false }
                    ) {
                        IndianLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${lang.nativeName} (${lang.englishName})",
                                        fontWeight = if (lang == language) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onLanguageChange(lang)
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }

                // Center: RailSathi Logo & Suburban Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = LocalizationManager.getString("app_title", language),
                        color = ForestInk,
                        fontSize = if (isSeniorMode) 22.sp else 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = when (role) {
                            UserRole.VENDOR -> LocalizationManager.getString("role_vendor", language)
                            UserRole.TRAVELER -> LocalizationManager.getString("role_passenger", language)
                            UserRole.GUEST -> LocalizationManager.getString("role_guest", language)
                        },
                        color = CharcoalTextMuted,
                        fontSize = if (isSeniorMode) 12.sp else 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Right Actions: Senior Mode + Notifications Bell + Guide Help
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Senior mode toggle button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(1440.dp))
                            .background(if (isSeniorMode) Marigold else SageWash)
                            .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                            .clickable { onToggleSeniorMode(!isSeniorMode) }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSeniorMode) "Sr. ON" else "Sr.",
                            color = ForestInk,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Notification Bell with unread indicator
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SageWash)
                            .border(BorderStroke(1.2.dp, ForestInk), CircleShape)
                            .clickable { onOpenNotifications() }
                            .testTag("top_bell_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alerts",
                            tint = ForestInk,
                            modifier = Modifier.size(17.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD93025))
                                .align(Alignment.TopEnd)
                        )
                    }

                    // User Handbook / Guide button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Marigold)
                            .border(BorderStroke(1.2.dp, ForestInk), CircleShape)
                            .clickable { onOpenHelp() }
                            .testTag("top_help_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Guide",
                            tint = ForestInk,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Train / Journey Status Bar: Displays active tracking ONLY if user is in an active journey!
            if (journeySession != null && route != null) {
                val currentStation = route.stations.getOrElse(route.currentStationIndex) { journeySession.currentStation }
                val nextStation = route.stations.getOrElse((route.currentStationIndex + 1) % route.stations.size) { "Terminus" }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(1440.dp))
                        .background(Parchment)
                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(VividFern)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${journeySession.trainName} • $currentStation",
                            color = ForestInk,
                            fontSize = if (isSeniorMode) 13.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(1440.dp))
                                .background(Marigold)
                                .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Next: $nextStation (${etaSeconds}s)",
                                color = ForestInk,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            } else {
                // No active journey indicator (Honest Standby)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(1440.dp))
                        .background(SageWash)
                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = "Location Status",
                            tint = ForestInk,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (locationInfo.isNearStation && locationInfo.nearestStation != null) {
                                "📍 Near ${locationInfo.nearestStation.nameEn} • Standby"
                            } else {
                                "🏠 Off-track / Standby • No active train"
                            },
                            color = ForestInk,
                            fontSize = if (isSeniorMode) 13.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Not Tracking",
                        color = CharcoalTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RailBottomNavBar(
    activeTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    language: IndianLanguage,
    role: UserRole,
    isSeniorMode: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, bottom = 12.dp, top = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Pill Container with Translucent Liquid Frosted Glass Styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color(0x38000000),
                    ambientColor = Color(0x1A000000)
                )
                .clip(RoundedCornerShape(36.dp))
                .background(
                    // Translucent Frosted Glass - content underneath is visible through the glass!
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xC8FFFFFF), // 78% translucent frosted white
                            Color(0x95FAF7EE), // 58% translucent warm tone
                            Color(0xB8FFFFFF)  // 72% translucent frosted white
                        )
                    )
                )
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color.White.copy(alpha = 0.45f),
                                ForestInk.copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0.85f)
                            )
                        )
                    ),
                    RoundedCornerShape(36.dp)
                )
        ) {
            // Liquid Specular Highlight Reflection on Top Arc
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.80f),
                                Color.White.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: HOME
                GlassyNavTabItem(
                    selected = activeTab == AppNavTab.HOME,
                    title = LocalizationManager.getString("nav_home", language),
                    iconFilled = Icons.Filled.Home,
                    iconOutlined = Icons.Outlined.Home,
                    isSeniorMode = isSeniorMode,
                    testTag = "nav_home_btn",
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(AppNavTab.HOME) }
                )

                // Tab 2: RADAR
                GlassyNavTabItem(
                    selected = activeTab == AppNavTab.COACH_RADAR,
                    title = LocalizationManager.getString("nav_coach_radar", language),
                    iconFilled = Icons.Filled.AccountTree,
                    iconOutlined = Icons.Outlined.AccountTree,
                    isSeniorMode = isSeniorMode,
                    testTag = "nav_radar_btn",
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(AppNavTab.COACH_RADAR) }
                )

                // Tab 3: BUDGET
                GlassyNavTabItem(
                    selected = activeTab == AppNavTab.BUDGET_LEDGER,
                    title = if (role == UserRole.VENDOR) "Earnings" else LocalizationManager.getString("nav_budget", language),
                    iconFilled = Icons.Filled.ReceiptLong,
                    iconOutlined = Icons.Outlined.ReceiptLong,
                    isSeniorMode = isSeniorMode,
                    testTag = "nav_budget_btn",
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(AppNavTab.BUDGET_LEDGER) }
                )

                // Tab 4: PROFILE
                GlassyNavTabItem(
                    selected = activeTab == AppNavTab.PROFILE,
                    title = LocalizationManager.getString("nav_profile", language),
                    iconFilled = Icons.Filled.AccountCircle,
                    iconOutlined = Icons.Outlined.AccountCircle,
                    isSeniorMode = isSeniorMode,
                    testTag = "nav_profile_btn",
                    modifier = Modifier.weight(1f),
                    onClick = { onTabSelected(AppNavTab.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun GlassyNavTabItem(
    selected: Boolean,
    title: String,
    iconFilled: androidx.compose.ui.graphics.vector.ImageVector,
    iconOutlined: androidx.compose.ui.graphics.vector.ImageVector,
    isSeniorMode: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            // Liquid active droplet pill with gradient glow and specular reflection
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Marigold.copy(alpha = 0.95f),
                                Color(0xFFFFD54F).copy(alpha = 0.92f),
                                Color(0xFFFFB300).copy(alpha = 0.95f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(1.2.dp, ForestInk.copy(alpha = 0.30f)),
                        RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Liquid sheen in droplet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = iconFilled,
                        contentDescription = title,
                        tint = ForestInk,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = title,
                        fontSize = if (isSeniorMode) 13.sp else 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ForestInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = iconOutlined,
                    contentDescription = title,
                    tint = ForestInk.copy(alpha = 0.85f),
                    modifier = Modifier.size(21.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    fontSize = if (isSeniorMode) 12.sp else 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestInk.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CollisionWarningDialog(
    collision: CollisionCheckResult.Conflict,
    language: IndianLanguage,
    onDismiss: () -> Unit,
    onAcceptAlternative: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = AlertRed,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = LocalizationManager.getString("collision_prevented", language),
                fontWeight = FontWeight.Bold,
                color = AlertRed
            )
        },
        text = {
            Column {
                Text(
                    text = "Coach ${collision.coachNumber} already has vendor ${collision.conflictingVendorName} selling ${collision.itemName}.",
                    fontSize = 14.sp,
                    color = CharcoalText
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = NatureGreenLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Recommended",
                            tint = NatureGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recommended: Board Coach ${collision.recommendedAlternativeCoach} (High hunger demand & no duplicate vendor)",
                            fontSize = 13.sp,
                            color = CharcoalText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAcceptAlternative(collision.recommendedAlternativeCoach) },
                colors = ButtonDefaults.buttonColors(containerColor = NatureGreen)
            ) {
                Text("Board Coach ${collision.recommendedAlternativeCoach} Instead")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CharcoalTextMuted)
            }
        }
    )
}

@Composable
fun ContextualHintCard(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "contextual_hint_card"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "💡 $title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF15803D),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("${testTag}_got_it")
            ) {
                Text(
                    text = "Got it",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun BannerNotificationToast(
    message: String?,
    isSeniorMode: Boolean
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        if (message != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalText),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert",
                            tint = TerracottaAmber,
                            modifier = Modifier.size(if (isSeniorMode) 28.dp else 22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = message,
                            color = Color.White,
                            fontSize = if (isSeniorMode) 16.sp else 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


