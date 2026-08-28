package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.local.FoodRequestEntity
import com.example.data.location.UserLocationInfo
import com.example.data.model.FoodItem
import com.example.data.model.IndianLanguage
import com.example.data.model.JourneySession
import com.example.data.model.RailwayStation
import com.example.data.model.RegionalSnacksCatalog
import com.example.data.model.RegularCommuteSchedule
import com.example.data.model.RequestStatus
import com.example.data.model.TrainCandidate
import com.example.data.model.TrainContextState
import com.example.data.repository.TrainRouteDetails
import com.example.ui.localization.LocalizationManager
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.NatureGreen
import com.example.ui.theme.NatureGreenLight
import com.example.ui.theme.RailNavy
import com.example.ui.theme.TerracottaAmber
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSandBackground
import com.example.ui.theme.WarmSurface

@Composable
fun TravelerHomeScreen(
    language: IndianLanguage,
    isSeniorMode: Boolean,
    selectedCoach: String,
    onCoachSelect: (String) -> Unit,
    activeRequests: List<FoodRequestEntity>,
    journeySession: JourneySession?,
    selectedRoute: TrainRouteDetails?,
    locationInfo: UserLocationInfo,
    contextState: TrainContextState,
    nearbyStation: RailwayStation?,
    stationCandidates: List<TrainCandidate>,
    selectedCandidate: TrainCandidate?,
    confidenceScore: Int,
    confidenceDescription: String,
    regularCommute: RegularCommuteSchedule,
    searchQuery: String,
    searchedTrains: List<TrainCandidate>,
    onSearchQueryChange: (String) -> Unit,
    onSelectCandidate: (TrainCandidate) -> Unit,
    onClearCandidate: () -> Unit,
    onStartJourney: (TrainCandidate, String) -> Unit,
    onStartRegularCommute: () -> Unit,
    onEndJourney: () -> Unit,
    onSendHungerSignal: (FoodItem, String) -> Unit,
    onCancelRequest: (Long) -> Unit,
    onSimulateStation: (String) -> Unit = {}
) {
    var seatLocationText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var localSelectedCoach by remember { mutableStateOf(selectedCoach) }

    val coachOptions = listOf("CAB-1", "LD-1", "VND-1", "GS-1", "GS-2", "GS-3", "VND-2", "LD-2", "CAB-2")

    val filteredSnacks = remember(selectedFilter) {
        RegionalSnacksCatalog.items.filter { item ->
            when (selectedFilter) {
                "Veg" -> item.isVeg
                "Jain" -> item.isJain
                "Senior" -> item.isSeniorFriendly
                "Under30" -> item.defaultPrice <= 30
                else -> true
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmSandBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // =========================================================================
            // CASE 1: USER IS NOT IN AN ACTIVE JOURNEY (STANDBY / OFF-TRACK / NEAR STN)
            // =========================================================================
            if (journeySession == null) {

                // Greeting & Location Awareness Card
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
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (locationInfo.isNearStation) NatureGreen else Color(0xFF94A3B8))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (locationInfo.isNearStation && locationInfo.nearestStation != null) {
                                            "📍 Near ${locationInfo.nearestStation.nameEn} Station"
                                        } else {
                                            "🏠 Off-Track / Standby"
                                        },
                                        fontSize = if (isSeniorMode) 17.sp else 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "No Train Assumed",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CharcoalTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (locationInfo.isNearStation && locationInfo.nearestStation != null) {
                                    "You are within ${(locationInfo.distanceToStationKm * 1000).toInt()}m of ${locationInfo.nearestStation.nameEn}. Select your train below when ready to board."
                                } else {
                                    "Your location is ~${String.format(java.util.Locale.US, "%.1f", locationInfo.distanceToStationKm)} km from nearest station. Please select a train or search timetable when you travel."
                                },
                                fontSize = if (isSeniorMode) 14.sp else 12.sp,
                                color = CharcoalTextMuted
                            )

                            // Fast simulation test buttons for user convenience
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Test at Station:", fontSize = 11.sp, color = CharcoalTextMuted)
                                listOf("SDAH" to "Sealdah", "BNR" to "Barasat", "CSMT" to "Mumbai CSMT").forEach { (code, name) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE2E8F0))
                                            .clickable { onSimulateStation(code) }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(name, fontSize = 11.sp, color = RailNavy, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Regular Commute Suggestion Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Commute",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RailNavy
                                )
                                Text(
                                    text = "${regularCommute.usualDepartureTime} • ${regularCommute.usualTrainName}",
                                    fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                                Text(
                                    text = "${regularCommute.originStationName} ➔ ${regularCommute.destStationName} (Coach ${regularCommute.usualCoach})",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                            }

                            Button(
                                onClick = onStartRegularCommute,
                                colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("start_regular_commute_btn")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Candidate Train Selection / Confirmation Panel
                if (selectedCandidate != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmSurface),
                            border = BorderStroke(2.dp, TerracottaAmber)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Confirm Your Journey",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaAmber
                                        )
                                        Text(
                                            text = "${selectedCandidate.trainName} (${selectedCandidate.trainNumber})",
                                            fontSize = if (isSeniorMode) 18.sp else 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalText
                                        )
                                    }

                                    IconButton(onClick = onClearCandidate) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = CharcoalTextMuted)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Route: ${selectedCandidate.originStationName} ➔ ${selectedCandidate.destStationName} • Departs ${selectedCandidate.departureTime} (${selectedCandidate.platform})",
                                    fontSize = 13.sp,
                                    color = CharcoalTextMuted
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Select Your Boarding Coach:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CharcoalText
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    coachOptions.forEach { coach ->
                                        val isSel = localSelectedCoach == coach
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) RailNavy else WarmSurface)
                                                .border(1.dp, if (isSel) RailNavy else WarmBorder, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    localSelectedCoach = coach
                                                    onCoachSelect(coach)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = coach,
                                                color = if (isSel) Color.White else CharcoalText,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { onStartJourney(selectedCandidate, localSelectedCoach) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NatureGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("confirm_start_journey_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Start Journey & Live Tracking",
                                        fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Station Departures Candidates (If near a station)
                if (locationInfo.isNearStation && stationCandidates.isNotEmpty()) {
                    item {
                        Text(
                            text = "Departures from ${locationInfo.nearestStation?.nameEn ?: "Station"}",
                            fontSize = if (isSeniorMode) 18.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = RailNavy
                        )
                    }

                    items(stationCandidates) { candidate ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCandidate(candidate) }
                                .testTag("candidate_train_${candidate.trainNumber}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmSurface),
                            border = BorderStroke(1.dp, WarmBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = candidate.trainName,
                                            fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalText
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFE2E8F0))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(candidate.trainNumber, fontSize = 11.sp, color = RailNavy, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${candidate.originStationName} ➔ ${candidate.destStationName}",
                                        fontSize = 12.sp,
                                        color = CharcoalTextMuted
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = candidate.departureTime,
                                        fontSize = if (isSeniorMode) 17.sp else 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaAmber
                                    )
                                    Text(
                                        text = candidate.platform,
                                        fontSize = 12.sp,
                                        color = CharcoalTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Timetable Search Bar
                item {
                    Text(
                        text = "Search Local Trains & Timetables",
                        fontSize = if (isSeniorMode) 17.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RailNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search by train name, number (e.g. 31821, Ranaghat, Sealdah)...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("train_search_input")
                    )
                }

                // Search Results
                if (searchedTrains.isNotEmpty()) {
                    items(searchedTrains) { candidate ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCandidate(candidate) }
                                .testTag("searched_train_${candidate.trainNumber}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmSurface),
                            border = BorderStroke(1.dp, WarmBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = candidate.trainName,
                                        fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                    Text(
                                        text = "${candidate.originStationName} ➔ ${candidate.destStationName} (${candidate.trainNumber})",
                                        fontSize = 12.sp,
                                        color = CharcoalTextMuted
                                    )
                                }

                                Button(
                                    onClick = { onSelectCandidate(candidate) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Select", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

            } else {
                // =========================================================================
                // CASE 2: ACTIVE JOURNEY IN PROGRESS (LIVE CORRIDOR TRACKING)
                // =========================================================================

                // Active Journey Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = RailNavy),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(NatureGreen)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ACTIVE JOURNEY",
                                        color = NatureGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = onEndJourney,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("end_journey_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Stop, contentDescription = "End", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("End Journey", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = journeySession.trainName,
                                color = Color.White,
                                fontSize = if (isSeniorMode) 20.sp else 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${journeySession.originStation} ➔ ${journeySession.destinationStation} (${journeySession.trainNumber})",
                                color = Color(0xFFCBD5E1),
                                fontSize = if (isSeniorMode) 14.sp else 12.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x33FFFFFF))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Coach: ${journeySession.currentCoach}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "$confidenceScore% • $confidenceDescription",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Active Requests Section
                if (activeRequests.isNotEmpty()) {
                    item {
                        Text(
                            text = "Your Active Orders & Hunger Signals",
                            fontSize = if (isSeniorMode) 18.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RailNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(activeRequests) { request ->
                        ActiveRequestCard(
                            request = request,
                            language = language,
                            isSeniorMode = isSeniorMode,
                            onCancel = { onCancelRequest(request.id) }
                        )
                    }
                }

                // Coach Selection & Seat Description Card
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
                                Text(
                                    text = LocalizationManager.getString("select_coach", language),
                                    fontSize = if (isSeniorMode) 17.sp else 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDBEAFE))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Current: $selectedCoach",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RailNavy
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Coach Chips Horizontal Scroll
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                coachOptions.forEach { coach ->
                                    val isSelected = selectedCoach == coach
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) RailNavy else WarmSurface)
                                            .border(1.dp, if (isSelected) RailNavy else WarmBorder, RoundedCornerShape(8.dp))
                                            .clickable { onCoachSelect(coach) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("coach_chip_$coach")
                                    ) {
                                        Text(
                                            text = coach,
                                            color = if (isSelected) Color.White else CharcoalText,
                                            fontSize = if (isSeniorMode) 15.sp else 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = seatLocationText,
                                onValueChange = { seatLocationText = it },
                                placeholder = { Text(LocalizationManager.getString("seat_desc", language)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("seat_location_input")
                            )
                        }
                    }
                }

                // Dietary & Price Filter Chips
                item {
                    Column {
                        Text(
                            text = "Signal Snacks to Onboard Vendors",
                            fontSize = if (isSeniorMode) 16.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "All" to LocalizationManager.getString("filter_all", language),
                                "Veg" to LocalizationManager.getString("filter_veg", language),
                                "Jain" to LocalizationManager.getString("filter_jain", language),
                                "Senior" to LocalizationManager.getString("filter_senior_soft", language),
                                "Under30" to LocalizationManager.getString("filter_under_30", language)
                            ).forEach { (key, label) ->
                                val isSelected = selectedFilter == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilter = key },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = if (isSeniorMode) 14.sp else 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RailNavy,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("filter_$key")
                                )
                            }
                        }
                    }
                }

                // Food Items List
                items(filteredSnacks) { snack ->
                    SnackFoodItemCard(
                        item = snack,
                        language = language,
                        isSeniorMode = isSeniorMode,
                        selectedCoach = selectedCoach,
                        onOrder = {
                            onSendHungerSignal(snack, seatLocationText)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SnackFoodItemCard(
    item: FoodItem,
    language: IndianLanguage,
    isSeniorMode: Boolean,
    selectedCoach: String,
    onOrder: () -> Unit
) {
    val localizedName = when (language) {
        IndianLanguage.BENGALI -> item.nameBn
        IndianLanguage.MARATHI -> item.nameMr
        IndianLanguage.HINDI -> item.nameHi
        else -> item.nameEn
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("snack_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WarmSurface),
        border = BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Snack Emoji/Avatar
            Box(
                modifier = Modifier
                    .size(if (isSeniorMode) 54.dp else 46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.emoji,
                    fontSize = if (isSeniorMode) 28.sp else 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = localizedName,
                        fontSize = if (isSeniorMode) 17.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = item.description,
                    fontSize = if (isSeniorMode) 13.sp else 11.sp,
                    color = CharcoalTextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "₹${item.defaultPrice}",
                        fontSize = if (isSeniorMode) 18.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaAmber
                    )

                    if (item.isSeniorFriendly) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("Soft/Easy", fontSize = 10.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Bold)
                        }
                    }

                    if (item.isJain) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NatureGreenLight)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("Jain", fontSize = 10.sp, color = NatureGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onOrder,
                colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(if (isSeniorMode) 46.dp else 38.dp)
                    .testTag("signal_btn_${item.id}")
            ) {
                Text(
                    text = "Signal in $selectedCoach",
                    fontSize = if (isSeniorMode) 14.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActiveRequestCard(
    request: FoodRequestEntity,
    language: IndianLanguage,
    isSeniorMode: Boolean,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (request.status) {
                RequestStatus.ASSIGNED.name -> Color(0xFFEFF6FF)
                RequestStatus.IN_TRANSIT.name -> Color(0xFFFEF3C7)
                RequestStatus.DELIVERED.name -> NatureGreenLight
                else -> Color(0xFFF1F5F9)
            }
        ),
        border = BorderStroke(1.dp, WarmBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (request.status) {
                            RequestStatus.DELIVERED.name -> Icons.Default.CheckCircle
                            RequestStatus.IN_TRANSIT.name -> Icons.Default.DirectionsTransit
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = "Status",
                        tint = when (request.status) {
                            RequestStatus.DELIVERED.name -> NatureGreen
                            RequestStatus.IN_TRANSIT.name -> TerracottaAmber
                            else -> RailNavy
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${request.foodItemName} • Coach ${request.coachNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSeniorMode) 16.sp else 14.sp,
                        color = CharcoalText
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (request.assignedVendorName != null) {
                        "Vendor: ${request.assignedVendorName} (Approaching coach)"
                    } else {
                        "Broadcasting signal to station vendors..."
                    },
                    fontSize = 12.sp,
                    color = CharcoalTextMuted
                )
            }

            if (request.status == RequestStatus.PENDING.name || request.status == RequestStatus.ASSIGNED.name) {
                IconButton(onClick = onCancel) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = AlertRed)
                }
            }
        }
    }
}
