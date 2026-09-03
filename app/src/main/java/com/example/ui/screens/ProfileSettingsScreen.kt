package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.data.location.UserLocationInfo
import com.example.data.model.IndianLanguage
import com.example.data.model.JourneySession
import com.example.data.model.UserRole
import com.example.data.repository.TrainRouteDetails
import com.example.ui.localization.LocalizationManager
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NatureGreen
import com.example.ui.theme.RailNavy
import com.example.ui.theme.TerracottaAmber
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSandBackground
import com.example.ui.theme.WarmSurface

@Composable
fun ProfileSettingsScreen(
    role: UserRole,
    user: UserEntity?,
    language: IndianLanguage,
    isSeniorMode: Boolean,
    journeySession: JourneySession?,
    selectedRoute: TrainRouteDetails?,
    availableRoutes: List<TrainRouteDetails>,
    locationInfo: UserLocationInfo? = null,
    onSwitchRole: (UserRole) -> Unit = {},
    onLanguageChange: (IndianLanguage) -> Unit,
    onToggleSeniorMode: (Boolean) -> Unit,
    onRouteChange: (TrainRouteDetails) -> Unit,
    onReplayTutorial: () -> Unit = {},
    onSimulateStation: (String) -> Unit = {},
    onOpenDiagnostics: () -> Unit = {},
    onUpdateProfile: (name: String, phone: String, lang: IndianLanguage, isSenior: Boolean, bio: String, preferredStation: String, regularRoute: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onLogoutAndClearData: () -> Unit = {}
) {
    var showRouteMenu by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var selectedLanguageCandidate by remember { mutableStateOf(language) }

    var editName by remember(user) { mutableStateOf(user?.name ?: "") }
    var editPhone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var editBio by remember(user) { mutableStateOf("") }
    var editPreferredStation by remember(user) { mutableStateOf("SDAH") }
    var editRegularRoute by remember(user) { mutableStateOf("Sealdah - Ranaghat Local") }

    // Edit Profile Modal Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Profile & Preferences",
                    fontWeight = FontWeight.Bold,
                    color = RailNavy
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name")
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone")
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editPreferredStation,
                        onValueChange = { editPreferredStation = it },
                        label = { Text("Preferred Station (e.g. SDAH, DDJ)") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_station")
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editRegularRoute,
                        onValueChange = { editRegularRoute = it },
                        label = { Text("Regular Commute Route") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_route")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(
                            editName,
                            editPhone,
                            language,
                            isSeniorMode,
                            editBio,
                            editPreferredStation,
                            editRegularRoute
                        )
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout & Clear Data Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Logout & Clear App Data?",
                    fontWeight = FontWeight.Bold,
                    color = RailNavy
                )
            },
            text = {
                Text(
                    text = "This will clear your local sessions, saved routes, and cached data, returning you to the welcome onboarding screen.",
                    fontSize = 14.sp,
                    color = CharcoalText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogoutAndClearData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Logout & Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Language Selection Modal Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = "Select Application Language",
                    fontWeight = FontWeight.Bold,
                    color = RailNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    IndianLanguage.values().forEach { lang ->
                        val isSelected = selectedLanguageCandidate == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF8FAFC))
                                .clickable { selectedLanguageCandidate = lang }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${lang.nativeName} (${lang.englishName})",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) RailNavy else CharcoalText,
                                fontSize = 14.sp
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = RailNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onLanguageChange(selectedLanguageCandidate)
                        showLanguageDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RailNavy)
                ) {
                    Text("Apply Language")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmSandBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Profile Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    border = BorderStroke(1.dp, WarmBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(RailNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (role == UserRole.VENDOR) Icons.Default.Storefront else Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = TerracottaAmber,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.name ?: if (role == UserRole.VENDOR) "Subhash Da" else "Commuter Passenger",
                                fontSize = if (isSeniorMode) 20.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                text = "Current Role: ${if (role == UserRole.VENDOR) "Train Vendor / Hawker" else "Daily Commuter"}",
                                fontSize = 13.sp,
                                color = CharcoalTextMuted
                            )
                            Text(
                                text = "Contact / ID: ${user?.phone ?: "9876543210"}",
                                fontSize = 12.sp,
                                color = CharcoalTextMuted
                            )
                        }

                        androidx.compose.material3.OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Real GPS / Cellular Tracker Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    border = BorderStroke(1.dp, WarmBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (locationInfo?.isGpsActive == true) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                                    contentDescription = "GPS Status",
                                    tint = if (locationInfo?.isGpsActive == true) NatureGreen else TerracottaAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Real-time Location & Station Radar",
                                    fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RailNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val distKm = locationInfo?.distanceToStationKm ?: 0.0
                        val stationStatusText = when {
                            locationInfo?.isNearStation == true && locationInfo.nearestStation != null -> {
                                val meters = (distKm * 1000).toInt().coerceAtLeast(10)
                                "📍 Detected Near: ${locationInfo.nearestStation.nameEn} (${locationInfo.nearestStation.code}) • ${meters}m away"
                            }
                            locationInfo?.nearestStation != null && distKm in 0.1..30.0 -> {
                                "🏠 Off-train (~${String.format(java.util.Locale.US, "%.1f", distKm)} km from ${locationInfo.nearestStation.nameEn})"
                            }
                            else -> {
                                "🏠 Off-train (Suburban Radar Standby)"
                            }
                        }

                        Text(
                            text = stationStatusText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalText
                        )
                        Text(
                            text = "Accurate Indian Railway EMU tracker with GPS coordinate matching and offline cell tower fallback.",
                            fontSize = 11.sp,
                            color = CharcoalTextMuted
                        )
                    }
                }
            }

            // Language Selector Card with Modal Confirmation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    border = BorderStroke(1.dp, WarmBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = RailNavy,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "App Language / भाषा / ভাষা",
                                    fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                                Text(
                                    text = "Current: ${language.nativeName} (${language.englishName})",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                            }
                        }

                        Button(
                            onClick = {
                                selectedLanguageCandidate = language
                                showLanguageDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Change", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Senior Citizen Accessibility Mode
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSeniorMode) Color(0xFFFEF3C7) else WarmSurface),
                    border = BorderStroke(1.dp, if (isSeniorMode) GoldYellow else WarmBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Elderly,
                                contentDescription = "Senior Mode",
                                tint = if (isSeniorMode) GoldYellow else CharcoalTextMuted,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = LocalizationManager.getString("senior_mode", language),
                                    fontSize = if (isSeniorMode) 17.sp else 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                                Text(
                                    text = "Large high-contrast fonts, clear targets, sugar/spice indicators",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                            }
                        }

                        Switch(
                            checked = isSeniorMode,
                            onCheckedChange = { onToggleSeniorMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GoldYellow
                            ),
                            modifier = Modifier.testTag("profile_senior_switch")
                        )
                    }
                }
            }

            // Train Route Timetable Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    border = BorderStroke(1.dp, WarmBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Train Route & Timetable",
                            fontSize = if (isSeniorMode) 16.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .clickable { showRouteMenu = true }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsTransit,
                                        contentDescription = "Train",
                                        tint = RailNavy,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = selectedRoute?.trainName ?: (journeySession?.trainName ?: "Select Timetable Schedule"),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = CharcoalText
                                        )
                                        Text(
                                            text = if (selectedRoute != null) {
                                                "Train No: ${selectedRoute.trainNumber} • ${selectedRoute.stations.size} Stations"
                                            } else {
                                                "Browse suburban train schedules"
                                            },
                                            fontSize = 12.sp,
                                            color = CharcoalTextMuted
                                        )
                                    }
                                }

                                Text(
                                    text = "Browse",
                                    color = RailNavy,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            DropdownMenu(
                                expanded = showRouteMenu,
                                onDismissRequest = { showRouteMenu = false }
                            ) {
                                availableRoutes.forEach { route ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(route.trainName, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${route.trainNumber} • ${route.stations.first()} to ${route.stations.last()}",
                                                    fontSize = 11.sp,
                                                    color = CharcoalTextMuted
                                                )
                                            }
                                        },
                                        onClick = {
                                            onRouteChange(route)
                                            showRouteMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Location & Train Diagnostics Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenDiagnostics() }
                        .testTag("profile_diagnostics_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    border = BorderStroke(1.dp, WarmBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "Diagnostics",
                                tint = RailNavy,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Location & Station Diagnostics",
                                    fontSize = if (isSeniorMode) 17.sp else 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                                Text(
                                    text = "Live GPS accuracy, station geofence, filtered departed trains",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                            }
                        }

                        Text(
                            text = "Inspect ➔",
                            color = RailNavy,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Help & Tutorial Replay
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onReplayTutorial() }
                        .testTag("profile_how_it_works_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    border = BorderStroke(1.dp, WarmBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Help",
                                tint = RailNavy,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "How RailSaathi works",
                                    fontSize = if (isSeniorMode) 17.sp else 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                                Text(
                                    text = "View 2-screen guide and quick instructions",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                            }
                        }

                        Text(
                            text = "View ➔",
                            color = RailNavy,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Railway Rules & Safety Policy
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    border = BorderStroke(1.dp, WarmBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Safety",
                                tint = NatureGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Suburban Railway Rules & Ethics",
                                fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• Divyangjan & Ladies coaches have strict priority reservations.\n" +
                                    "• Hawker collision guard prevents market saturation and ensures fair daily earnings for all local train vendors.\n" +
                                    "• Transparent prices with zero surge charges and verified QR payments.",
                            fontSize = 12.sp,
                            color = CharcoalTextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Logout & Clear Data Action Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showLogoutConfirmDialog = true }
                        .testTag("logout_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Logout & Reset App Data",
                                fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = "Clear session tokens, database cache, and restart onboarding",
                                fontSize = 11.sp,
                                color = CharcoalTextMuted
                            )
                        }

                        Text(
                            text = "Reset ➔",
                            color = Color(0xFFDC2626),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
