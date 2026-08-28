package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IndianLanguage
import com.example.data.model.UserRole
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.RailNavy
import com.example.ui.theme.TerracottaAmber
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSandBackground
import com.example.ui.theme.WarmSurface

@Composable
fun OnboardingAuthScreen(
    currentLanguage: IndianLanguage = IndianLanguage.ENGLISH,
    onLanguageSelect: ((IndianLanguage) -> Unit)? = null,
    onComplete: (name: String, phone: String, role: UserRole, lang: IndianLanguage, isSenior: Boolean, train: String, coach: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onSimpleComplete: (() -> Unit)? = null,
    onDismissReplay: (() -> Unit)? = null
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val isReplayMode = onDismissReplay != null

    fun handleFinish() {
        if (isReplayMode) {
            onDismissReplay?.invoke()
        } else if (onSimpleComplete != null) {
            onSimpleComplete.invoke()
        } else {
            onComplete(
                "Daily Commuter",
                "9876543210",
                UserRole.TRAVELER,
                currentLanguage,
                false,
                "31821 Sealdah - Ranaghat Local",
                "C-4"
            )
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = WarmSandBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Top Bar: Back / Close & Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep == 2) {
                    IconButton(
                        onClick = { currentStep = 1 },
                        modifier = Modifier.testTag("onboarding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Step 1",
                            tint = RailNavy
                        )
                    }
                } else if (isReplayMode) {
                    IconButton(
                        onClick = { onDismissReplay?.invoke() },
                        modifier = Modifier.testTag("onboarding_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tutorial",
                            tint = CharcoalTextMuted
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                if (!isReplayMode) {
                    TextButton(
                        onClick = { handleFinish() },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip",
                            color = CharcoalTextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Carousel (Screen 1 vs Screen 2)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "onboarding_screen_animation"
                ) { step ->
                    if (step == 1) {
                        OnboardingScreenOneContent()
                    } else {
                        OnboardingScreenTwoContent()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Actions & Page Indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentStep == 1) {
                    Button(
                        onClick = { currentStep = 2 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_get_started_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RailNavy)
                    ) {
                        Text(
                            text = "Get Started",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Button(
                        onClick = { handleFinish() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_start_using_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RailNavy)
                    ) {
                        Text(
                            text = if (isReplayMode) "Got it" else "Start Using RailSaathi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Minimal Page Indicator: 1 / 2 or 2 / 2
                Text(
                    text = "$currentStep / 2",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = CharcoalTextMuted,
                    modifier = Modifier.testTag("onboarding_page_indicator")
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun OnboardingScreenOneContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Subtle, clean visual: Train + Passenger + Food
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(WarmSurface)
                    .background(RailNavy.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsTransit,
                    contentDescription = "Train",
                    tint = RailNavy,
                    modifier = Modifier.size(28.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(WarmSurface)
                    .background(TerracottaAmber.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Passenger",
                    tint = TerracottaAmber,
                    modifier = Modifier.size(28.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(WarmSurface)
                    .background(Color(0xFF16A34A).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = "Food",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Title
        Text(
            text = "RailSaathi",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = RailNavy,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle
        Text(
            text = "Your journey, made easier.",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = CharcoalTextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3 Key Bullets in a clean card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            border = BorderStroke(1.dp, WarmBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureBulletRow(
                    icon = Icons.Default.DirectionsTransit,
                    iconTint = RailNavy,
                    text = "Find your train."
                )

                FeatureBulletRow(
                    icon = Icons.Default.Fastfood,
                    iconTint = TerracottaAmber,
                    text = "Discover local food."
                )

                FeatureBulletRow(
                    icon = Icons.Default.Storefront,
                    iconTint = Color(0xFF16A34A),
                    text = "Connect with nearby vendors."
                )
            }
        }
    }
}

@Composable
private fun FeatureBulletRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = CharcoalText
        )
    }
}

@Composable
private fun OnboardingScreenTwoContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "How RailSaathi works",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RailNavy,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmSurface),
            border = BorderStroke(1.dp, WarmBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Step 01
                StepItemRow(
                    number = "01",
                    title = "Start your journey",
                    description = "Select the train you're travelling on."
                )

                // Step 02
                StepItemRow(
                    number = "02",
                    title = "Find what you need",
                    description = "Browse food and nearby vendors."
                )

                // Step 03
                StepItemRow(
                    number = "03",
                    title = "Request & confirm",
                    description = "Choose quantity. The vendor confirms the price."
                )
            }
        }
    }
}

@Composable
private fun StepItemRow(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TerracottaAmber,
            modifier = Modifier.padding(top = 1.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = CharcoalTextMuted,
                lineHeight = 18.sp
            )
        }
    }
}
