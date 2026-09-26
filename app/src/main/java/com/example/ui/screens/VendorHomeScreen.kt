package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.AppConfig
import com.example.data.local.FoodRequestEntity
import com.example.data.local.VendorEntity
import com.example.data.location.UserLocationInfo
import com.example.data.model.IndianLanguage
import com.example.data.model.JourneySession
import com.example.data.model.OrderStatus
import com.example.data.model.TrainCandidate
import com.example.data.repository.TrainRouteDetails
import com.example.ui.components.ContextualHintCard
import com.example.ui.components.RailMadadDialog
import com.example.ui.components.UserGuidanceModal
import com.example.ui.components.UserGuideAudience
import com.example.ui.components.VendorInteractiveTutorialDialog
import com.example.ui.theme.ForestInk
import com.example.ui.theme.ForestPillButton
import com.example.ui.theme.Marigold
import com.example.ui.theme.MarigoldPillButton
import com.example.ui.theme.OutlinedPillButton
import com.example.ui.theme.Parchment
import com.example.ui.theme.SunlitCream
import com.example.ui.theme.SunlitStampedCard

enum class VendorOperationalTab(val label: String) {
    NEW_REQUESTS("New Requests"),
    ACCEPTED_ORDERS("Accepted Orders"),
    COMPLETED_ORDERS("Completed"),
    HELP("Help & Guide")
}

