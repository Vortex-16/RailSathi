package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.data.repository.CollisionCheckResult
import com.example.ui.components.BannerNotificationToast
import com.example.ui.components.CollisionWarningDialog
import com.example.ui.components.RailAppTopBar
import com.example.ui.components.RailBottomNavBar
import com.example.ui.components.TrainDiagnosticsDialog
import com.example.ui.screens.BudgetExpenseScreen
import com.example.ui.screens.CoachRadarScreen
import com.example.ui.screens.OnboardingAuthScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.TravelerHomeScreen
import com.example.ui.screens.VendorHomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RailSathiApp()
            }
        }
    }
}

@Composable
fun RailSathiApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val isOnboardingDone by viewModel.isOnboardingDone.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isSeniorMode by viewModel.isSeniorMode.collectAsState()
    val activeNavTab by viewModel.activeNavTab.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val selectedCoach by viewModel.selectedCoach.collectAsState()
    val etaSeconds by viewModel.activeStationEta.collectAsState()
    val locationInfo by viewModel.locationState.collectAsState()

    // Engine & Journey state flows
    val journeySession by viewModel.journeySession.collectAsState()
    val contextState by viewModel.contextState.collectAsState()
    val stationCandidates by viewModel.stationCandidates.collectAsState()
    val selectedCandidate by viewModel.selectedCandidate.collectAsState()
    val confidenceScore by viewModel.confidenceScore.collectAsState()
    val confidenceDescription by viewModel.confidenceDescription.collectAsState()
    val regularCommute by viewModel.regularCommute.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchedTrains by viewModel.searchedTrains.collectAsState()
    val selectedQuantities by viewModel.selectedQuantities.collectAsState()

    val activeUser by viewModel.activeUser.collectAsState()
    val activeRequests by viewModel.activeRequests.collectAsState()
    val allVendors by viewModel.allVendors.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val selectedVendorId by viewModel.selectedVendorId.collectAsState()
    val collisionResult by viewModel.collisionResult.collectAsState()
    val alertBanner by viewModel.alertBanner.collectAsState()
    val isReplayingTutorial by viewModel.isReplayingTutorial.collectAsState()
    val journeyHintShown by viewModel.journeyHintShown.collectAsState()
    val foodHintShown by viewModel.foodHintShown.collectAsState()
    val requestHintShown by viewModel.requestHintShown.collectAsState()
    val vendorHintShown by viewModel.vendorHintShown.collectAsState()

    var showDiagnosticsDialog by remember { mutableStateOf(false) }

    // Permission launcher for Location and Notifications (Contextual)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.onLocationPermissionResult(fineGranted || coarseGranted)
    }

    // Contextual permission requester helper
    fun checkAndRequestLocationPermission(onProceed: () -> Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            viewModel.onLocationPermissionResult(true)
        }
        onProceed()
    }

    val activeVendor = allVendors.find { it.vendorId == selectedVendorId } ?: allVendors.firstOrNull()

    if (!isOnboardingDone || isReplayingTutorial) {
        OnboardingAuthScreen(
            currentLanguage = currentLanguage,
            onLanguageSelect = { viewModel.setLanguage(it) },
            onCompleteAuth = { role, lang, email, name, idToken, googleId, photoUrl ->
                viewModel.completeOnboardingWithGoogle(role, lang, email, name, idToken, googleId, photoUrl)
                if (isReplayingTutorial) {
                    viewModel.finishTutorialReplay()
                }
            }
        )
    } else {
        Scaffold(
            topBar = {
                RailAppTopBar(
                    role = currentRole,
                    language = currentLanguage,
                    isSeniorMode = isSeniorMode,
                    journeySession = journeySession,
                    route = selectedRoute,
                    locationInfo = locationInfo,
                    etaSeconds = etaSeconds,
                    onLanguageChange = { viewModel.setLanguage(it) },
                    onToggleSeniorMode = { viewModel.toggleSeniorMode(it) },
                    onSwitchRole = {
                        viewModel.setNavTab(AppNavTab.PROFILE)
                    }
                )
            },
            bottomBar = {
                RailBottomNavBar(
                    activeTab = activeNavTab,
                    onTabSelected = { viewModel.setNavTab(it) },
                    language = currentLanguage,
                    role = currentRole,
                    isSeniorMode = isSeniorMode
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeNavTab) {
                    AppNavTab.HOME -> {
                        if (currentRole == UserRole.VENDOR) {
                            VendorHomeScreen(
                                vendor = activeVendor,
                                allVendors = allVendors,
                                activeRequests = activeRequests,
                                journeySession = journeySession,
                                selectedRoute = selectedRoute,
                                selectedCoach = selectedCoach,
                                locationInfo = locationInfo,
                                stationCandidates = stationCandidates,
                                language = currentLanguage,
                                isSeniorMode = isSeniorMode,
                                vendorHintShown = vendorHintShown,
                                onDismissVendorHint = { viewModel.dismissVendorHint() },
                                onSelectVendorProfile = { viewModel.setSelectedVendorId(it) },
                                onVerifyCoachBoarding = { vendorId, specId, coach ->
                                    viewModel.verifyAndBoardCoach(vendorId, specId, coach)
                                },
                                onStartShift = { candidate, coach ->
                                    checkAndRequestLocationPermission {
                                        viewModel.startJourney(candidate, coach)
                                    }
                                },
                                onEndShift = {
                                    viewModel.endJourney()
                                },
                                onAcceptAndOfferPrice = { req, v, price ->
                                    viewModel.vendorAcceptAndOfferPrice(req.id, price)
                                },
                                onDeliverSale = { req, v ->
                                    viewModel.vendorDeliverAndCollect(req, v)
                                },
                                onQuickManualSale = { v, item, amt, coach ->
                                    viewModel.recordManualSale(v, item, amt, coach)
                                }
                            )
                        } else {
                            TravelerHomeScreen(
                                language = currentLanguage,
                                isSeniorMode = isSeniorMode,
                                selectedCoach = selectedCoach,
                                onCoachSelect = { viewModel.setCoach(it) },
                                activeRequests = activeRequests,
                                selectedQuantities = selectedQuantities,
                                onQuantityChange = { itemId, delta ->
                                    viewModel.updateItemQuantity(itemId, delta)
                                },
                                journeySession = journeySession,
                                selectedRoute = selectedRoute,
                                locationInfo = locationInfo,
                                contextState = contextState,
                                nearbyStation = locationInfo.nearestStation,
                                stationCandidates = stationCandidates,
                                selectedCandidate = selectedCandidate,
                                confidenceScore = confidenceScore,
                                confidenceDescription = confidenceDescription,
                                regularCommute = regularCommute,
                                searchQuery = searchQuery,
                                searchedTrains = searchedTrains,
                                journeyHintShown = journeyHintShown,
                                foodHintShown = foodHintShown,
                                requestHintShown = requestHintShown,
                                onDismissJourneyHint = { viewModel.dismissJourneyHint() },
                                onDismissFoodHint = { viewModel.dismissFoodHint() },
                                onDismissRequestHint = { viewModel.dismissRequestHint() },
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onSelectCandidate = { viewModel.selectCandidate(it) },
                                onClearCandidate = { viewModel.clearCandidate() },
                                onStartJourney = { candidate, coach ->
                                    checkAndRequestLocationPermission {
                                        viewModel.startJourney(candidate, coach)
                                    }
                                },
                                onStartRegularCommute = {
                                    checkAndRequestLocationPermission {
                                        viewModel.startRegularCommuteJourney()
                                    }
                                },
                                onEndJourney = {
                                    viewModel.endJourney()
                                },
                                onSendHungerSignal = { item, note ->
                                    viewModel.sendHungerSignal(item, note)
                                },
                                onConfirmOrder = { reqId ->
                                    viewModel.customerConfirmOrder(reqId)
                                },
                                onCancelRequest = { viewModel.customerCancelOrder(it) },
                                onSimulateStation = { stationCode ->
                                    viewModel.simulateAtStation(stationCode)
                                }
                            )
                        }
                    }

                    AppNavTab.COACH_RADAR -> {
                        CoachRadarScreen(
                            role = currentRole,
                            selectedCoach = selectedCoach,
                            selectedRoute = selectedRoute,
                            allVendors = allVendors,
                            activeRequests = activeRequests,
                            language = currentLanguage,
                            isSeniorMode = isSeniorMode,
                            onSelectCoach = { viewModel.setCoach(it) },
                            onVendorBoardCoach = { coach ->
                                if (activeVendor != null) {
                                    viewModel.verifyAndBoardCoach(activeVendor.vendorId, activeVendor.specialityItemId, coach)
                                }
                            }
                        )
                    }

                    AppNavTab.BUDGET_LEDGER -> {
                        BudgetExpenseScreen(
                            role = currentRole,
                            user = activeUser,
                            vendor = activeVendor,
                            expenses = expenses,
                            totalSpent = totalSpent,
                            language = currentLanguage,
                            isSeniorMode = isSeniorMode,
                            onAddExpense = { title, cat, amt, coach, note ->
                                viewModel.addManualExpense(title, cat, amt, coach, note)
                            },
                            onUpdateBudgetLimit = { viewModel.updateMonthlyBudgetLimit(it) },
                            onResetBudget = { viewModel.resetMonthlyBudget() }
                        )
                    }

                    AppNavTab.PROFILE -> {
                        ProfileSettingsScreen(
                            role = currentRole,
                            user = activeUser,
                            language = currentLanguage,
                            isSeniorMode = isSeniorMode,
                            journeySession = journeySession,
                            selectedRoute = selectedRoute,
                            availableRoutes = viewModel.repository.availableRoutes,
                            locationInfo = locationInfo,
                            onSwitchRole = { viewModel.switchRole(it) },
                            onLanguageChange = { viewModel.setLanguage(it) },
                            onToggleSeniorMode = { viewModel.toggleSeniorMode(it) },
                            onRouteChange = { viewModel.setRoute(it) },
                            onReplayTutorial = { viewModel.replayTutorial() },
                            onSimulateStation = { stationCode ->
                                viewModel.simulateAtStation(stationCode)
                            },
                            onOpenDiagnostics = { showDiagnosticsDialog = true },
                            onUpdateProfile = { name, phone, lang, senior, bio, stn, route ->
                                viewModel.updateUserProfile(name, phone, lang, senior, bio, stn, route)
                            },
                            onLogoutAndClearData = { viewModel.logoutAndClearAppData() }
                        )
                    }
                }

                if (showDiagnosticsDialog) {
                    TrainDiagnosticsDialog(
                        diagnostics = viewModel.getDiagnosticsSnapshot(),
                        onDismiss = { showDiagnosticsDialog = false }
                    )
                }

                // In-App Notification Toast
                BannerNotificationToast(
                    message = alertBanner,
                    isSeniorMode = isSeniorMode
                )

                // Coach Collision Dialog (if conflict triggered)
                val collision = collisionResult
                if (collision is CollisionCheckResult.Conflict) {
                    CollisionWarningDialog(
                        collision = collision,
                        language = currentLanguage,
                        onDismiss = { viewModel.clearCollisionAlert() },
                        onAcceptAlternative = { altCoach ->
                            if (activeVendor != null) {
                                viewModel.verifyAndBoardCoach(activeVendor.vendorId, activeVendor.specialityItemId, altCoach)
                            }
                            viewModel.clearCollisionAlert()
                        }
                    )
                }
            }
        }
    }
}
