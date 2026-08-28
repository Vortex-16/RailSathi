package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FoodRequestEntity
import com.example.data.local.VendorEntity
import com.example.data.model.AuthenticEmuFormations
import com.example.data.model.CoachType
import com.example.data.model.IndianLanguage
import com.example.data.model.UserRole
import com.example.data.repository.TrainRouteDetails
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
fun CoachRadarScreen(
    role: UserRole,
    selectedCoach: String,
    selectedRoute: TrainRouteDetails?,
    allVendors: List<VendorEntity>,
    activeRequests: List<FoodRequestEntity>,
    language: IndianLanguage,
    isSeniorMode: Boolean,
    onSelectCoach: (String) -> Unit,
    onVendorBoardCoach: (String) -> Unit
) {
    val emuCoaches = AuthenticEmuFormations.easternRailway9CarRake

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

                // Train Header Overview
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Authentic EMU Rake Formation & Radar",
                                    fontSize = if (isSeniorMode) 18.sp else 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RailNavy
                                )
                                Text(
                                    text = "${selectedRoute?.trainName ?: "Standard 9-Car Suburban EMU Rake"} • Eastern Railway",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFDBEAFE))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Selected: $selectedCoach",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RailNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Collision Guard policy badge
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NatureGreenLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Collision Policy",
                                    tint = NatureGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Collision Policy: Different snack vendors (e.g. Jhalmuri + Chai) can share a coach. Same-item vendors are routed to alternative coaches to protect income.",
                                    fontSize = 11.sp,
                                    color = CharcoalText
                                )
                            }
                        }
                    }
                }
            }

            // Visual Train Rake Strip (West Bengal & Indian suburban authentic structure)
            item {
                Text(
                    text = "EMU Rake Coach Sequence (Engine ➔ Rear)",
                    fontSize = if (isSeniorMode) 16.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    emuCoaches.forEach { emuCoach ->
                        val coachCode = emuCoach.coachCode
                        val coachRequests = activeRequests.filter { it.coachNumber == coachCode }
                        val coachVendors = allVendors.filter { it.currentCoach == coachCode }
                        val isSelected = selectedCoach == coachCode

                        val (coachColor, coachIcon) = when (emuCoach.type) {
                            CoachType.CAB_DIVYANG -> Pair(Color(0xFF334155), Icons.Default.Accessible)
                            CoachType.LADIES_SPECIAL -> Pair(Color(0xFFBE185D), Icons.Default.Female)
                            CoachType.VENDOR_LUGGAGE -> Pair(Color(0xFFD97706), Icons.Default.LocalShipping)
                            CoachType.GENERAL -> Pair(Color(0xFF1E3A8A), Icons.Default.Groups)
                        }

                        Card(
                            modifier = Modifier
                                .clickable { onSelectCoach(coachCode) }
                                .testTag("rake_coach_$coachCode"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFDBEAFE) else WarmSurface
                            ),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) coachColor else WarmBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = coachIcon,
                                        contentDescription = emuCoach.nameEn,
                                        tint = coachColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = coachCode,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) coachColor else CharcoalText
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (coachRequests.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(TerracottaAmber)
                                        )
                                    }
                                    if (coachVendors.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(NatureGreen)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Coach Cards Detailed Breakdown
            items(emuCoaches) { emuCoach ->
                val coachCode = emuCoach.coachCode
                val coachRequests = activeRequests.filter { it.coachNumber == coachCode }
                val coachVendors = allVendors.filter { it.currentCoach == coachCode }
                val isSelected = selectedCoach == coachCode

                val (badgeColor, badgeTextColor, typeLabel) = when (emuCoach.type) {
                    CoachType.CAB_DIVYANG -> Triple(Color(0xFF334155), Color.White, "Engine + Divyangjan (দিব্যাঙ্গ)")
                    CoachType.LADIES_SPECIAL -> Triple(Color(0xFFFCE7F3), Color(0xFF9D174D), "Ladies Special (মহিলা কামরা)")
                    CoachType.VENDOR_LUGGAGE -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "Hawker & Luggage (সবজি/হকার डिब्बा)")
                    CoachType.GENERAL -> Triple(Color(0xFFDBEAFE), Color(0xFF1E40AF), "General Passenger (সাধারণ)")
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCoach(coachCode) }
                        .testTag("coach_card_$coachCode"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFF0FDF4) else WarmSurface
                    ),
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) NatureGreen else WarmBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RailNavy)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = coachCode,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isSeniorMode) 15.sp else 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeColor)
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = typeLabel,
                                        color = badgeTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (role == UserRole.VENDOR && emuCoach.isVendorAllowed) {
                                Button(
                                    onClick = { onVendorBoardCoach(coachCode) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.heightIn(min = 34.dp)
                                ) {
                                    Text("Board $coachCode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = when (language) {
                                IndianLanguage.BENGALI -> emuCoach.nameBn
                                IndianLanguage.HINDI -> emuCoach.nameHi
                                else -> emuCoach.nameEn
                            } + " • " + emuCoach.description,
                            fontSize = 11.sp,
                            color = CharcoalTextMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Active Hunger Signals
                        if (coachRequests.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ ${coachRequests.size} Active Food Request(s): " + coachRequests.joinToString { it.foodItemName },
                                        color = Color(0xFF92400E),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Vendors currently in this coach
                        if (coachVendors.isNotEmpty()) {
                            Text(
                                text = "Vendors in $coachCode:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            coachVendors.forEach { v ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = "Vendor",
                                        tint = NatureGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${v.name} (${v.specialityItemName}) • ${v.todaySalesCount} sales today",
                                        fontSize = 12.sp,
                                        color = CharcoalText
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = if (emuCoach.isVendorAllowed) "No vendor in $coachCode right now. Open for business!" else "Hawkers restricted in Cab coach.",
                                fontSize = 11.sp,
                                color = if (emuCoach.isVendorAllowed) NatureGreen else CharcoalTextMuted,
                                fontWeight = FontWeight.Medium
                            )
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
