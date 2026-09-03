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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FoodRequestEntity
import com.example.data.location.UserLocationInfo
import com.example.data.model.FoodItem
import com.example.data.model.IndianLanguage
import com.example.data.model.JourneySession
import com.example.data.model.OrderStatus
import com.example.data.model.RailwayStation
import com.example.data.model.RegionalSnacksCatalog
import com.example.data.model.RegularCommuteSchedule
import com.example.data.model.RequestStatus
import com.example.data.model.TrainCandidate
import com.example.data.model.TrainContextState
import com.example.data.repository.TrainRouteDetails
import com.example.ui.components.ContextualHintCard
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
    selectedQuantities: Map<String, Int>,
    onQuantityChange: (String, Int) -> Unit,
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
    journeyHintShown: Boolean = true,
    foodHintShown: Boolean = true,
    requestHintShown: Boolean = true,
    onDismissJourneyHint: () -> Unit = {},
    onDismissFoodHint: () -> Unit = {},
    onDismissRequestHint: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onSelectCandidate: (TrainCandidate) -> Unit,
    onClearCandidate: () -> Unit,
    onStartJourney: (TrainCandidate, String) -> Unit,
    onStartRegularCommute: () -> Unit,
    onEndJourney: () -> Unit,
    onSendHungerSignal: (FoodItem, String) -> Unit,
    onConfirmOrder: (Long) -> Unit,
    onCancelRequest: (Long) -> Unit,
    onSimulateStation: (String) -> Unit = {},
    userTravelStatus: com.example.data.location.UserTravelStatus = com.example.data.location.UserTravelStatus.STATIONARY,
    locationManagerState: com.example.data.location.LocationManagerState = com.example.data.location.LocationManagerState(),
    onToggleActiveTravel: () -> Unit = {}
) {
    var seatLocationText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val coachOptions = listOf("CAB-1", "LD-1", "VND-1", "GS-1", "GS-2", "GS-3", "VND-2", "LD-2", "CAB-2")

    val filteredSnacks = remember(selectedFilter) {
        RegionalSnacksCatalog.items.filter { item ->
            when (selectedFilter) {
                "Veg" -> item.isVeg
                "Jain" -> item.isJain
                "Senior" -> item.isSeniorFriendly
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

                if (!journeyHintShown) {
                    item {
                        ContextualHintCard(
                            title = "Start here",
                            description = "Select your train to begin your journey.",
                            onDismiss = onDismissJourneyHint,
                            testTag = "hint_start_journey"
                        )
                    }
                }

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
                                            "🏠 No Active Journey"
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
                                        text = "Standby Radar",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CharcoalTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (locationInfo.isNearStation && locationInfo.nearestStation != null) {
                                    "You are near ${locationInfo.nearestStation.nameEn}. Select your train below when ready to board."
                                } else {
                                    "Start a journey to track your train and signal snacks to vendors onboard."
                                },
                                fontSize = if (isSeniorMode) 14.sp else 12.sp,
                                color = CharcoalTextMuted
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Battery-saving GPS policy status & trigger toggle
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (locationManagerState.isTrackingGps) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (locationManagerState.isTrackingGps) Color(0xFFBBF7D0) else Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (locationManagerState.serviceState) {
                                                            com.example.data.location.LocationServiceState.ACTIVE -> NatureGreen
                                                            com.example.data.location.LocationServiceState.PAUSED_BACKGROUND -> Color(0xFFF59E0B)
                                                            else -> Color(0xFF94A3B8)
                                                        }
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (userTravelStatus == com.example.data.location.UserTravelStatus.ACTIVE_TRAVEL) {
                                                    "Active Travel Mode • GPS Tracking"
                                                } else {
                                                    "Stationary Mode • GPS Paused"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalText
                                            )
                                        }
                                        Text(
                                            text = locationManagerState.statusMessage,
                                            fontSize = 11.sp,
                                            color = CharcoalTextMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    TextButton(
                                        onClick = onToggleActiveTravel,
                                        modifier = Modifier.testTag("toggle_active_travel_mode_btn")
                                    ) {
                                        Text(
                                            text = if (userTravelStatus == com.example.data.location.UserTravelStatus.ACTIVE_TRAVEL) "Pause GPS" else "Start Travel",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (userTravelStatus == com.example.data.location.UserTravelStatus.ACTIVE_TRAVEL) AlertRed else RailNavy
                                        )
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
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Commute",
                                    fontSize = 12.sp,
                                    color = RailNavy,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${regularCommute.usualTrainName} (${regularCommute.usualTrainNumber})",
                                    fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                                Text(
                                    text = "${regularCommute.originStationName} ➔ ${regularCommute.destStationName} • ${regularCommute.usualDepartureTime} • Coach ${regularCommute.usualCoach}",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                            }

                            Button(
                                onClick = onStartRegularCommute,
                                colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("start_daily_commute_btn")
                            ) {
                                Text("Start", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Candidate Train Preview (if user tapped a candidate or searched)
                if (selectedCandidate != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmSurface),
                            border = BorderStroke(2.dp, RailNavy)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Selected Candidate Train",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RailNavy
                                    )
                                    IconButton(onClick = onClearCandidate) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = CharcoalTextMuted)
                                    }
                                }

                                Text(
                                    text = "${selectedCandidate.trainName} (${selectedCandidate.trainNumber})",
                                    fontSize = if (isSeniorMode) 18.sp else 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                                Text(
                                    text = "Dep: ${selectedCandidate.departureTime} • ${selectedCandidate.platform} • ${selectedCandidate.originStationName} ➔ ${selectedCandidate.destStationName}",
                                    fontSize = 13.sp,
                                    color = CharcoalTextMuted
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onClearCandidate,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Cancel")
                                    }

                                    Button(
                                        onClick = { onStartJourney(selectedCandidate, selectedCoach) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("confirm_board_train_btn"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RailNavy)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Board Train", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Station Departures / Radar List
                item {
                    Column {
                        Text(
                            text = if (nearbyStation != null) "Departures from ${nearbyStation.nameEn}" else "Nearby Departure Radar",
                            fontSize = if (isSeniorMode) 18.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (stationCandidates.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = WarmSurface),
                                border = BorderStroke(1.dp, WarmBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("No immediate departures detected at current position.", fontSize = 13.sp, color = CharcoalTextMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Use Search or click a simulation station above to test.", fontSize = 12.sp, color = RailNavy)
                                }
                            }
                        }
                    }
                }

                items(stationCandidates) { candidate ->
                    CandidateTrainCard(
                        candidate = candidate,
                        isSeniorMode = isSeniorMode,
                        onSelect = { onSelectCandidate(candidate) }
                    )
                }

                // Timetable Search Bar
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Search All Suburban Locals",
                            fontSize = if (isSeniorMode) 17.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    text = "Search train number, station or name...",
                                    color = CharcoalTextMuted,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = RailNavy
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = CharcoalTextMuted
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    onSearchQueryChange(searchQuery)
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CharcoalText,
                                unfocusedTextColor = CharcoalText,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                cursorColor = RailNavy,
                                focusedBorderColor = RailNavy,
                                unfocusedBorderColor = WarmBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("timetable_search_input")
                        )

                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            if (searchedTrains.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Search Results (${searchedTrains.size} trains)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RailNavy
                                    )
                                    TextButton(onClick = { onSearchQueryChange("") }) {
                                        Text("Clear", fontSize = 12.sp, color = CharcoalTextMuted)
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                                    border = BorderStroke(1.dp, WarmBorder)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "No trains found for \"$searchQuery\"",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CharcoalText
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Try searching by station name (e.g. Sealdah, Howrah, Bandel, Naihati, Dadar) or 5-digit train number (e.g. 31811, 37211).",
                                            fontSize = 12.sp,
                                            color = CharcoalTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(searchedTrains) { candidate ->
                    CandidateTrainCard(
                        candidate = candidate,
                        isSeniorMode = isSeniorMode,
                        onSelect = { onSelectCandidate(candidate) }
                    )
                }

            } else {
                // =========================================================================
                // CASE 2: ACTIVE JOURNEY IN PROGRESS
                // =========================================================================

                // Live Journey Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = RailNavy),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                        text = "LIVE JOURNEY TRACKING",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Button(
                                    onClick = onEndJourney,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("End", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${journeySession.trainName} (${journeySession.trainNumber})",
                                color = Color.White,
                                fontSize = if (isSeniorMode) 20.sp else 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${journeySession.originStation} ➔ ${journeySession.destinationStation}",
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp
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

                // Active Requests Section (including Price Confirmations)
                if (activeRequests.isNotEmpty()) {
                    if (!requestHintShown) {
                        item {
                            ContextualHintCard(
                                title = "Request from a vendor",
                                description = "The vendor will confirm the price before you pay.",
                                onDismiss = onDismissRequestHint,
                                testTag = "hint_request_vendor"
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Your Orders & Price Offers",
                            fontSize = if (isSeniorMode) 18.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RailNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(activeRequests) { request ->
                        TravelerRequestItemCard(
                            request = request,
                            language = language,
                            isSeniorMode = isSeniorMode,
                            onConfirmPrice = { onConfirmOrder(request.id) },
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

                if (!foodHintShown) {
                    item {
                        ContextualHintCard(
                            title = "Choose what you want",
                            description = "Select an item and adjust the quantity.",
                            onDismiss = onDismissFoodHint,
                            testTag = "hint_choose_food"
                        )
                    }
                }

                // Quick Suggested Rail Favorites Carousel
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Train Favorites • Real-Time Suggestions",
                                fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RailNavy
                            )
                            Text(
                                text = "1-Tap Signal",
                                fontSize = 11.sp,
                                color = TerracottaAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RegionalSnacksCatalog.items.take(4).forEach { fav ->
                                Card(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSendHungerSignal(fav, seatLocationText) },
                                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                                    border = BorderStroke(1.dp, WarmBorder)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(80.dp)
                                                .background(Color(0xFFFEF3C7))
                                        ) {
                                            if (!fav.imageUrl.isNullOrEmpty()) {
                                                AsyncImage(
                                                    model = fav.imageUrl,
                                                    contentDescription = fav.nameEn,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Text(
                                                    text = fav.emoji,
                                                    fontSize = 32.sp,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = fav.nameEn,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "₹${fav.typicalPriceInr}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TerracottaAmber
                                                )
                                                Text(
                                                    text = "Signal ➔",
                                                    fontSize = 10.sp,
                                                    color = RailNavy,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Dietary & Price Filter Chips
                item {
                    Column {
                        Text(
                            text = "Full Snack Menu",
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
                                "Senior" to LocalizationManager.getString("filter_senior_soft", language)
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

                // Food Items List with Stepper & Dynamic Price Note
                items(filteredSnacks) { snack ->
                    val quantity = selectedQuantities[snack.id] ?: 1
                    SnackFoodItemCard(
                        item = snack,
                        quantity = quantity,
                        language = language,
                        isSeniorMode = isSeniorMode,
                        selectedCoach = selectedCoach,
                        onQuantityIncrement = { onQuantityChange(snack.id, 1) },
                        onQuantityDecrement = { onQuantityChange(snack.id, -1) },
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
fun CandidateTrainCard(
    candidate: TrainCandidate,
    isSeniorMode: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
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
                        text = "${candidate.trainName} (${candidate.trainNumber})",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSeniorMode) 16.sp else 14.sp,
                        color = CharcoalText
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${candidate.originStationName} ➔ ${candidate.destStationName}",
                    fontSize = 12.sp,
                    color = CharcoalTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(candidate.platform, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RailNavy)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Upcoming", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NatureGreen)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = candidate.departureTime,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isSeniorMode) 17.sp else 15.sp,
                    color = RailNavy
                )
                Text(
                    text = "Tap to Board ➔",
                    fontSize = 11.sp,
                    color = TerracottaAmber,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SnackFoodItemCard(
    item: FoodItem,
    quantity: Int,
    language: IndianLanguage,
    isSeniorMode: Boolean,
    selectedCoach: String,
    onQuantityIncrement: () -> Unit,
    onQuantityDecrement: () -> Unit,
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = WarmSurface),
        border = BorderStroke(1.dp, WarmBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Real Food Image with fallback emoji
                Box(
                    modifier = Modifier
                        .size(if (isSeniorMode) 64.dp else 56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.nameEn,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = item.emoji,
                            fontSize = if (isSeniorMode) 28.sp else 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizedName,
                            fontSize = if (isSeniorMode) 17.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Typical ~₹${item.typicalPriceInr}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaAmber
                        )
                    }

                    Text(
                        text = item.description,
                        fontSize = if (isSeniorMode) 13.sp else 11.sp,
                        color = CharcoalTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Dietary Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.dietaryTags.take(2).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stepper and Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onQuantityDecrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "$quantity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CharcoalText,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = onQuantityIncrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }

                Button(
                    onClick = onOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(if (isSeniorMode) 44.dp else 38.dp)
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
}

@Composable
fun TravelerRequestItemCard(
    request: FoodRequestEntity,
    language: IndianLanguage,
    isSeniorMode: Boolean,
    onConfirmPrice: () -> Unit,
    onCancel: () -> Unit
) {
    val isPriceConfirmed = request.status == OrderStatus.PRICE_CONFIRMED.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("request_item_${request.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (request.status) {
                OrderStatus.PRICE_CONFIRMED.name -> Color(0xFFEFF6FF)
                OrderStatus.CUSTOMER_CONFIRMED.name -> Color(0xFFFEF3C7)
                OrderStatus.COMPLETED.name -> NatureGreenLight
                else -> Color(0xFFF8FAFC)
            }
        ),
        border = BorderStroke(
            if (isPriceConfirmed) 2.dp else 1.dp,
            if (isPriceConfirmed) RailNavy else WarmBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (request.status) {
                            OrderStatus.COMPLETED.name -> Icons.Default.CheckCircle
                            OrderStatus.CUSTOMER_CONFIRMED.name -> Icons.Default.DirectionsTransit
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = "Status",
                        tint = when (request.status) {
                            OrderStatus.COMPLETED.name -> NatureGreen
                            OrderStatus.CUSTOMER_CONFIRMED.name -> TerracottaAmber
                            else -> RailNavy
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${request.foodItemName} × ${request.quantity}",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSeniorMode) 16.sp else 14.sp,
                        color = CharcoalText
                    )
                }

                if (request.status != OrderStatus.COMPLETED.name && request.status != OrderStatus.CUSTOMER_CONFIRMED.name) {
                    IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = AlertRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Price or Status Details
            if (isPriceConfirmed) {
                val unitPrice = request.offeredUnitPrice ?: 15
                val totalPrice = request.calculatedTotalPrice ?: (request.quantity * unitPrice)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDBEAFE))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Vendor Offered: ₹$unitPrice / item",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RailNavy
                        )
                        Text(
                            text = "Total: ₹$totalPrice (${request.quantity} items)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                    }

                    Button(
                        onClick = onConfirmPrice,
                        colors = ButtonDefaults.buttonColors(containerColor = NatureGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_price_order_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirm", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                Text(
                    text = when (request.status) {
                        OrderStatus.CUSTOMER_CONFIRMED.name -> "Order Confirmed. Vendor approaching Coach ${request.coachNumber}."
                        OrderStatus.COMPLETED.name -> "Delivered & Settled in Cash"
                        else -> "Broadcasting request to station & coach vendors..."
                    },
                    fontSize = 12.sp,
                    color = CharcoalTextMuted
                )
            }
        }
    }
}