@Composable
fun VendorHomeScreen(
    vendor: VendorEntity?,
    allVendors: List<VendorEntity>,
    activeRequests: List<FoodRequestEntity>,
    journeySession: JourneySession?,
    selectedRoute: TrainRouteDetails?,
    selectedCoach: String,
    locationInfo: UserLocationInfo,
    stationCandidates: List<TrainCandidate>,
    language: IndianLanguage,
    isSeniorMode: Boolean,
    vendorHintShown: Boolean = true,
    onDismissVendorHint: () -> Unit = {},
    onSelectVendorProfile: (String) -> Unit,
    onVerifyCoachBoarding: (vendorId: String, specialityId: String, coachNumber: String) -> Unit,
    onStartShift: (TrainCandidate, String) -> Unit,
    onEndShift: () -> Unit,
    onAcceptAndOfferPrice: (FoodRequestEntity, VendorEntity, Int) -> Unit,
    onDeliverSale: (FoodRequestEntity, VendorEntity) -> Unit,
    onQuickManualSale: (vendor: VendorEntity, foodName: String, amount: Double, coach: String) -> Unit,
    availableCoaches: List<String> = emptyList(),
    onToggleAvailability: (Boolean) -> Unit = {},
    onRejectRequest: (FoodRequestEntity) -> Unit = {}
) {
    val currentVendor = vendor ?: (allVendors.firstOrNull() ?: VendorEntity(
        vendorId = "vendor_jhalmuri_1",
        name = "Subhash Da (ঝালমুড়ি)",
        badgeNumber = "ER-SDAH-104",
        specialityItemId = "jhalmuri_kol",
        specialityItemName = "Kolkata Jhalmuri",
        currentTrain = "31821",
        currentCoach = "VND-1",
        currentStation = "Barrackpore",
        todaySalesCount = 2,
        todayEarnings = 40.0
    ))

    var isOnline by remember(currentVendor.vendorId, currentVendor.isOnline) {
        mutableStateOf(currentVendor.isOnline)
    }

    var selectedTab by remember { mutableStateOf(VendorOperationalTab.NEW_REQUESTS) }

    // Selected unit price for pending requests: Map<RequestId, UnitPrice>
    val selectedPrices = remember { mutableStateMapOf<Long, Int>() }

    var showVendorGuide by remember { mutableStateOf(false) }
    var showVendorMadad by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }

    // Group requests by status
    val pendingRequests = activeRequests.filter {
        it.status == OrderStatus.REQUESTED.name || it.status == OrderStatus.OFFERED_TO_VENDOR.name
    }
    val acceptedOrders = activeRequests.filter {
        it.status == OrderStatus.PRICE_CONFIRMED.name || it.status == OrderStatus.CUSTOMER_CONFIRMED.name
    }
    val completedOrders = activeRequests.filter {
        it.status == OrderStatus.COMPLETED.name
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SunlitCream
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Section 6: Header with RailSaathi, Availability Status, and Reconnect/Sync State
                SunlitStampedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vendor_primary_header_card"),
                    containerColor = Parchment,
                    borderColor = ForestInk,
                    shadowOffset = 5.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "RailSaathi",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ForestInk
                                )
                                Text(
                                    text = "VENDOR DASHBOARD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = ForestInk.copy(alpha = 0.7f)
                                )
                            }

                            // Availability Status Control
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(1440.dp))
                                    .background(if (isOnline) Color(0xFFE8F8EE) else Color(0xFFFDE8E8))
                                    .border(
                                        BorderStroke(1.5.dp, if (isOnline) Color(0xFF1E8E3E) else Color(0xFFD93025)),
                                        RoundedCornerShape(1440.dp)
                                    )
                                    .clickable {
                                        val newStatus = !isOnline
                                        isOnline = newStatus
                                        onToggleAvailability(newStatus)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .testTag("vendor_availability_toggle_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isOnline) Color(0xFF1E8E3E) else Color(0xFFD93025))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isOnline) "Available" else "Not Available",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = if (isOnline) Color(0xFF1E8E3E) else Color(0xFFD93025)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Offline-First / Reconnect sync state indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync State",
                                    tint = ForestInk.copy(alpha = 0.7f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) "Live Engine Active • Connected" else "Standby Mode • Accepted orders stay active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForestInk.copy(alpha = 0.8f)
                                )
                            }

                            Text(
                                text = "Badge: ${currentVendor.badgeNumber}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestInk
                            )
                        }
                    }
                }
            }

            // Section 6: Current train/journey context & Current station/operating area
            item {
                SunlitStampedCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (journeySession != null) ForestInk else Parchment,
                    borderColor = ForestInk,
                    shadowOffset = 4.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (journeySession != null) "CURRENT TRAIN & COACH" else "OPERATING AREA / STATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp,
                                    color = if (journeySession != null) Marigold else ForestInk.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (journeySession != null) {
                                        "${journeySession.trainName} • Coach ${journeySession.currentCoach}"
                                    } else {
                                        val stationName = locationInfo.nearestStation?.nameEn ?: currentVendor.currentStation
                                        "$stationName Station Standby"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (journeySession != null) SunlitCream else ForestInk
                                )
                            }

                            if (journeySession != null) {
                                MarigoldPillButton(
                                    onClick = onEndShift,
                                    text = "End Shift"
                                )
                            } else if (stationCandidates.isNotEmpty()) {
                                ForestPillButton(
                                    onClick = { onStartShift(stationCandidates.first(), "VND-1") },
                                    text = "Board Train"
                                )
                            }
                        }
                    }
                }
            }

            // Section 6: Main Actions Tabs (New Requests, Accepted Orders, Completed Orders, Help)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // New Requests Tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(1440.dp))
                            .background(if (selectedTab == VendorOperationalTab.NEW_REQUESTS) ForestInk else Parchment)
                            .border(BorderStroke(1.2.dp, ForestInk), RoundedCornerShape(1440.dp))
                            .clickable { selectedTab = VendorOperationalTab.NEW_REQUESTS }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("vendor_tab_new_requests")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "New Requests",
                                color = if (selectedTab == VendorOperationalTab.NEW_REQUESTS) SunlitCream else ForestInk,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (pendingRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (selectedTab == VendorOperationalTab.NEW_REQUESTS) Marigold else ForestInk)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${pendingRequests.size}",
                                        color = if (selectedTab == VendorOperationalTab.NEW_REQUESTS) ForestInk else SunlitCream,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // Accepted Orders Tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(1440.dp))
                            .background(if (selectedTab == VendorOperationalTab.ACCEPTED_ORDERS) ForestInk else Parchment)
                            .border(BorderStroke(1.2.dp, ForestInk), RoundedCornerShape(1440.dp))
                            .clickable { selectedTab = VendorOperationalTab.ACCEPTED_ORDERS }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("vendor_tab_accepted_orders")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Accepted Orders",
                                color = if (selectedTab == VendorOperationalTab.ACCEPTED_ORDERS) SunlitCream else ForestInk,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (acceptedOrders.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (selectedTab == VendorOperationalTab.ACCEPTED_ORDERS) Marigold else ForestInk)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${acceptedOrders.size}",
                                        color = if (selectedTab == VendorOperationalTab.ACCEPTED_ORDERS) ForestInk else SunlitCream,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // Completed Orders Tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(1440.dp))
                            .background(if (selectedTab == VendorOperationalTab.COMPLETED_ORDERS) ForestInk else Parchment)
                            .border(BorderStroke(1.2.dp, ForestInk), RoundedCornerShape(1440.dp))
                            .clickable { selectedTab = VendorOperationalTab.COMPLETED_ORDERS }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("vendor_tab_completed_orders")
                    ) {
                        Text(
                            text = "Completed (${completedOrders.size + currentVendor.todaySalesCount})",
                            color = if (selectedTab == VendorOperationalTab.COMPLETED_ORDERS) SunlitCream else ForestInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Help Tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(1440.dp))
                            .background(if (selectedTab == VendorOperationalTab.HELP) ForestInk else Parchment)
                            .border(BorderStroke(1.2.dp, ForestInk), RoundedCornerShape(1440.dp))
                            .clickable { selectedTab = VendorOperationalTab.HELP }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("vendor_tab_help")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Help",
                                tint = if (selectedTab == VendorOperationalTab.HELP) SunlitCream else ForestInk,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Help",
                                color = if (selectedTab == VendorOperationalTab.HELP) SunlitCream else ForestInk,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // TAB 1: NEW REQUESTS (Section 7: Clear Hierarchy & Immediate Action)
            // -----------------------------------------------------------------
            if (selectedTab == VendorOperationalTab.NEW_REQUESTS) {
                if (!isOnline) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vendor_not_available_banner"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7E0)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFF29900))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "Warning",
                                    tint = Color(0xFFF29900),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "You are currently marked as Not Available",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = ForestInk
                                    )
                                    Text(
                                        text = "Toggle your status to 'Available' above to accept new passenger requests.",
                                        fontSize = 11.sp,
                                        color = ForestInk.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                if (pendingRequests.isEmpty()) {
                    item {
                        SunlitStampedCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Parchment,
                            borderColor = ForestInk,
                            shadowOffset = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Listening",
                                        tint = ForestInk.copy(alpha = 0.5f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No pending food requests right now.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestInk
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "You will be alerted instantly when passengers signal hunger!",
                                        fontSize = 12.sp,
                                        color = ForestInk.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(pendingRequests) { req ->
                        val chosenPrice = selectedPrices[req.id] ?: (req.offeredUnitPrice ?: 15)

                        SunlitStampedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vendor_request_card_${req.id}"),
                            containerColor = Parchment,
                            borderColor = ForestInk,
                            shadowOffset = 5.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Request Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Marigold)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "NEW FOOD REQUEST",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = ForestInk
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(1440.dp))
                                            .background(ForestInk)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Coach ${req.coachNumber}",
                                            color = SunlitCream,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Clear Information Hierarchy (Section 7)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${req.foodItemName} × ${req.quantity}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ForestInk
                                    )
                                    Text(
                                        text = "Train: ${req.trainName.ifBlank { req.trainNumber }}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ForestInk
                                    )
                                    if (req.targetStationName.isNotBlank() || req.targetStationCode.isNotBlank()) {
                                        Text(
                                            text = "Target Station: ${req.targetStationName} (${req.targetStationCode})",
                                            fontSize = 12.sp,
                                            color = ForestInk.copy(alpha = 0.8f)
                                        )
                                    }
                                    if (req.seatDetail.isNotBlank()) {
                                        Text(
                                            text = "Seat / Location Note: ${req.seatDetail}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ForestInk
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Price Selector
                                Text(
                                    text = "Select Unit Price for Passenger:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestInk
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AppConfig.ALLOWED_UNIT_PRICES.forEach { price ->
                                        val isSelected = chosenPrice == price
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(1440.dp))
                                                .background(if (isSelected) ForestInk else SunlitCream)
                                                .border(BorderStroke(1.2.dp, ForestInk), RoundedCornerShape(1440.dp))
                                                .clickable { selectedPrices[req.id] = price }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "₹$price",
                                                color = if (isSelected) SunlitCream else ForestInk,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Primary Actions: ACCEPT (Large, 52dp) and REJECT (48dp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { onRejectRequest(req) },
                                        border = BorderStroke(1.5.dp, Color(0xFFD93025)),
                                        shape = RoundedCornerShape(1440.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("reject_btn_${req.id}")
                                    ) {
                                        Text(
                                            text = "Reject",
                                            color = Color(0xFFD93025),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Button(
                                        onClick = { onAcceptAndOfferPrice(req, currentVendor, chosenPrice) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestInk),
                                        shape = RoundedCornerShape(1440.dp),
                                        modifier = Modifier
                                            .weight(1.8f)
                                            .height(52.dp)
                                            .testTag("offer_price_btn_${req.id}")
                                    ) {
                                        Text(
                                            text = "ACCEPT (₹${chosenPrice * req.quantity})",
                                            color = SunlitCream,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // TAB 2: ACCEPTED ORDERS
            // -----------------------------------------------------------------
            if (selectedTab == VendorOperationalTab.ACCEPTED_ORDERS) {
                if (acceptedOrders.isEmpty()) {
                    item {
                        SunlitStampedCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Parchment,
                            borderColor = ForestInk,
                            shadowOffset = 4.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No accepted orders currently in progress.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForestInk.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    items(acceptedOrders) { req ->
                        SunlitStampedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vendor_accepted_order_${req.id}"),
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
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(1440.dp))
                                            .background(ForestInk)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Coach ${req.coachNumber}",
                                            color = SunlitCream,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Text(
                                        text = "Total: ₹${req.calculatedTotalPrice}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ForestInk
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "${req.foodItemName} × ${req.quantity}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ForestInk
                                )

                                if (req.seatDetail.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Deliver to: ${req.seatDetail}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ForestInk
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (req.status == OrderStatus.PRICE_CONFIRMED.name) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SunlitCream)
                                            .border(BorderStroke(1.dp, ForestInk.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "Offered ₹${req.offeredUnitPrice}/item. Awaiting passenger confirmation...",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestInk
                                        )
                                    }
                                } else if (req.status == OrderStatus.CUSTOMER_CONFIRMED.name) {
                                    Button(
                                        onClick = { onDeliverSale(req, currentVendor) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Marigold),
                                        shape = RoundedCornerShape(1440.dp),
                                        border = BorderStroke(1.5.dp, ForestInk),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("deliver_req_${req.id}")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Deliver",
                                                tint = ForestInk,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Deliver & Collect ₹${req.calculatedTotalPrice}",
                                                color = ForestInk,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // TAB 3: COMPLETED ORDERS & EARNINGS SUMMARY
            // -----------------------------------------------------------------
            if (selectedTab == VendorOperationalTab.COMPLETED_ORDERS) {
                item {
                    // Earnings Summary Card
                    SunlitStampedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vendor_earnings_summary_card"),
                        containerColor = Parchment,
                        borderColor = ForestInk,
                        shadowOffset = 5.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Today's Earnings Summary",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = ForestInk
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Sales Revenue",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ForestInk.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "₹${currentVendor.todayEarnings.toInt()}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ForestInk
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Items Delivered",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ForestInk.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${currentVendor.todaySalesCount}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ForestInk
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Cash Register for Platform Walk-Ups
                item {
                    SunlitStampedCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Parchment,
                        borderColor = ForestInk,
                        shadowOffset = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Quick Walk-Up Sale Register",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestInk
                            )
                            Text(
                                text = "Record platform or corridor cash sales instantly:",
                                fontSize = 11.sp,
                                color = ForestInk.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(10, 15, 20, 30).forEach { amt ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(1440.dp))
                                            .background(SunlitCream)
                                            .border(BorderStroke(1.5.dp, ForestInk), RoundedCornerShape(1440.dp))
                                            .clickable {
                                                onQuickManualSale(
                                                    currentVendor,
                                                    currentVendor.specialityItemName,
                                                    amt.toDouble(),
                                                    currentVendor.currentCoach
                                                )
                                            }
                                            .padding(vertical = 10.dp)
                                            .testTag("quick_add_$amt"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+₹$amt",
                                            color = ForestInk,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // TAB 4: HELP & TOOLS
            // -----------------------------------------------------------------
            if (selectedTab == VendorOperationalTab.HELP) {
                // Interactive Tutorial Replay
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(BorderStroke(1.5.dp, ForestInk), RoundedCornerShape(14.dp))
                            .clickable { showTutorialDialog = true }
                            .testTag("vendor_replay_tutorial_card"),
                        colors = CardDefaults.cardColors(containerColor = Marigold)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Parchment)
                                    .border(BorderStroke(1.dp, ForestInk), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "Tutorial",
                                    tint = ForestInk,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Replay Vendor Tutorial",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestInk
                                )
                                Text(
                                    text = "Interactive 5-step guide on orders, pricing, and delivery",
                                    fontSize = 11.sp,
                                    color = ForestInk.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Vendor Handbook
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(BorderStroke(1.5.dp, ForestInk), RoundedCornerShape(14.dp))
                            .clickable { showVendorGuide = true }
                            .testTag("vendor_guide_handbook_card"),
                        colors = CardDefaults.cardColors(containerColor = Parchment)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F0FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "Handbook",
                                    tint = ForestInk,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Vendor Handbook & Guidelines",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestInk
                                )
                                Text(
                                    text = "Authorized hawker rules and suburban route guidelines",
                                    fontSize = 11.sp,
                                    color = ForestInk.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // Rail Madad 139
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(BorderStroke(1.5.dp, ForestInk), RoundedCornerShape(14.dp))
                            .clickable { showVendorMadad = true }
                            .testTag("vendor_rail_madad_card"),
                        colors = CardDefaults.cardColors(containerColor = Parchment)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFCE8E6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handshake,
                                    contentDescription = "Rail Madad",
                                    tint = Color(0xFFD93025),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Rail Madad 139 & RPF Emergency",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestInk
                                )
                                Text(
                                    text = "Emergency helpline, medical assistance, and RPF contact",
                                    fontSize = 11.sp,
                                    color = ForestInk.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        UserGuidanceModal(
            isOpen = showVendorGuide,
            onDismiss = { showVendorGuide = false },
            initialAudience = UserGuideAudience.SUBURBAN_VENDOR
        )

        RailMadadDialog(
            isOpen = showVendorMadad,
            onDismiss = { showVendorMadad = false }
        )

        VendorInteractiveTutorialDialog(
            isOpen = showTutorialDialog,
            onDismiss = { showTutorialDialog = false }
        )
    }
}
