package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestInk
import com.example.ui.theme.Parchment

// =========================================================================
// SECTION 1: SUBURBAN COMMUTER HERO ACTIONS (AUTHENTIC LOCAL COMMUTER NEEDS)
// =========================================================================
@Composable
fun SuburbanCommuterHeroSection(
    isSeniorMode: Boolean,
    onSignalChai: () -> Unit,
    onLiveTimetable: () -> Unit,
    onCoachRadar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Commuter Quick Actions",
                fontSize = if (isSeniorMode) 18.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ForestInk
            )
            Text(
                text = "Real-time Suburban EMU",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ForestInk.copy(alpha = 0.65f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Signal Chai to Coach (The core RailSathi feature)
            SuburbanActionCard(
                title = "Signal Chai",
                subtitle = "Hot Tea to Seat",
                icon = Icons.Default.Fastfood,
                gradientColors = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
                badgeText = "Instant",
                badgeBg = Color(0xFFE65100),
                modifier = Modifier.weight(1f),
                onClick = onSignalChai,
                testTag = "suburban_signal_chai_btn"
            )

            // Card 2: Live EMU Timetable (Real API Departures)
            SuburbanActionCard(
                title = "Live Board",
                subtitle = "Platform & Trains",
                icon = Icons.Default.DirectionsTransit,
                gradientColors = listOf(Color(0xFFE8F8EE), Color(0xFFC8E6C9)),
                badgeText = "Live API",
                badgeBg = Color(0xFF1B5E20),
                modifier = Modifier.weight(1f),
                onClick = onLiveTimetable,
                testTag = "suburban_live_board_btn"
            )

            // Card 3: Coach Crowding Radar
            SuburbanActionCard(
                title = "Coach Radar",
                subtitle = "Ladies & Divyang",
                icon = Icons.Default.NearMe,
                gradientColors = listOf(Color(0xFFE8F0FE), Color(0xFFBBDEFB)),
                badgeText = "EMU",
                badgeBg = Color(0xFF0D47A1),
                modifier = Modifier.weight(1f),
                onClick = onCoachRadar,
                testTag = "suburban_coach_radar_btn"
            )
        }
    }
}

@Composable
private fun SuburbanActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    badgeText: String,
    badgeBg: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.5.dp, ForestInk), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = ForestInk,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ForestInk,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// =========================================================================
