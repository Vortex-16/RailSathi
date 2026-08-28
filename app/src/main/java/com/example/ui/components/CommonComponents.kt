package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Fastfood
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
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NatureGreen
import com.example.ui.theme.NatureGreenLight
import com.example.ui.theme.RailNavy
import com.example.ui.theme.TerracottaAmber
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
    onSwitchRole: () -> Unit = {}
) {
    var showLangMenu by remember { mutableStateOf(false) }

    Surface(
        color = RailNavy,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSeniorMode) 44.dp else 38.dp)
                            .clip(CircleShape)
                            .background(TerracottaAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsTransit,
                            contentDescription = "Train Icon",
                            tint = Color.White,
                            modifier = Modifier.size(if (isSeniorMode) 26.dp else 22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = LocalizationManager.getString("app_title", language),
                            color = Color.White,
                            fontSize = if (isSeniorMode) 22.sp else 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (role) {
                                UserRole.VENDOR -> LocalizationManager.getString("role_vendor", language)
                                UserRole.TRAVELER -> LocalizationManager.getString("role_passenger", language)
                                UserRole.GUEST -> LocalizationManager.getString("role_guest", language)
                            },
                            color = Color(0xFFE2E8F0),
                            fontSize = if (isSeniorMode) 13.sp else 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Senior mode pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSeniorMode) GoldYellow else Color(0x33FFFFFF))
                            .clickable { onToggleSeniorMode(!isSeniorMode) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Elderly,
                                contentDescription = "Senior Mode",
                                tint = if (isSeniorMode) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSeniorMode) "Senior ON" else "Senior",
                                color = if (isSeniorMode) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Language dropdown button
                    Box {
                        IconButton(
                            onClick = { showLangMenu = true },
                            modifier = Modifier.size(38.dp).testTag("lang_select_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = Color.White
                            )
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
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x2BFFFFFF))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
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
                                .background(NatureGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${journeySession.trainName} • $currentStation",
                            color = Color.White,
                            fontSize = if (isSeniorMode) 13.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
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
                                .clip(RoundedCornerShape(6.dp))
                                .background(TerracottaAmber)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Next: $nextStation (${etaSeconds}s)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // No active journey indicator (Honest Standby)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x1AFFFFFF))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
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
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (locationInfo.isNearStation && locationInfo.nearestStation != null) {
                                "📍 Near ${locationInfo.nearestStation.nameEn} • Standby"
                            } else {
                                "🏠 Off-track / Standby • No active train"
                            },
                            color = Color(0xFFCBD5E1),
                            fontSize = if (isSeniorMode) 13.sp else 11.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Not Tracking",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
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
    isSeniorMode: Boolean
) {
    NavigationBar(
        containerColor = WarmSurface,
        contentColor = CharcoalText,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = activeTab == AppNavTab.HOME,
            onClick = { onTabSelected(AppNavTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (activeTab == AppNavTab.HOME) Icons.Filled.Fastfood else Icons.Outlined.Fastfood,
                    contentDescription = "Home"
                )
            },
            label = {
                Text(
                    text = LocalizationManager.getString("nav_home", language),
                    fontSize = if (isSeniorMode) 13.sp else 11.sp,
                    fontWeight = if (activeTab == AppNavTab.HOME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RailNavy,
                indicatorColor = Color(0xFFDBEAFE),
                selectedTextColor = RailNavy
            ),
            modifier = Modifier.testTag("nav_home_btn")
        )

        NavigationBarItem(
            selected = activeTab == AppNavTab.COACH_RADAR,
            onClick = { onTabSelected(AppNavTab.COACH_RADAR) },
            icon = {
                Icon(
                    imageVector = if (activeTab == AppNavTab.COACH_RADAR) Icons.Filled.AccountTree else Icons.Outlined.AccountTree,
                    contentDescription = "Radar"
                )
            },
            label = {
                Text(
                    text = LocalizationManager.getString("nav_coach_radar", language),
                    fontSize = if (isSeniorMode) 13.sp else 11.sp,
                    fontWeight = if (activeTab == AppNavTab.COACH_RADAR) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RailNavy,
                indicatorColor = Color(0xFFDBEAFE),
                selectedTextColor = RailNavy
            ),
            modifier = Modifier.testTag("nav_radar_btn")
        )

        NavigationBarItem(
            selected = activeTab == AppNavTab.BUDGET_LEDGER,
            onClick = { onTabSelected(AppNavTab.BUDGET_LEDGER) },
            icon = {
                Icon(
                    imageVector = if (activeTab == AppNavTab.BUDGET_LEDGER) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                    contentDescription = "Budget"
                )
            },
            label = {
                Text(
                    text = if (role == UserRole.VENDOR) "Sales & Earning" else LocalizationManager.getString("nav_budget", language),
                    fontSize = if (isSeniorMode) 13.sp else 11.sp,
                    fontWeight = if (activeTab == AppNavTab.BUDGET_LEDGER) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RailNavy,
                indicatorColor = Color(0xFFDBEAFE),
                selectedTextColor = RailNavy
            ),
            modifier = Modifier.testTag("nav_budget_btn")
        )

        NavigationBarItem(
            selected = activeTab == AppNavTab.PROFILE,
            onClick = { onTabSelected(AppNavTab.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (activeTab == AppNavTab.PROFILE) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    contentDescription = "Profile"
                )
            },
            label = {
                Text(
                    text = LocalizationManager.getString("nav_profile", language),
                    fontSize = if (isSeniorMode) 13.sp else 11.sp,
                    fontWeight = if (activeTab == AppNavTab.PROFILE) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RailNavy,
                indicatorColor = Color(0xFFDBEAFE),
                selectedTextColor = RailNavy
            ),
            modifier = Modifier.testTag("nav_profile_btn")
        )
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
