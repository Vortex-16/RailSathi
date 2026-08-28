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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
    onLanguageSelect: (IndianLanguage) -> Unit = {},
    onCompleteAuth: (role: UserRole, lang: IndianLanguage, googleEmail: String?, googleName: String?) -> Unit = { _, _, _, _ -> }
) {
    var currentStep by remember { mutableStateOf(1) } // 1, 2, or 3
    var selectedLang by remember { mutableStateOf(currentLanguage) }
    var selectedRole by remember { mutableStateOf(UserRole.TRAVELER) }
    var isSigningIn by remember { mutableStateOf(false) }

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
            // Top Bar: Back & Step Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = { currentStep -= 1 },
                        modifier = Modifier.testTag("onboarding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RailNavy
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Page Indicator: 1 / 3, 2 / 3, 3 / 3
                Text(
                    text = "$currentStep / 3",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalTextMuted,
                    modifier = Modifier.testTag("onboarding_page_indicator")
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Carousel for 3 Screens
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
                    label = "onboarding_step_anim"
                ) { step ->
                    when (step) {
                        1 -> Screen1Introduction()
                        2 -> Screen2LanguageSelection(
                            selected = selectedLang,
                            onSelect = {
                                selectedLang = it
                                onLanguageSelect(it)
                            }
                        )
                        else -> Screen3AuthAndRole(
                            selectedRole = selectedRole,
                            onRoleSelect = { selectedRole = it },
                            isSigningIn = isSigningIn,
                            onGoogleSignIn = { email, name ->
                                isSigningIn = true
                                onCompleteAuth(selectedRole, selectedLang, email, name)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Navigation Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (currentStep) {
                    1 -> {
                        Button(
                            onClick = { currentStep = 2 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_step1_continue"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RailNavy)
                        ) {
                            Text(
                                text = "Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    2 -> {
                        Button(
                            onClick = {
                                onLanguageSelect(selectedLang)
                                currentStep = 3
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_step2_continue"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RailNavy)
                        ) {
                            Text(
                                text = "Continue with ${selectedLang.englishName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    3 -> {
                        // Screen 3 contains its own primary Google sign-in actions
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 1 — Existing Introduction
// -------------------------------------------------------------
@Composable
private fun Screen1Introduction() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visual Brand Badges
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(RailNavy.copy(alpha = 0.1f)),
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
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(TerracottaAmber.copy(alpha = 0.15f)),
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
                    .size(56.dp)
                    .clip(CircleShape)
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

        Text(
            text = "RailSaathi",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = RailNavy,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your journey, made easier.",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = CharcoalTextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                IntroFeatureRow(
                    icon = Icons.Default.DirectionsTransit,
                    iconTint = RailNavy,
                    text = "Find your train and platform."
                )

                IntroFeatureRow(
                    icon = Icons.Default.Fastfood,
                    iconTint = TerracottaAmber,
                    text = "Discover fresh local station food."
                )

                IntroFeatureRow(
                    icon = Icons.Default.Storefront,
                    iconTint = Color(0xFF16A34A),
                    text = "Connect directly with nearby vendors."
                )
            }
        }
    }
}

@Composable
private fun IntroFeatureRow(
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

// -------------------------------------------------------------
// SCREEN 2 — Language Selection
// -------------------------------------------------------------
@Composable
private fun Screen2LanguageSelection(
    selected: IndianLanguage,
    onSelect: (IndianLanguage) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Select Language",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RailNavy
        )
        Text(
            text = "Choose your preferred language for RailSaathi",
            fontSize = 14.sp,
            color = CharcoalTextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IndianLanguage.values().forEach { lang ->
                val isChosen = lang == selected
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelect(lang) }
                        .testTag("lang_option_${lang.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChosen) RailNavy.copy(alpha = 0.08f) else WarmSurface
                    ),
                    border = BorderStroke(
                        width = if (isChosen) 2.dp else 1.dp,
                        color = if (isChosen) RailNavy else WarmBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = lang.englishName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen) RailNavy else CharcoalText
                            )
                            Text(
                                text = lang.nativeName,
                                fontSize = 14.sp,
                                color = if (isChosen) RailNavy.copy(alpha = 0.8f) else CharcoalTextMuted
                            )
                        }

                        if (isChosen) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(RailNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 3 — Authentication + Role
// -------------------------------------------------------------
@Composable
private fun Screen3AuthAndRole(
    selectedRole: UserRole,
    onRoleSelect: (UserRole) -> Unit,
    isSigningIn: Boolean,
    onGoogleSignIn: (email: String?, name: String?) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Welcome to RailSaathi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = RailNavy
        )
        Text(
            text = "Select your role and sign in to get started",
            fontSize = 14.sp,
            color = CharcoalTextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Role Selection Header
        Text(
            text = "Select Role",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalText,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // 2 Roles: Traveler & Vendor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Traveler Role Card
            RoleCard(
                title = "Traveler",
                description = "Daily commuter & train passenger",
                icon = Icons.Default.Person,
                isSelected = selectedRole == UserRole.TRAVELER,
                modifier = Modifier
                    .weight(1f)
                    .testTag("role_option_traveler"),
                onSelect = { onRoleSelect(UserRole.TRAVELER) }
            )

            // Vendor Role Card
            RoleCard(
                title = "Vendor",
                description = "Station & coach snack vendor",
                icon = Icons.Default.Storefront,
                isSelected = selectedRole == UserRole.VENDOR,
                modifier = Modifier
                    .weight(1f)
                    .testTag("role_option_vendor"),
                onSelect = { onRoleSelect(UserRole.VENDOR) }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Authentication Block
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Sign in to save your commute profile & offline orders",
                    fontSize = 13.sp,
                    color = CharcoalTextMuted,
                    textAlign = TextAlign.Center
                )

                // Google Sign In Primary Button
                Button(
                    onClick = {
                        val email = if (selectedRole == UserRole.VENDOR) "vendor.demo@railsaathi.in" else "commuter.demo@railsaathi.in"
                        val name = if (selectedRole == UserRole.VENDOR) "Station Vendor" else "Daily Commuter"
                        onGoogleSignIn(email, name)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_google_signin_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                    enabled = !isSigningIn
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isSigningIn) "Signing in..." else "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Quick Guest Commuter option
                OutlinedButton(
                    onClick = {
                        onGoogleSignIn(null, if (selectedRole == UserRole.VENDOR) "Station Vendor" else "Daily Commuter")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("onboarding_guest_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, WarmBorder)
                ) {
                    Text(
                        text = "Continue as Guest",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalText
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RailNavy.copy(alpha = 0.08f) else WarmSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) RailNavy else WarmBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) RailNavy else CharcoalTextMuted.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else CharcoalText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) RailNavy else CharcoalText
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = CharcoalTextMuted,
                lineHeight = 16.sp
            )
        }
    }
}
