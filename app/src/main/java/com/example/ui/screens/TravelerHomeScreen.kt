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
import com.example.ui.components.SuburbanCommuterHeroSection
import com.example.ui.components.SuburbanOfferingsGrid
import com.example.ui.components.DoYouKnowCarousel
import com.example.ui.components.UserGuidanceModal
import com.example.ui.components.UserGuideAudience
import com.example.ui.components.RailMadadDialog
import com.example.ui.components.NotificationsDialog
import com.example.ui.localization.LocalizationManager
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.theme.BotanicalStarRating
import com.example.ui.theme.ForestInk
import com.example.ui.theme.ForestPillButton
import com.example.ui.theme.Marigold
import com.example.ui.theme.MarigoldPillButton
import com.example.ui.theme.OutlinedPillButton
import com.example.ui.theme.Parchment
import com.example.ui.theme.SageWash
import com.example.ui.theme.SunlitCardShape
import com.example.ui.theme.SunlitCream
import com.example.ui.theme.SunlitPillShape
import com.example.ui.theme.SunlitStampedCard
import com.example.ui.theme.VividFern
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
    onToggleActiveTravel: () -> Unit = {},
    availableCoaches: List<String> = emptyList(),
    userName: String = "Kailash Kumar",
    allStations: List<RailwayStation> = emptyList(),
    onSelectStationCode: (String) -> Unit = {},
    betweenTrains: List<TrainCandidate> = emptyList(),
    isLoadingBetweenTrains: Boolean = false,
    onFetchTrainsBetween: (String, String) -> Unit = { _, _ -> },
    onNavigateToTab: (AppNavTab) -> Unit = {}
) {
    var seatLocationText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    var showGuidanceModal by remember { mutableStateOf(false) }
    var guidanceInitialAudience by remember { mutableStateOf(UserGuideAudience.PUBLIC_COMMUTER) }
    var showRailMadadModal by remember { mutableStateOf(false) }
    var showNotificationsModal by remember { mutableStateOf(false) }

    val coachOptions = if (availableCoaches.isNotEmpty()) {
        availableCoaches
    } else if (selectedRoute?.coachCodes?.isNotEmpty() == true) {
        selectedRoute.coachCodes
    } else if (selectedCandidate?.coachCodes?.isNotEmpty() == true) {
        selectedCandidate.coachCodes
    } else {
        listOf("CAB-1", "LD-1", "VND-1", "GS-1", "GS-2", "GS-3", "VND-2", "LD-2", "CAB-2")
    }

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
            // GREETING & QUICK ACCESS (MATCHING REFERENCE UI)
            // =========================================================================
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 2.dp)) {
                    Text(
                        text = "Hi, $userName!",
                        fontSize = if (isSeniorMode) 24.sp else 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ForestInk
                    )
                    Text(
                        text = "Where are you heading today?",
                        fontSize = if (isSeniorMode) 14.sp else 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = CharcoalTextMuted
                    )
                }
            }

            // Suburban Commuter Quick Actions: Signal Chai, Live EMU Board, Coach Crowding Radar
            item {
                SuburbanCommuterHeroSection(
                    isSeniorMode = isSeniorMode,
                    onSignalChai = {
                        val chai = RegionalSnacksCatalog.items.find { it.id == "chai_1" } ?: RegionalSnacksCatalog.items.first()
                        onSendHungerSignal(chai, seatLocationText)
                    },
                    onLiveTimetable = {
                        if (stationCandidates.isNotEmpty()) {
                            onSelectCandidate(stationCandidates.first())
                        }
                    },
                    onCoachRadar = { onNavigateToTab(AppNavTab.COACH_RADAR) }
                )
            }

            // Suburban Travel Utilities (Real functionality: Timetable, Radar, Chai, Budget, Rail Madad, Guide)
            item {
                SuburbanOfferingsGrid(
                    isSeniorMode = isSeniorMode,
                    onLiveTimetable = {
                        if (stationCandidates.isNotEmpty() && selectedCandidate == null) {
                            onSelectCandidate(stationCandidates.first())
                        }
                    },
                    onCoachPosition = { onNavigateToTab(AppNavTab.COACH_RADAR) },
                    onTrackTrain = {
                        if (stationCandidates.isNotEmpty() && selectedCandidate == null) {
                            onSelectCandidate(stationCandidates.first())
                        }
                    },
                    onOrderFood = { selectedFilter = "All" },
                    onDailyBudget = { onNavigateToTab(AppNavTab.BUDGET_LEDGER) },
                    onRailMadad = { showRailMadadModal = true },
                    onUserGuidance = {
                        guidanceInitialAudience = UserGuideAudience.PUBLIC_COMMUTER
                        showGuidanceModal = true
                    }
                )
            }

            // Do You Know? Carousel
            item {
                DoYouKnowCarousel(isSeniorMode = isSeniorMode)
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
                    SunlitStampedCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Parchment,
                        borderColor = ForestInk,
                        shadowOffset = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
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
                                            .background(if (locationInfo.isNearStation) VividFern else CharcoalTextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (locationInfo.isNearStation && locationInfo.nearestStation != null) {
                                            "📍 Near ${locationInfo.nearestStation.nameEn} Station"
                                        } else {
                                            "🏠 No Active Journey"
                                        },
                                        fontSize = if (isSeniorMode) 17.sp else 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestInk
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(1440.dp))
                                        .background(SageWash)
                                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Standby Radar",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestInk
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
                                fontWeight = FontWeight.Medium,
                                color = CharcoalTextMuted
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Battery-saving GPS policy status & trigger toggle
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(1440.dp))
                                    .background(SunlitCream)
                                    .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
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
                                                            com.example.data.location.LocationServiceState.ACTIVE -> VividFern
                                                            com.example.data.location.LocationServiceState.PAUSED_BACKGROUND -> Marigold
                                                            else -> CharcoalTextMuted
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
                                                color = ForestInk
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
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (userTravelStatus == com.example.data.location.UserTravelStatus.ACTIVE_TRAVEL) AlertRed else ForestInk
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Regular Commute Suggestion Card
                item {
                    SunlitStampedCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Parchment,
                        borderColor = ForestInk,
                        shadowOffset = 5.dp
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
                                    color = ForestInk,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${regularCommute.usualTrainName} (${regularCommute.usualTrainNumber})",
                                    fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestInk
                                )
                                Text(
                                    text = "${regularCommute.originStationName} ➔ ${regularCommute.destStationName} • ${regularCommute.usualDepartureTime} • Coach ${regularCommute.usualCoach}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CharcoalTextMuted
                                )
                            }

                            MarigoldPillButton(
                                onClick = onStartRegularCommute,
                                text = "Start",
                                modifier = Modifier.testTag("start_daily_commute_btn")
                            )
                        }
                    }
                }

                // Live Train Timetable Between Stations (e.g. Howrah to Hind Motor)
                item {
                    var fromStationInput by remember { mutableStateOf(nearbyStation?.code ?: "HWH") }
                    var toStationInput by remember { mutableStateOf("HMZ") }

                    SunlitStampedCard(
                        modifier = Modifier.fillMaxWidth().testTag("route_schedule_finder_card"),
                        containerColor = Parchment,
                        borderColor = ForestInk,
                        shadowOffset = 5.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🚆 Trains Between Stations",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestInk
                                )
                                Text(
                                    text = "etrain.info sync",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VividFern
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Fetch live suburban timetable (e.g. Howrah to Hind Motor) dynamically without hardcoded stations.",
                                fontSize = 11.sp,
                                color = CharcoalTextMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = fromStationInput,
                                    onValueChange = { fromStationInput = it.uppercase() },
                                    label = { Text("From (e.g. HWH)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("from_station_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = toStationInput,
                                    onValueChange = { toStationInput = it.uppercase() },
                                    label = { Text("To (e.g. HMZ)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("to_station_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            ForestPillButton(
                                onClick = { onFetchTrainsBetween(fromStationInput.trim(), toStationInput.trim()) },
                                text = if (isLoadingBetweenTrains) "Fetching Live Schedule..." else "Find Trains (${fromStationInput} ➔ ${toStationInput})",
                                modifier = Modifier.fillMaxWidth().testTag("search_between_trains_btn")
                            )

                            if (betweenTrains.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Available Trains (${betweenTrains.size} found):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestInk
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    betweenTrains.take(10).forEach { train ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                onSelectCandidate(train)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = SunlitCream,
                                            border = BorderStroke(1.dp, WarmBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${train.trainName} (${train.trainNumber})",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ForestInk
                                                    )
                                                    Text(
                                                        text = "Dep: ${train.departureTime} • ${train.platform} • ${train.originStationCode} ➔ ${train.destStationCode}",
                                                        fontSize = 11.sp,
                                                        color = CharcoalTextMuted
                                                    )
                                                }
                                                ForestPillButton(
                                                    onClick = { onSelectCandidate(train) },
                                                    text = "Select",
                                                    modifier = Modifier.testTag("select_train_${train.trainNumber}")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Candidate Train Preview (if user tapped a candidate or searched)
                if (selectedCandidate != null) {
                    item {
                        SunlitStampedCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = SunlitCream,
                            borderColor = ForestInk,
                            shadowOffset = 6.dp
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Selected Candidate Train",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestInk
                                    )
                                    IconButton(onClick = onClearCandidate) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = ForestInk)
                                    }
                                }

                                Text(
                                    text = "${selectedCandidate.trainName} (${selectedCandidate.trainNumber})",
                                    fontSize = if (isSeniorMode) 19.sp else 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestInk
                                )
                                Text(
                                    text = "Dep: ${selectedCandidate.departureTime} • ${selectedCandidate.platform} • ${selectedCandidate.originStationName} ➔ ${selectedCandidate.destStationName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CharcoalTextMuted
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedPillButton(
                                        onClick = onClearCandidate,
                                        text = "Cancel",
                                        modifier = Modifier.weight(1f)
                                    )

                                    ForestPillButton(
                                        onClick = { onStartJourney(selectedCandidate, selectedCoach) },
                                        text = "Board Train",
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("confirm_board_train_btn")
                                    )
                                }
                            }
                        }
                    }
                }

                // Station Departures / Radar List
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (nearbyStation != null) "Departures from ${nearbyStation.nameEn} (${nearbyStation.code})" else "Live Suburban Departures",
                                fontSize = if (isSeniorMode) 18.sp else 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestInk
                            )
                            Text(
                                text = "Real API Timetable",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VividFern
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Real API Station Switcher Chips
                        if (allStations.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allStations.forEach { st ->
                                    val isSelected = (nearbyStation?.code.equals(st.code, ignoreCase = true))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(1440.dp))
                                            .background(if (isSelected) ForestInk else SunlitCream)
                                            .border(BorderStroke(1.2.dp, ForestInk), RoundedCornerShape(1440.dp))
                                            .clickable {
                                                onSelectStationCode(st.code)
                                                onSimulateStation(st.code)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${st.code} • ${st.nameEn}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else ForestInk
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (stationCandidates.isEmpty()) {
                            SunlitStampedCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Parchment,
                                borderColor = ForestInk,
                                shadowOffset = 4.dp
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("No immediate departures detected at current station.", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ForestInk)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Tap any station chip above or search to view suburban trains.", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CharcoalTextMuted)
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
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestInk
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
                                    tint = ForestInk
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = ForestInk
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
                            shape = RoundedCornerShape(1440.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ForestInk,
                                unfocusedTextColor = ForestInk,
                                focusedContainerColor = SunlitCream,
                                unfocusedContainerColor = SunlitCream,
                                cursorColor = ForestInk,
                                focusedBorderColor = ForestInk,
                                unfocusedBorderColor = ForestInk.copy(alpha = 0.5f)
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
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestInk
                                    )
                                    TextButton(onClick = { onSearchQueryChange("") }) {
                                        Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestInk)
                                    }
                                }
                            } else {
                                SunlitStampedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = Parchment,
                                    borderColor = ForestInk,
                                    shadowOffset = 4.dp
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "No trains found for \"$searchQuery\"",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ForestInk
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
                    SunlitStampedCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Parchment,
                        borderColor = ForestInk,
                        shadowOffset = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
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
                                            .background(VividFern)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "LIVE JOURNEY TRACKING",
                                        color = ForestInk,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(1440.dp))
                                        .background(Marigold)
                                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                                        .clickable { onEndJourney() }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Stop, contentDescription = null, tint = ForestInk, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("End", color = ForestInk, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${journeySession.trainName} (${journeySession.trainNumber})",
                                color = ForestInk,
                                fontSize = if (isSeniorMode) 22.sp else 19.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Text(
                                text = "${journeySession.originStation} ➔ ${journeySession.destinationStation}",
                                color = CharcoalTextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(1440.dp))
                                        .background(SageWash)
                                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Coach: ${journeySession.currentCoach}",
                                        color = ForestInk,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "$confidenceScore% • $confidenceDescription",
                                    color = CharcoalTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
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
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestInk
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
                    SunlitStampedCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Parchment,
                        borderColor = ForestInk,
                        shadowOffset = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = LocalizationManager.getString("select_coach", language),
                                    fontSize = if (isSeniorMode) 17.sp else 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestInk
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(1440.dp))
                                        .background(Marigold)
                                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Current: $selectedCoach",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestInk
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

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
                                            .clip(RoundedCornerShape(1440.dp))
                                            .background(if (isSelected) ForestInk else SunlitCream)
                                            .border(1.dp, ForestInk, RoundedCornerShape(1440.dp))
                                            .clickable { onCoachSelect(coach) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("coach_chip_$coach")
                                    ) {
                                        Text(
                                            text = coach,
                                            color = if (isSelected) SunlitCream else ForestInk,
                                            fontSize = if (isSeniorMode) 15.sp else 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = seatLocationText,
                                onValueChange = { seatLocationText = it },
                                placeholder = { Text(LocalizationManager.getString("seat_desc", language)) },
                                singleLine = true,
                                shape = RoundedCornerShape(1440.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ForestInk,
                                    unfocusedTextColor = ForestInk,
                                    focusedContainerColor = SunlitCream,
                                    unfocusedContainerColor = SunlitCream,
                                    cursorColor = ForestInk,
                                    focusedBorderColor = ForestInk,
                                    unfocusedBorderColor = ForestInk.copy(alpha = 0.5f)
                                ),
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
                                SunlitStampedCard(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clickable { onSendHungerSignal(fav, seatLocationText) },
                                    containerColor = Parchment,
                                    borderColor = ForestInk,
                                    shadowOffset = 4.dp
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(80.dp)
                                                .background(SunlitCream)
                                        ) {
                                            if (!fav.imageUrl.isNullOrEmpty()) {
                                                AsyncImage(
                                                    model = fav.imageUrl,
                                                    contentDescription = fav.nameEn,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(4.dp)
                                                        .clip(CircleShape)
                                                        .background(Marigold)
                                                        .border(BorderStroke(1.dp, ForestInk), CircleShape)
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(text = fav.emoji, fontSize = 12.sp)
                                                }
                                            } else {
                                                Text(
                                                    text = fav.emoji,
                                                    fontSize = 32.sp,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = fav.nameEn,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = ForestInk,
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
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = ForestInk
                                                )
                                                Text(
                                                    text = "Signal ➔",
                                                    fontSize = 10.sp,
                                                    color = ForestInk,
                                                    fontWeight = FontWeight.ExtraBold
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
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestInk
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
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(1440.dp))
                                        .background(if (isSelected) ForestInk else SunlitCream)
                                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                                        .clickable { selectedFilter = key }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("filter_$key")
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = if (isSeniorMode) 14.sp else 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) SunlitCream else ForestInk
                                    )
                                }
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
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        UserGuidanceModal(
            isOpen = showGuidanceModal,
            onDismiss = { showGuidanceModal = false },
            initialAudience = guidanceInitialAudience
        )

        RailMadadDialog(
            isOpen = showRailMadadModal,
            onDismiss = { showRailMadadModal = false }
        )

        NotificationsDialog(
            isOpen = showNotificationsModal,
            onDismiss = { showNotificationsModal = false }
        )
    }
}

@Composable
fun CandidateTrainCard(
    candidate: TrainCandidate,
    isSeniorMode: Boolean,
    onSelect: () -> Unit
) {
    SunlitStampedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("candidate_train_${candidate.trainNumber}"),
        containerColor = Parchment,
        borderColor = ForestInk,
        shadowOffset = 4.dp
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
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (isSeniorMode) 16.sp else 14.sp,
                        color = ForestInk
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${candidate.originStationName} ➔ ${candidate.destStationName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = CharcoalTextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(1440.dp))
                            .background(SunlitCream)
                            .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(candidate.platform, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestInk)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(1440.dp))
                            .background(SageWash)
                            .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Upcoming", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestInk)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = candidate.departureTime,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (isSeniorMode) 18.sp else 16.sp,
                    color = ForestInk
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(1440.dp))
                        .background(Marigold)
                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Board ➔",
                        fontSize = 11.sp,
                        color = ForestInk,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
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

    SunlitStampedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("snack_card_${item.id}"),
        containerColor = Parchment,
        borderColor = ForestInk,
        shadowOffset = 5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Real Food Image with fallback emoji
                Box(
                    modifier = Modifier
                        .size(if (isSeniorMode) 68.dp else 60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SunlitCream)
                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.nameEn,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clip(RoundedCornerShape(topStart = 8.dp))
                                .background(Marigold)
                                .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(topStart = 8.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = item.emoji, fontSize = 12.sp)
                        }
                    } else {
                        Text(
                            text = item.emoji,
                            fontSize = if (isSeniorMode) 30.sp else 26.sp
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
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Typical ~₹${item.typicalPriceInr}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestInk
                        )
                    }

                    Text(
                        text = item.description,
                        fontSize = if (isSeniorMode) 13.sp else 11.sp,
                        color = CharcoalTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dietary Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.dietaryTags.take(2).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(1440.dp))
                                    .background(SageWash)
                                    .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    color = ForestInk,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stepper and Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Stepper with Sunlit styling
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(1440.dp))
                        .background(SunlitCream)
                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onQuantityDecrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = ForestInk, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "$quantity",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = ForestInk,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = onQuantityIncrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = ForestInk, modifier = Modifier.size(16.dp))
                    }
                }

                ForestPillButton(
                    onClick = onOrder,
                    text = "Signal in $selectedCoach",
                    modifier = Modifier
                        .height(if (isSeniorMode) 46.dp else 40.dp)
                        .testTag("signal_btn_${item.id}")
                )
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

    SunlitStampedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("request_item_${request.id}"),
        containerColor = when (request.status) {
            OrderStatus.PRICE_CONFIRMED.name -> SunlitCream
            OrderStatus.CUSTOMER_CONFIRMED.name -> Marigold
            OrderStatus.COMPLETED.name -> SageWash
            else -> Parchment
        },
        borderColor = ForestInk,
        shadowOffset = 4.dp
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
                            OrderStatus.COMPLETED.name -> VividFern
                            OrderStatus.CUSTOMER_CONFIRMED.name -> ForestInk
                            else -> ForestInk
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${request.foodItemName} × ${request.quantity}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (isSeniorMode) 16.sp else 14.sp,
                        color = ForestInk
                    )
                }

                if (request.status != OrderStatus.COMPLETED.name && request.status != OrderStatus.CUSTOMER_CONFIRMED.name) {
                    IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = ForestInk)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Price or Status Details
            if (isPriceConfirmed) {
                val unitPrice = request.offeredUnitPrice ?: 15
                val totalPrice = request.calculatedTotalPrice ?: (request.quantity * unitPrice)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(1440.dp))
                        .background(Parchment)
                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Vendor Offered: ₹$unitPrice / item",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestInk
                        )
                        Text(
                            text = "Total: ₹$totalPrice (${request.quantity} items)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestInk
                        )
                    }

                    MarigoldPillButton(
                        onClick = onConfirmPrice,
                        text = "Confirm",
                        modifier = Modifier.testTag("confirm_price_order_btn")
                    )
                }
            } else {
                Text(
                    text = when (request.status) {
                        OrderStatus.CUSTOMER_CONFIRMED.name -> "Order Confirmed. Vendor approaching Coach ${request.coachNumber}."
                        OrderStatus.COMPLETED.name -> "Delivered & Settled in Cash"
                        else -> "Broadcasting request to station & coach vendors..."
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = CharcoalTextMuted
                )
            }
        }
    }
}
