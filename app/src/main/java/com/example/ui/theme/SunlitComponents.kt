package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Botanical Neobrutalist Design Tokens from Style Reference:
 * - 40.dp rounded cards
 * - 1440.dp pill buttons & tags
 * - Hard 6.dp to 8.dp offset shadow in Forest Ink (#10380b)
 * - Sunlit Cream (#F2EE98), Parchment (#FEFDE6), Marigold (#FCE519)
 */
val SunlitPillShape = RoundedCornerShape(1440.dp)
val SunlitCardShape = RoundedCornerShape(24.dp)
val SunlitSmallPillShape = RoundedCornerShape(1440.dp)

/**
 * Neobrutalist Stamp Card with hard offset shadow in Forest Ink.
 */
@Composable
fun SunlitStampedCard(
    modifier: Modifier = Modifier,
    shape: Shape = SunlitCardShape,
    containerColor: Color = Parchment,
    borderColor: Color = ForestInk,
    shadowColor: Color = ForestInk,
    shadowOffset: Dp = 6.dp,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        // Shadow Stamp Layer underneath
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(color = shadowColor, shape = shape)
        )
        // Main Foreground Card
        Surface(
            modifier = Modifier
                .border(BorderStroke(1.5.dp, borderColor), shape = shape),
            shape = shape,
            color = containerColor
        ) {
            content()
        }
    }
}

/**
 * Marigold Pill Primary Action CTA
 */
@Composable
fun MarigoldPillButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = SunlitPillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Marigold,
            contentColor = ForestInk,
            disabledContainerColor = SageWash,
            disabledContentColor = ForestInk.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        border = BorderStroke(1.5.dp, ForestInk),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = ForestInk,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * Forest Ink Pill Action Button
 */
@Composable
fun ForestPillButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = SunlitPillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ForestInk,
            contentColor = SunlitCream,
            disabledContainerColor = ForestInk.copy(alpha = 0.4f),
            disabledContentColor = SunlitCream.copy(alpha = 0.7f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        border = BorderStroke(1.dp, ForestInk),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = SunlitCream,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * Outlined Botanical Pill Button
 */
@Composable
fun OutlinedPillButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        shape = SunlitPillShape,
        border = BorderStroke(1.5.dp, ForestInk),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = ForestInk
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = ForestInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Botanical Status Pill Badge
 */
@Composable
fun SunlitStatusPill(
    text: String,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) Marigold else SageWash
    val textColor = ForestInk
    Box(
        modifier = modifier
            .clip(SunlitPillShape)
            .background(bgColor)
            .border(BorderStroke(1.dp, ForestInk), shape = SunlitPillShape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Botanical 5-Star Rating Row
 */
@Composable
fun BotanicalStarRating(
    rating: Int = 5,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star",
                tint = if (index < rating) Marigold else SageWash,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
