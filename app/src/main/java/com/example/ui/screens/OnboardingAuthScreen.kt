package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.auth.AuthManager
import com.example.data.model.IndianLanguage
import com.example.data.model.UserRole
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
    onCompleteAuth: (
        role: UserRole,
        lang: IndianLanguage,
        googleEmail: String?,
        googleName: String?,
        googleIdToken: String?,
        googleId: String?,
        photoUrl: String?
    ) -> Unit = { _, _, _, _, _, _, _ -> }
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
                            onGoogleSignIn = { email, name, idToken, googleId, photoUrl ->
                                isSigningIn = true
                                onCompleteAuth(selectedRole, selectedLang, email, name, idToken, googleId, photoUrl)
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
                        ForestPillButton(
                            onClick = { currentStep = 2 },
                            text = "Continue",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("onboarding_step1_continue")
                        )
                    }
                    2 -> {
                        ForestPillButton(
                            onClick = {
                                onLanguageSelect(selectedLang)
                                currentStep = 3
                            },
                            text = "Continue with ${selectedLang.englishName}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("onboarding_step2_continue")
                        )
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
        // Textla Botanical Emblem: Sunlit Stamped Emblem with Forest Ink border
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Marigold)
                .border(BorderStroke(2.dp, ForestInk), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsTransit,
                contentDescription = "RailSathi Logo",
                tint = ForestInk,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "RailSathi",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your journey, made easier.",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = CharcoalTextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Textla 5-Star Social Proof Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotanicalStarRating(rating = 5)
            Text(
                text = "Trusted by Daily Commuters",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ForestInk
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Textla Stamped Card with 6.dp hard offset shadow in Forest Ink
        SunlitStampedCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Parchment,
            borderColor = ForestInk,
            shadowOffset = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IntroFeatureRow(
                    icon = Icons.Default.DirectionsTransit,
                    iconTint = ForestInk,
                    text = "Find your train and platform."
                )

                IntroFeatureRow(
                    icon = Icons.Default.Fastfood,
                    iconTint = ForestInk,
                    text = "Discover fresh local station food."
                )

                IntroFeatureRow(
                    icon = Icons.Default.Storefront,
                    iconTint = ForestInk,
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
                .size(40.dp)
                .clip(CircleShape)
                .background(Marigold)
                .border(BorderStroke(1.5.dp, ForestInk), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ForestInk,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = ForestInk
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
            fontWeight = FontWeight.ExtraBold,
            color = ForestInk
        )
        Text(
            text = "Choose your preferred language for RailSathi",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ForestInk.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IndianLanguage.values().forEach { lang ->
                val isChosen = lang == selected
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(1440.dp))
                        .background(if (isChosen) Marigold else Parchment)
                        .border(
                            width = if (isChosen) 2.dp else 1.dp,
                            color = ForestInk,
                            shape = RoundedCornerShape(1440.dp)
                        )
                        .clickable { onSelect(lang) }
                        .testTag("lang_option_${lang.name.lowercase()}"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = lang.englishName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForestInk
                            )
                            Text(
                                text = lang.nativeName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChosen) ForestInk else CharcoalTextMuted
                            )
                        }

                        if (isChosen) {
                            Box(
                                modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ForestInk),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = SunlitCream,
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
    onGoogleSignIn: (email: String?, name: String?, idToken: String?, googleId: String?, photoUrl: String?) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager(context) }

    var customNameInput by remember { mutableStateOf("") }
    var customEmailInput by remember { mutableStateOf("") }
    var isSigningInState by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Welcome to RailSathi",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestInk
        )
        Text(
            text = "Select your role and sign in to get started",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ForestInk.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Role Selection Header
        Text(
            text = "Select Role",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestInk,
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

        Spacer(modifier = Modifier.height(24.dp))

        // Authentication Block: Sunlit Stamped Card with Forest Ink shadow
        SunlitStampedCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Parchment,
            borderColor = ForestInk,
            shadowOffset = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Sign in to securely link your profile & sync your train preferences",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = CharcoalTextMuted,
                    textAlign = TextAlign.Center
                )

                // Google Sign In Primary Pill Button
                ForestPillButton(
                    onClick = {
                        isSigningInState = true
                        authErrorMessage = null
                        scope.launch {
                            try {
                                val result = authManager.signInWithGoogle(context)
                                if (result.success && !result.email.isNullOrBlank()) {
                                    onGoogleSignIn(
                                        result.email,
                                        result.displayName,
                                        result.idToken,
                                        result.googleId,
                                        result.photoUrl
                                    )
                                } else {
                                    authErrorMessage = result.errorMessage ?: "Google Sign-In unavailable. Enter details below to continue."
                                }
                            } catch (e: Exception) {
                                authErrorMessage = "Sign-in error: ${e.message}"
                            } finally {
                                isSigningInState = false
                            }
                        }
                    },
                    text = if (isSigningInState) "Connecting to Google..." else "Sign in with Google",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_google_signin_button"),
                    enabled = !isSigningInState
                )

                if (authErrorMessage != null) {
                    Text(
                        text = authErrorMessage ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB91C1C),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ForestInk.copy(alpha = 0.2f))
                    Text(
                        text = " OR ENTER DETAILS ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ForestInk,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = ForestInk.copy(alpha = 0.2f))
                }

                // Custom Name Input
                OutlinedTextField(
                    value = customNameInput,
                    onValueChange = { customNameInput = it },
                    label = { Text("Your Full Name") },
                    placeholder = { Text(if (selectedRole == UserRole.VENDOR) "e.g. Ramesh Kumar" else "e.g. Amit Sen") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(1440.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ForestInk,
                        unfocusedTextColor = ForestInk,
                        focusedContainerColor = SunlitCream,
                        unfocusedContainerColor = SunlitCream,
                        cursorColor = ForestInk,
                        focusedBorderColor = ForestInk,
                        unfocusedBorderColor = ForestInk.copy(alpha = 0.5f)
                    )
                )

                // Custom Email/Phone Input
                OutlinedTextField(
                    value = customEmailInput,
                    onValueChange = { customEmailInput = it },
                    label = { Text("Email or Mobile Number") },
                    placeholder = { Text("e.g. yourname@gmail.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(1440.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ForestInk,
                        unfocusedTextColor = ForestInk,
                        focusedContainerColor = SunlitCream,
                        unfocusedContainerColor = SunlitCream,
                        cursorColor = ForestInk,
                        focusedBorderColor = ForestInk,
                        unfocusedBorderColor = ForestInk.copy(alpha = 0.5f)
                    )
                )

                // Sign In with details button
                if (customNameInput.isNotBlank() || customEmailInput.isNotBlank()) {
                    MarigoldPillButton(
                        onClick = {
                            val cleanName = customNameInput.trim().ifBlank { if (selectedRole == UserRole.VENDOR) "Station Vendor" else "Daily Commuter" }
                            val cleanEmail = customEmailInput.trim().ifBlank { null }
                            val genId = "usr_${System.currentTimeMillis()}"
                            onGoogleSignIn(cleanEmail, cleanName, "token_direct", genId, null)
                        },
                        text = "Continue with My Details",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )
                }

                // Quick Guest Commuter option
                OutlinedPillButton(
                    onClick = {
                        val defaultTitle = if (selectedRole == UserRole.VENDOR) "Station Vendor" else "Daily Commuter"
                        onGoogleSignIn(null, defaultTitle, null, null, null)
                    },
                    text = "Continue as Guest",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("onboarding_guest_button")
                )
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Marigold else Parchment)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = ForestInk,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onSelect() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) ForestInk else SageWash)
                    .border(BorderStroke(1.dp, ForestInk), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) SunlitCream else ForestInk,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ForestInk
            )

            Text(
                text = description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = CharcoalTextMuted,
                lineHeight = 16.sp
            )
        }
    }
}
