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
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storefront
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
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GoldYellowLight
import com.example.ui.theme.NatureGreen
import com.example.ui.theme.RailNavy
import com.example.ui.theme.TerracottaAmber
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSandBackground
import com.example.ui.theme.WarmSurface

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
    onQuickManualSale: (vendor: VendorEntity, foodName: String, amount: Double, coach: String) -> Unit
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

    var isOnline by remember { mutableStateOf(currentVendor.isOnline) }
    val coachList = listOf("CAB-1", "LD-1", "VND-1", "GS-1", "GS-2", "GS-3", "VND-2", "LD-2", "CAB-2")

    // Selected unit price for pending requests: Map<RequestId, UnitPrice>
    val selectedPrices = remember { mutableStateMapOf<Long, Int>() }

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

                // Vendor Active Profile Card
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
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(TerracottaAmber),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = "Vendor",
                                        tint = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = currentVendor.name,
                                        fontSize = if (isSeniorMode) 18.sp else 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                    Text(
                                        text = "Badge: ${currentVendor.badgeNumber} • ${currentVendor.specialityItemName}",
                                        fontSize = 12.sp,
                                        color = CharcoalTextMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isOnline) "ONLINE" else "OFFLINE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnline) NatureGreen else CharcoalTextMuted
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = isOnline,
                                    onCheckedChange = { isOnline = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = NatureGreen
                                    ),
                                    modifier = Modifier.testTag("vendor_online_switch")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch vendor identity row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allVendors.forEach { v ->
                                val isSelected = v.vendorId == currentVendor.vendorId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) RailNavy else Color(0xFFF1F5F9))
                                        .clickable { onSelectVendorProfile(v.vendorId) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = v.name,
                                        color = if (isSelected) Color.White else CharcoalText,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Vendor Train Shift Status
            item {
                if (journeySession != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = RailNavy)
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
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NatureGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ACTIVE SHIFT ONBOARD",
                                        color = NatureGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${journeySession.trainName} • Coach ${journeySession.currentCoach}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onEndShift,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = "End", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("End Shift", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Station Standby • Select Train to Board Shift",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RailNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (locationInfo.isNearStation && locationInfo.nearestStation != null) {
                                    "Near ${locationInfo.nearestStation.nameEn} Station. Pick train below to start receiving passenger hunger signals."
                                } else {
                                    "Ready to hawk? Choose train schedule to begin vending session."
                                },
                                fontSize = 12.sp,
                                color = CharcoalTextMuted
                            )

                            if (stationCandidates.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    stationCandidates.take(3).forEach { candidate ->
                                        Button(
                                            onClick = { onStartShift(candidate, "VND-1") },
                                            colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Board ${candidate.trainName}", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fair Income Distribution Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldYellowLight),
                    border = BorderStroke(1.dp, GoldYellow)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Balance,
                            contentDescription = "Fair Income Distribution",
                            tint = Color(0xFF92400E),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Fair Income Dispatch Priority Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isSeniorMode) 14.sp else 12.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Vendors with lower sales today receive hunger alerts first for equal daily earnings.",
                                fontSize = 11.sp,
                                color = CharcoalText
                            )
                        }
                    }
                }
            }

            // Coach Boarding & Conflict Prevention Guide
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
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Collision Guard",
                                    tint = NatureGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Coach Guard & Boarding",
                                    fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                            }

                            Text(
                                text = "Current: Coach ${currentVendor.currentCoach}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap a coach to verify collision: prevents duplicate vendors selling ${currentVendor.specialityItemName} in the same coach.",
                            fontSize = 11.sp,
                            color = CharcoalTextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Coach selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            coachList.forEach { coach ->
                                val isCurrent = currentVendor.currentCoach == coach
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCurrent) TerracottaAmber else WarmSurface)
                                        .border(1.dp, if (isCurrent) TerracottaAmber else WarmBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            onVerifyCoachBoarding(
                                                currentVendor.vendorId,
                                                currentVendor.specialityItemId,
                                                coach
                                            )
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("vendor_coach_$coach")
                                ) {
                                    Text(
                                        text = coach,
                                        color = if (isCurrent) Color.White else CharcoalText,
                                        fontSize = if (isSeniorMode) 15.sp else 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live Passenger Hunger Orders Feed
            if (!vendorHintShown) {
                item {
                    ContextualHintCard(
                        title = "New request",
                        description = "Accept a request and choose your selling price.",
                        onDismiss = onDismissVendorHint,
                        testTag = "hint_vendor_new_request"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Live Requests",
                            tint = TerracottaAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Passenger Signals (${activeRequests.size})",
                            fontSize = if (isSeniorMode) 17.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RailNavy
                        )
                    }
                }
            }

            if (activeRequests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = WarmSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Scanning train coaches for hunger requests... You're ready to serve!",
                                fontSize = 13.sp,
                                color = CharcoalTextMuted
                            )
                        }
                    }
                }
            } else {
                items(activeRequests) { req ->
                    val chosenPrice = selectedPrices[req.id] ?: (req.offeredUnitPrice ?: 15)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (req.assignedVendorId == currentVendor.vendorId) Color(0xFFEFF6FF) else WarmSurface
                        ),
                        border = BorderStroke(1.dp, WarmBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(RailNavy)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Coach ${req.coachNumber}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${req.foodItemName} × ${req.quantity}",
                                        fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText
                                    )
                                }

                                Text(
                                    text = if (req.offeredUnitPrice != null) "₹${req.calculatedTotalPrice}" else "Pending Price",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaAmber
                                )
                            }

                            if (req.seatDetail.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Seat Note: ${req.seatDetail}",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Step 1: Request is pending price offer from vendor
                            if (req.status == OrderStatus.REQUESTED.name || req.status == OrderStatus.OFFERED_TO_VENDOR.name) {
                                Text(
                                    text = "Select Unit Price for Passenger:",
                                    fontSize = 12.sp,
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
                                    AppConfig.ALLOWED_UNIT_PRICES.forEach { price ->
                                        val isSelected = chosenPrice == price
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) RailNavy else Color(0xFFF1F5F9))
                                                .clickable { selectedPrices[req.id] = price }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "₹$price",
                                                color = if (isSelected) Color.White else CharcoalText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total: ₹${chosenPrice * req.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CharcoalText
                                    )

                                    Button(
                                        onClick = { onAcceptAndOfferPrice(req, currentVendor, chosenPrice) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("offer_price_btn_${req.id}")
                                    ) {
                                        Text("Offer ₹$chosenPrice / item")
                                    }
                                }
                            } else if (req.status == OrderStatus.PRICE_CONFIRMED.name) {
                                Text(
                                    text = "Offered ₹${req.offeredUnitPrice} / item. Waiting for customer confirmation...",
                                    fontSize = 12.sp,
                                    color = Color(0xFFD97706),
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else if (req.status == OrderStatus.CUSTOMER_CONFIRMED.name) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Confirmed! Total: ₹${req.calculatedTotalPrice}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NatureGreen
                                    )

                                    Button(
                                        onClick = { onDeliverSale(req, currentVendor) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NatureGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("deliver_req_${req.id}")
                                    ) {
                                        Text("Deliver & Collect ₹${req.calculatedTotalPrice}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Manual Sale Register
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
                                text = "Quick Sale Cash Register",
                                fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )

                            Text(
                                text = "Today's Total: ₹${currentVendor.todayEarnings.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NatureGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick buttons (+10, +15, +20, +30)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(10, 15, 20, 30).forEach { amt ->
                                Button(
                                    onClick = {
                                        onQuickManualSale(
                                            currentVendor,
                                            currentVendor.specialityItemName,
                                            amt.toDouble(),
                                            currentVendor.currentCoach
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("quick_add_$amt"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF93C5FD))
                                ) {
                                    Text("+₹$amt", color = RailNavy, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
