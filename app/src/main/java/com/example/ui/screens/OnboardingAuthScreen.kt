package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IndianLanguage
import com.example.data.model.UserRole
import com.example.ui.localization.LocalizationManager
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NatureGreen
import com.example.ui.theme.RailNavy
import com.example.ui.theme.TerracottaAmber
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSandBackground
import com.example.ui.theme.WarmSurface
import kotlinx.coroutines.launch

@Composable
fun OnboardingAuthScreen(
    currentLanguage: IndianLanguage,
    onLanguageSelect: (IndianLanguage) -> Unit,
    onComplete: (name: String, phone: String, role: UserRole, lang: IndianLanguage, isSenior: Boolean, train: String, coach: String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.TRAVELER) }
    var selectedLang by remember { mutableStateOf(currentLanguage) }
    var seniorMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var selectedTrain by remember { mutableStateOf("31821 Sealdah - Ranaghat Local") }
    var selectedCoach by remember { mutableStateOf("C-4") }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

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
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // App Emblem
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(RailNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsTransit,
                    contentDescription = "RailSathi Emblem",
                    tint = TerracottaAmber,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = LocalizationManager.getString("app_title", selectedLang),
                fontSize = if (seniorMode) 28.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = RailNavy,
                textAlign = TextAlign.Center
            )

            Text(
                text = LocalizationManager.getString("app_tagline", selectedLang),
                fontSize = if (seniorMode) 15.sp else 13.sp,
                color = CharcoalTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Language Selector Chips
            Text(
                text = LocalizationManager.getString("language_select", selectedLang),
                fontSize = if (seniorMode) 16.sp else 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalText,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Indian Languages row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    IndianLanguage.HINDI,
                    IndianLanguage.BENGALI,
                    IndianLanguage.MARATHI,
                    IndianLanguage.TAMIL,
                    IndianLanguage.ENGLISH
                ).forEach { lang ->
                    val isSelected = selectedLang == lang
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) RailNavy else WarmSurface)
                            .border(1.dp, if (isSelected) RailNavy else WarmBorder, RoundedCornerShape(20.dp))
                            .clickable {
                                selectedLang = lang
                                onLanguageSelect(lang)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("lang_chip_${lang.code}")
                    ) {
                        Text(
                            text = lang.nativeName,
                            color = if (isSelected) Color.White else CharcoalText,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Row 2 of Indian languages
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    IndianLanguage.TELUGU,
                    IndianLanguage.GUJARATI,
                    IndianLanguage.KANNADA,
                    IndianLanguage.ODIA,
                    IndianLanguage.PUNJABI,
                    IndianLanguage.MALAYALAM
                ).forEach { lang ->
                    val isSelected = selectedLang == lang
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) RailNavy else WarmSurface)
                            .border(1.dp, if (isSelected) RailNavy else WarmBorder, RoundedCornerShape(20.dp))
                            .clickable {
                                selectedLang = lang
                                onLanguageSelect(lang)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = lang.nativeName,
                            color = if (isSelected) Color.White else CharcoalText,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Senior Citizen Switch Card
            Card(
                colors = CardDefaults.cardColors(containerColor = if (seniorMode) Color(0xFFFEF3C7) else WarmSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (seniorMode) GoldYellow else WarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Elderly,
                            contentDescription = "Senior Mode",
                            tint = if (seniorMode) GoldYellow else CharcoalTextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = LocalizationManager.getString("senior_mode", selectedLang),
                                fontSize = if (seniorMode) 16.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                text = LocalizationManager.getString("senior_mode_subtitle", selectedLang),
                                fontSize = 12.sp,
                                color = CharcoalTextMuted
                            )
                        }
                    }

                    Switch(
                        checked = seniorMode,
                        onCheckedChange = { seniorMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GoldYellow
                        ),
                        modifier = Modifier.testTag("senior_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Role Selection Header
            Text(
                text = LocalizationManager.getString("role_select_title", selectedLang),
                fontSize = if (seniorMode) 18.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalText,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 1: Train Passenger
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRole = UserRole.TRAVELER }
                    .testTag("role_traveler_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedRole == UserRole.TRAVELER) Color(0xFFEFF6FF) else WarmSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (selectedRole == UserRole.TRAVELER) 2.dp else 1.dp,
                    if (selectedRole == UserRole.TRAVELER) RailNavy else Color(0xFFCBD5E1)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (selectedRole == UserRole.TRAVELER) RailNavy else Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Passenger",
                            tint = if (selectedRole == UserRole.TRAVELER) Color.White else CharcoalText
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LocalizationManager.getString("role_passenger", selectedLang),
                            fontSize = if (seniorMode) 17.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = LocalizationManager.getString("role_passenger_desc", selectedLang),
                            fontSize = 12.sp,
                            color = CharcoalTextMuted
                        )
                    }

                    if (selectedRole == UserRole.TRAVELER) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = RailNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Train Vendor / Hawker
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRole = UserRole.VENDOR }
                    .testTag("role_vendor_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedRole == UserRole.VENDOR) Color(0xFFFFF7ED) else WarmSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (selectedRole == UserRole.VENDOR) 2.dp else 1.dp,
                    if (selectedRole == UserRole.VENDOR) TerracottaAmber else Color(0xFFCBD5E1)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (selectedRole == UserRole.VENDOR) TerracottaAmber else Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Vendor",
                            tint = if (selectedRole == UserRole.VENDOR) Color.White else CharcoalText
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LocalizationManager.getString("role_vendor", selectedLang),
                            fontSize = if (seniorMode) 17.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = LocalizationManager.getString("role_vendor_desc", selectedLang),
                            fontSize = 12.sp,
                            color = CharcoalTextMuted
                        )
                    }

                    if (selectedRole == UserRole.VENDOR) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = TerracottaAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Profile Fields Container (Darker borders, high-contrast text and keyboard auto-scroll)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF94A3B8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (selectedRole == UserRole.VENDOR) "Vendor Details • विक्रेता विवरण" else "Passenger Details • यात्री विवरण",
                        fontSize = if (seniorMode) 16.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val activeAccent = if (selectedRole == UserRole.VENDOR) TerracottaAmber else RailNavy

                    // Name Input
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = {
                            Text(
                                if (selectedRole == UserRole.VENDOR) "Vendor Name (फेरीवाले का नाम)" else "Passenger Name (यात्री का नाम)",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        placeholder = {
                            Text(
                                if (selectedRole == UserRole.VENDOR) "e.g. Subhash Da" else "e.g. Aniket Sharma",
                                color = Color(0xFF64748B)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (selectedRole == UserRole.VENDOR) Icons.Default.Storefront else Icons.Default.Person,
                                contentDescription = "Name Icon",
                                tint = activeAccent
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = CharcoalText,
                            fontSize = if (seniorMode) 17.sp else 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText,
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            cursorColor = activeAccent,
                            focusedBorderColor = activeAccent,
                            unfocusedBorderColor = Color(0xFF64748B),
                            focusedLabelColor = activeAccent,
                            unfocusedLabelColor = Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                }
                            }
                            .testTag("input_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone Input
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = {
                            Text(
                                "Mobile Number (मोबाईल नंबर)",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        placeholder = {
                            Text(
                                "e.g. 9876543210",
                                color = Color(0xFF64748B)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone Icon",
                                tint = activeAccent
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = CharcoalText,
                            fontSize = if (seniorMode) 17.sp else 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText,
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            cursorColor = activeAccent,
                            focusedBorderColor = activeAccent,
                            unfocusedBorderColor = Color(0xFF64748B),
                            focusedLabelColor = activeAccent,
                            unfocusedLabelColor = Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                }
                            }
                            .testTag("input_phone")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    onComplete(
                        nameInput,
                        phoneInput,
                        selectedRole,
                        selectedLang,
                        seniorMode,
                        selectedTrain,
                        selectedCoach
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (seniorMode) 56.dp else 50.dp)
                    .testTag("get_started_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedRole == UserRole.VENDOR) TerracottaAmber else RailNavy
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Get Started • आगे बढ़ें",
                    fontSize = if (seniorMode) 18.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Guest access button
            TextButton(
                onClick = {
                    onComplete(
                        "Guest Passenger",
                        "9999999999",
                        UserRole.GUEST,
                        selectedLang,
                        seniorMode,
                        selectedTrain,
                        selectedCoach
                    )
                },
                modifier = Modifier.testTag("guest_btn")
            ) {
                Text(
                    text = LocalizationManager.getString("role_guest", selectedLang),
                    color = CharcoalTextMuted,
                    fontSize = if (seniorMode) 15.sp else 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

