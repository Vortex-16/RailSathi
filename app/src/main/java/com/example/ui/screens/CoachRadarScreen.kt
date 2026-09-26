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
import com.example.ui.theme.ForestPillButton
import com.example.ui.theme.MarigoldPillButton
import com.example.ui.theme.SunlitStampedCard
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.ForestInk
import com.example.ui.theme.Marigold
import com.example.ui.theme.NatureGreen
import com.example.ui.theme.Parchment
import com.example.ui.theme.RailNavy
import com.example.ui.theme.SunlitCream
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
        color = SunlitCream
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
                SunlitStampedCard(
                    modifier = Modifier.fillMaxWidth(),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Authentic EMU Rake Formation & Radar",
                                    fontSize = if (isSeniorMode) 18.sp else 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestInk
                                )
                                Text(
                                    text = "${selectedRoute?.trainName ?: "Standard 9-Car Suburban EMU Rake"} • Eastern Railway",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CharcoalTextMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(1440.dp))
                                    .background(Marigold)
                                    .border(BorderStroke(1.5.dp, ForestInk), RoundedCornerShape(1440.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Selected: $selectedCoach",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestInk
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Collision Guard policy badge
                        SunlitStampedCard(
                            containerColor = SunlitCream,
                            borderColor = ForestInk,
                            shadowOffset = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Collision Policy",
                                    tint = ForestInk,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Collision Policy: Different snack vendors (e.g. Jhalmuri + Chai) can share a coach. Same-item vendors are routed to alternative coaches to protect income.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ForestInk
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
                    fontWeight = FontWeight.ExtraBold,
                    color = ForestInk
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emuCoaches.forEach { emuCoach ->
                        val coachCode = emuCoach.coachCode
                        val coachRequests = activeRequests.filter { it.coachNumber == coachCode }
                        val coachVendors = allVendors.filter { it.currentCoach == coachCode }
                        val isSelected = selectedCoach == coachCode

                        val (coachIcon, coachLabel) = when (emuCoach.type) {
                            CoachType.CAB_DIVYANG -> Pair(Icons.Default.Accessible, "Cab")
                            CoachType.LADIES_SPECIAL -> Pair(Icons.Default.Female, "Ladies")
                            CoachType.VENDOR_LUGGAGE -> Pair(Icons.Default.LocalShipping, "Luggage")
                            CoachType.GENERAL -> Pair(Icons.Default.Groups, "General")
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Marigold else Parchment)
                                .border(BorderStroke(if (isSelected) 2.5.dp else 1.5.dp, ForestInk), RoundedCornerShape(12.dp))
                                .clickable { onSelectCoach(coachCode) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("rake_coach_$coachCode"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = coachIcon,
                                        contentDescription = emuCoach.nameEn,
                                        tint = ForestInk,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = coachCode,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = ForestInk
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (coachRequests.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Marigold)
                                                .border(BorderStroke(1.dp, ForestInk), CircleShape)
                                        )
                                    }
                                    if (coachVendors.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(ForestInk)
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

                val typeLabel = when (emuCoach.type) {
                    CoachType.CAB_DIVYANG -> "Engine + Divyangjan (দিব্যাঙ্গ)"
                    CoachType.LADIES_SPECIAL -> "Ladies Special (মহিলা কামরা)"
                    CoachType.VENDOR_LUGGAGE -> "Hawker & Luggage (সবজি/হকার डिब्बा)"
                    CoachType.GENERAL -> "General Passenger (সাধারণ)"
                }

                SunlitStampedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCoach(coachCode) }
                        .testTag("coach_card_$coachCode"),
                    containerColor = if (isSelected) Marigold else Parchment,
                    borderColor = ForestInk,
                    shadowOffset = if (isSelected) 6.dp else 4.dp
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
                                        .clip(RoundedCornerShape(1440.dp))
                                        .background(ForestInk)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = coachCode,
                                        color = SunlitCream,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = if (isSeniorMode) 15.sp else 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(1440.dp))
                                        .background(SunlitCream)
                                        .border(BorderStroke(1.dp, ForestInk), RoundedCornerShape(1440.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = typeLabel,
                                        color = ForestInk,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            if (role == UserRole.VENDOR && emuCoach.isVendorAllowed) {
                                ForestPillButton(
                                    onClick = { onVendorBoardCoach(coachCode) },
                                    text = "Board $coachCode"
                                )
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
                            fontWeight = FontWeight.Medium,
                            color = ForestInk.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Active Hunger Signals
                        if (coachRequests.isNotEmpty()) {
                            SunlitStampedCard(
                                containerColor = SunlitCream,
                                borderColor = ForestInk,
                                shadowOffset = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ ${coachRequests.size} Active Food Request(s): " + coachRequests.joinToString { it.foodItemName },
                                        color = ForestInk,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
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
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestInk
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
                                        tint = ForestInk,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${v.name} (${v.specialityItemName}) • ${v.todaySalesCount} sales today",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ForestInk
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = if (emuCoach.isVendorAllowed) "No vendor in $coachCode right now. Open for business!" else "Hawkers restricted in Cab coach.",
                                fontSize = 11.sp,
                                color = if (emuCoach.isVendorAllowed) ForestInk else CharcoalTextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}