// SECTION 2: SUBURBAN OFFERINGS GRID (AUTHENTIC COMMUTER & HAWKER UTILITIES)
// =========================================================================
@Composable
fun SuburbanOfferingsGrid(
    isSeniorMode: Boolean,
    onLiveTimetable: () -> Unit,
    onCoachPosition: () -> Unit,
    onTrackTrain: () -> Unit,
    onOrderFood: () -> Unit,
    onDailyBudget: () -> Unit,
    onRailMadad: () -> Unit,
    onUserGuidance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Suburban Travel Utilities",
            fontSize = if (isSeniorMode) 18.sp else 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestInk
        )

        // Row 1 (4 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuburbanOfferingTile(
                title = "Live Board",
                icon = Icons.Default.DirectionsTransit,
                bg = Color(0xFFFFEBEE),
                tint = Color(0xFFD32F2F),
                onClick = onLiveTimetable,
                testTag = "offering_live_board",
                modifier = Modifier.weight(1f)
            )

            SuburbanOfferingTile(
                title = "Coach Radar",
                icon = Icons.Default.NearMe,
                bg = Color(0xFFE3F2FD),
                tint = Color(0xFF1565C0),
                onClick = onCoachPosition,
                testTag = "offering_coach_position",
                modifier = Modifier.weight(1f)
            )

            SuburbanOfferingTile(
                title = "Track EMU",
                icon = Icons.Default.Shield,
                bg = Color(0xFFFFF8E1),
                tint = Color(0xFFF57F17),
                onClick = onTrackTrain,
                testTag = "offering_track_train",
                modifier = Modifier.weight(1f)
            )

            SuburbanOfferingTile(
                title = "Order Chai",
                icon = Icons.Default.Fastfood,
                bg = Color(0xFFF3E5F5),
                tint = Color(0xFF7B1FA2),
                onClick = onOrderFood,
                testTag = "offering_order_food",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2 (4 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuburbanOfferingTile(
                title = "Daily Budget",
                icon = Icons.Default.Receipt,
                bg = Color(0xFFECEFF1),
                tint = Color(0xFF455A64),
                onClick = onDailyBudget,
                testTag = "offering_daily_budget",
                modifier = Modifier.weight(1f)
            )

            SuburbanOfferingTile(
                title = "Rail Madad",
                icon = Icons.Default.Handshake,
                bg = Color(0xFFFBE9E7),
                tint = Color(0xFFD84315),
                onClick = onRailMadad,
                testTag = "offering_rail_madad",
                modifier = Modifier.weight(1f)
            )

            SuburbanOfferingTile(
                title = "Hawker Shield",
                icon = Icons.Default.Shield,
                bg = Color(0xFFE8F5E9),
                tint = Color(0xFF2E7D32),
                onClick = onUserGuidance,
                testTag = "offering_hawker_shield",
                modifier = Modifier.weight(1f)
            )

            SuburbanOfferingTile(
                title = "User Guide",
                icon = Icons.Default.MenuBook,
                bg = Color(0xFFE0F2F1),
                tint = Color(0xFF00695C),
                onClick = onUserGuidance,
                testTag = "offering_user_guide",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SuburbanOfferingTile(
    title: String,
    icon: ImageVector,
    bg: Color,
    tint: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(BorderStroke(1.dp, ForestInk.copy(alpha = 0.2f)), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ForestInk,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 13.sp
            )
        }
    }
}

// =========================================================================
// SECTION 3: DO YOU KNOW? (SUBURBAN RAILWAY CAROUSEL)
// =========================================================================
@Composable
fun DoYouKnowCarousel(
    isSeniorMode: Boolean,
    modifier: Modifier = Modifier
) {
    val facts = listOf(
        FactCardData(
            id = "1",
            title = "Zero-Data Station Tracking",
            description = "Offline cellular and step cadence automatically determine your next station stop without burning 4G/5G data.",
            tag = "Offline Tech",
            icon = Icons.Default.NearMe,
            accent = Color(0xFF1E8E3E),
            bg = Color(0xFFE8F8EE)
        ),
        FactCardData(
            id = "2",
            title = "Vendor Collision Guard",
            description = "Suburban vendors check coach occupancy so two chai or jhalmuri sellers don't enter the same coach at the same time.",
            tag = "Crowd Guard",
            icon = Icons.Default.Shield,
            accent = Color(0xFF1967D2),
            bg = Color(0xFFE8F0FE)
        ),
        FactCardData(
            id = "3",
            title = "Silent Chai Signals",
            description = "Signal tea or water from your seat silently. Nearby vendors will bring it to your coach without yelling in the aisle.",
            tag = "Smart Commute",
            icon = Icons.Default.Fastfood,
            accent = Color(0xFFF29900),
            bg = Color(0xFFFEF7E0)
        ),
        FactCardData(
            id = "4",
            title = "Divyang & Senior Priority",
            description = "Emergency 139 and coach radar prioritize middle and vendor-adjacent coaches for senior accessibility.",
            tag = "Passenger Care",
            icon = Icons.Default.Info,
            accent = Color(0xFF8430CE),
            bg = Color(0xFFF3E8FD)
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Tips",
                    tint = Color(0xFFF29900),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Do You know?",
                    fontSize = if (isSeniorMode) 18.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ForestInk
                )
            }
            Text(
                text = "Suburban Railway Tips",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ForestInk.copy(alpha = 0.6f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            facts.forEach { fact ->
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.2.dp, ForestInk), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Parchment),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(fact.bg)
                                    .border(BorderStroke(1.dp, fact.accent.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = fact.tag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = fact.accent
                                )
                            }

                            Icon(
                                imageVector = fact.icon,
                                contentDescription = fact.title,
                                tint = fact.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = fact.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestInk
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = fact.description,
                            fontSize = 11.sp,
                            color = Color(0xFF444444),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

private data class FactCardData(
    val id: String,
    val title: String,
    val description: String,
    val tag: String,
    val icon: ImageVector,
    val accent: Color,
    val bg: Color
)
