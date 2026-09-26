package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestInk
import com.example.ui.theme.Marigold
import com.example.ui.theme.Parchment
import com.example.ui.theme.SunlitCream

data class VendorTutorialStep(
    val stepIndex: Int,
    val title: String,
    val headline: String,
    val explanation: String,
    val icon: ImageVector,
    val visualSimulation: String
)

private val TUTORIAL_STEPS = listOf(
    VendorTutorialStep(
        stepIndex = 1,
        title = "Step 1 of 5: Order Arrives",
        headline = "Passenger signals hunger from their seat",
        explanation = "When a commuter on your train wants Chai, Jhalmuri, or snacks, their request appears instantly in your 'New Requests' tab with sound and gentle vibration.",
        icon = Icons.Default.NotificationsActive,
        visualSimulation = "🔔 NEW SIGNAL: Coach GS-2 needs 2x Masala Chai"
    ),
    VendorTutorialStep(
        stepIndex = 2,
        title = "Step 2 of 5: Tap to View Details",
        headline = "Check item, quantity, coach, and seat",
        explanation = "Verify what food is needed and where the passenger is seated. You can see their exact coach (e.g., GS-2) and seat note so you don't wander through the wrong compartment.",
        icon = Icons.Default.Fastfood,
        visualSimulation = "📋 DETAILS: Item: Masala Chai • Qty: 2 • Target: Coach GS-2 • Seat: Window 42"
    ),
    VendorTutorialStep(
        stepIndex = 3,
        title = "Step 3 of 5: Accept or Reject",
        headline = "Only accept if you can serve on time",
        explanation = "If you have fresh stock and can walk to that coach before the train arrives at the station, tap ACCEPT. If you are busy or out of stock, tap REJECT so another vendor can serve them.",
        icon = Icons.Default.Storefront,
        visualSimulation = "✅ Tap [ACCEPT] to claim or [REJECT] if you cannot serve"
    ),
    VendorTutorialStep(
        stepIndex = 4,
        title = "Step 4 of 5: Confirm Fair Price",
        headline = "Transparent, official railway pricing",
        explanation = "Select the standard unit price (₹10, ₹15, ₹20, etc.). The passenger sees the total before confirming. Zero unexpected price hikes or disputes!",
        icon = Icons.Default.Paid,
        visualSimulation = "💰 PRICE CONFIRMATION: Unit Price ₹10 × 2 = Total ₹20"
    ),
    VendorTutorialStep(
        stepIndex = 5,
        title = "Step 5 of 5: Deliver & Collect",
        headline = "Hand over food and record sale",
        explanation = "Walk to the passenger's seat, hand over the warm food, and collect cash or show your UPI QR code. Tap 'Deliver & Collect' to record the revenue in your daily register!",
        icon = Icons.Default.CheckCircle,
        visualSimulation = "🎉 DELIVERED: ₹20 collected • Added to Today's Turnover"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorInteractiveTutorialDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = TUTORIAL_STEPS[currentStepIndex]
    val isLastStep = currentStepIndex == TUTORIAL_STEPS.size - 1

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SunlitCream,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("vendor_interactive_tutorial_dialog")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Marigold)
                            .border(BorderStroke(1.dp, ForestInk), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Tutorial",
                            tint = ForestInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Vendor Training Guide",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestInk
                        )
                        Text(
                            text = step.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestInk.copy(alpha = 0.8f)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_vendor_tutorial_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ForestInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TUTORIAL_STEPS.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (idx <= currentStepIndex) ForestInk else Parchment)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Content Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Parchment),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, ForestInk)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Marigold)
                                .border(BorderStroke(1.dp, ForestInk), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = step.headline,
                                tint = ForestInk,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = step.headline,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestInk
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = step.explanation,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = ForestInk
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Visual Mock / Simulation Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SunlitCream)
                            .border(BorderStroke(1.dp, ForestInk.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = step.visualSimulation,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestInk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStepIndex > 0) {
                    OutlinedButton(
                        onClick = { currentStepIndex-- },
                        border = BorderStroke(1.5.dp, ForestInk),
                        shape = RoundedCornerShape(1440.dp),
                        modifier = Modifier.testTag("vendor_tutorial_prev_btn")
                    ) {
                        Text("Previous", color = ForestInk, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.5.dp, ForestInk),
                        shape = RoundedCornerShape(1440.dp),
                        modifier = Modifier.testTag("vendor_tutorial_skip_btn")
                    ) {
                        Text("Skip Tutorial", color = ForestInk, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (isLastStep) {
                            onDismiss()
                        } else {
                            currentStepIndex++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestInk),
                    shape = RoundedCornerShape(1440.dp),
                    modifier = Modifier.testTag("vendor_tutorial_next_btn")
                ) {
                    Text(
                        text = if (isLastStep) "Complete Tutorial" else "Next Step (${currentStepIndex + 1}/5)",
                        color = SunlitCream,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
