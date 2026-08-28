package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.TrainContextEngine
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.FoodRequestEntity
import com.example.data.local.UserEntity
import com.example.data.local.VendorEntity
import com.example.data.location.TrainLocationTracker
import com.example.data.location.UserLocationInfo
import com.example.data.model.FoodItem
import com.example.data.model.IndianLanguage
import com.example.data.model.JourneySession
import com.example.data.model.RailwayStation
import com.example.data.model.RegularCommuteSchedule
import com.example.data.model.TrainCandidate
import com.example.data.model.TrainContextState
import com.example.data.model.UserRole
import com.example.data.repository.CollisionCheckResult
import com.example.data.repository.LocalStaticRailwayDataProvider
import com.example.data.repository.RailSathiRepository
import com.example.data.repository.RailwayDataProvider
import com.example.data.repository.TrainRouteDetails
import com.example.ui.localization.LocalizationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab {
    HOME,
    COACH_RADAR,
    BUDGET_LEDGER,
    PROFILE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val railwayDataProvider: RailwayDataProvider = LocalStaticRailwayDataProvider()
    val repository = RailSathiRepository(db, railwayDataProvider)
    private val prefs = AppPreferences(application)
    val locationTracker = TrainLocationTracker(application)

    val trainContextEngine = TrainContextEngine(
        db = db,
        railwayDataProvider = railwayDataProvider,
        locationTracker = locationTracker,
        scope = viewModelScope
    )

    val currentLanguage: StateFlow<IndianLanguage> = prefs.languageFlow
    val isSeniorMode: StateFlow<Boolean> = prefs.seniorModeFlow
    val isOnboardingDone: StateFlow<Boolean> = prefs.onboardingCompletedFlow
    val locationState: StateFlow<UserLocationInfo> = locationTracker.locationState

    // Train Context Engine States
    val contextState: StateFlow<TrainContextState> = trainContextEngine.contextState
    val activeJourneySession: StateFlow<JourneySession?> = trainContextEngine.activeJourneyFlow
    val journeySession: StateFlow<JourneySession?> = activeJourneySession
    val nearbyStation: StateFlow<RailwayStation?> = trainContextEngine.nearbyStation
    val stationCandidates: StateFlow<List<TrainCandidate>> = trainContextEngine.stationCandidates
    val selectedCandidate: StateFlow<TrainCandidate?> = trainContextEngine.selectedCandidate
    val confidenceScore: StateFlow<Int> = trainContextEngine.confidenceScore
    val confidenceDescription: StateFlow<String> = trainContextEngine.confidenceDescription

    // Regular Commute Info
    val regularCommute = MutableStateFlow(RegularCommuteSchedule())

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchedTrains = MutableStateFlow<List<TrainCandidate>>(emptyList())
    val searchedTrains: StateFlow<List<TrainCandidate>> = _searchedTrains.asStateFlow()

    private val _searchedStations = MutableStateFlow<List<RailwayStation>>(emptyList())
    val searchedStations: StateFlow<List<RailwayStation>> = _searchedStations.asStateFlow()

    private val _currentRole = MutableStateFlow(prefs.getSavedRole() ?: UserRole.GUEST)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _activeNavTab = MutableStateFlow(AppNavTab.HOME)
    val activeNavTab: StateFlow<AppNavTab> = _activeNavTab.asStateFlow()

    val activeUser = repository.activeUserFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val activeRequests = repository.activeRequestsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allVendors = repository.allVendorsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val expenses = repository.allExpensesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalSpent = repository.totalSpentFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Current active train route details (only non-null if there is an active journey or candidate preview)
    private val _activeRouteDetails = MutableStateFlow<TrainRouteDetails?>(null)
    val activeRouteDetails: StateFlow<TrainRouteDetails?> = _activeRouteDetails.asStateFlow()
    val selectedRoute: StateFlow<TrainRouteDetails?> = activeRouteDetails

    private val _selectedCoach = MutableStateFlow("GS-2")
    val selectedCoach: StateFlow<String> = _selectedCoach.asStateFlow()

    private val _selectedFilterTag = MutableStateFlow("All")
    val selectedFilterTag: StateFlow<String> = _selectedFilterTag.asStateFlow()

    private val _selectedVendorId = MutableStateFlow("vendor_jhalmuri_1")
    val selectedVendorId: StateFlow<String> = _selectedVendorId.asStateFlow()

    private val _collisionResult = MutableStateFlow<CollisionCheckResult?>(null)
    val collisionResult: StateFlow<CollisionCheckResult?> = _collisionResult.asStateFlow()

    private val _alertBanner = MutableStateFlow<String?>(null)
    val alertBanner: StateFlow<String?> = _alertBanner.asStateFlow()

    private val _activeStationEta = MutableStateFlow(45)
    val activeStationEta: StateFlow<Int> = _activeStationEta.asStateFlow()

    // Real-time Hunger Signal event bus for sub-second vendor alerts (<50ms latency)
    private val _instantOrderEvents = MutableSharedFlow<FoodRequestEntity>(extraBufferCapacity = 64)
    val instantOrderEvents: SharedFlow<FoodRequestEntity> = _instantOrderEvents.asSharedFlow()

    init {
        // Sync active route with active journey session (never from location alone!)
        viewModelScope.launch {
            activeJourneySession.collect { session ->
                if (session != null) {
                    val route = repository.availableRoutes.find { it.trainNumber == session.trainNumber }
                    if (route != null) {
                        _activeRouteDetails.value = route
                        _selectedCoach.value = session.currentCoach
                    }
                } else {
                    _activeRouteDetails.value = null
                }
            }
        }

        // Live ticker runs ONLY when there is an active journey
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentSession = activeJourneySession.value
                val currentRoute = _activeRouteDetails.value

                if (currentSession != null && currentRoute != null) {
                    if (_activeStationEta.value > 0) {
                        _activeStationEta.value -= 1
                    } else {
                        val nextIdx = (currentRoute.currentStationIndex + 1) % currentRoute.stations.size
                        _activeRouteDetails.value = currentRoute.copy(currentStationIndex = nextIdx)
                        _activeStationEta.value = 45 // Next station halt countdown
                        triggerHapticNotification()
                    }
                }
            }
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        locationTracker.updatePermissionStatus(granted)
    }

    fun simulateAtStation(stationCode: String) {
        locationTracker.setManualSimulationLocation(stationCode)
    }

    fun setLanguage(lang: IndianLanguage) {
        prefs.saveLanguage(lang)
    }

    fun toggleSeniorMode(enabled: Boolean) {
        prefs.saveSeniorMode(enabled)
    }

    fun setNavTab(tab: AppNavTab) {
        _activeNavTab.value = tab
    }

    fun setCoach(coach: String) {
        _selectedCoach.value = coach
        val active = activeJourneySession.value
        if (active != null) {
            trainContextEngine.updateJourneyCoach(coach)
        }
    }

    fun setFilterTag(tag: String) {
        _selectedFilterTag.value = tag
    }

    fun setSelectedVendorId(id: String) {
        _selectedVendorId.value = id
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        prefs.saveRole(role)
        if (role == UserRole.VENDOR) {
            _selectedCoach.value = "VND-1"
        }
    }

    fun searchTimetable(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            val (stations, trains) = railwayDataProvider.searchStationsAndTrains(query)
            _searchedStations.value = stations
            _searchedTrains.value = trains
        }
    }

    fun setSearchQuery(query: String) {
        searchTimetable(query)
    }

    fun selectCandidateTrain(candidate: TrainCandidate) {
        trainContextEngine.selectCandidateTrain(candidate)
        val route = repository.availableRoutes.find { it.trainNumber == candidate.trainNumber }
        if (route != null) {
            _activeRouteDetails.value = route
        }
    }

    fun selectCandidate(candidate: TrainCandidate) {
        selectCandidateTrain(candidate)
    }

    fun clearCandidateSelection() {
        trainContextEngine.clearCandidateSelection()
        if (activeJourneySession.value == null) {
            _activeRouteDetails.value = null
        }
    }

    fun clearCandidate() {
        clearCandidateSelection()
    }

    fun setRoute(route: TrainRouteDetails) {
        _activeRouteDetails.value = route
    }

    fun startJourney(candidate: TrainCandidate, coach: String = "GS-2") {
        _selectedCoach.value = coach
        val userId = activeUser.value?.userId ?: "traveler_1"
        trainContextEngine.startJourney(candidate, userId, coach)
        triggerHapticNotification()
        val lang = currentLanguage.value
        _alertBanner.value = "Journey Started: ${candidate.trainName} ($coach)"
        viewModelScope.launch {
            delay(3000)
            _alertBanner.value = null
        }
    }

    fun startRegularCommuteJourney() {
        val commute = regularCommute.value
        val candidate = TrainCandidate(
            trainNumber = commute.usualTrainNumber,
            trainName = commute.usualTrainName,
            originStationCode = commute.originStationCode,
            originStationName = commute.originStationName,
            destStationCode = commute.destStationCode,
            destStationName = commute.destStationName,
            departureTime = commute.usualDepartureTime,
            arrivalTime = "09:30",
            platform = "PF 2",
            zone = "Eastern Railway",
            coachCodes = listOf("CAB-1", "LD-1", "VND-1", "GS-1", "GS-2", "GS-3", "VND-2", "LD-2", "CAB-2")
        )
        startJourney(candidate, commute.usualCoach)
    }

    fun endActiveJourney() {
        trainContextEngine.endJourney()
        _activeRouteDetails.value = null
        triggerHapticNotification()
        _alertBanner.value = "Journey completed. Hope you enjoyed your commute!"
        viewModelScope.launch {
            delay(3500)
            _alertBanner.value = null
        }
    }

    fun endJourney() {
        endActiveJourney()
    }

    fun cancelActiveJourney() {
        trainContextEngine.cancelJourney()
        _activeRouteDetails.value = null
        triggerHapticNotification()
        _alertBanner.value = "Journey tracking ended."
        viewModelScope.launch {
            delay(2500)
            _alertBanner.value = null
        }
    }

    fun vendorStartShift(candidate: TrainCandidate, coach: String = "VND-1") {
        _selectedCoach.value = coach
        val userId = activeUser.value?.userId ?: "vendor_1"
        trainContextEngine.startJourney(candidate, userId, coach)
        viewModelScope.launch {
            repository.updateVendorCoach(selectedVendorId.value, coach)
        }
        triggerHapticNotification()
        _alertBanner.value = "Shift Active on ${candidate.trainName} in Coach $coach!"
        viewModelScope.launch {
            delay(3000)
            _alertBanner.value = null
        }
    }

    fun vendorEndShift() {
        trainContextEngine.endJourney()
        _activeRouteDetails.value = null
        triggerHapticNotification()
        _alertBanner.value = "Shift ended. Great work today!"
        viewModelScope.launch {
            delay(3000)
            _alertBanner.value = null
        }
    }

    fun completeOnboarding(
        name: String,
        phone: String,
        role: UserRole,
        language: IndianLanguage,
        seniorMode: Boolean,
        train: String,
        coach: String
    ) {
        viewModelScope.launch {
            prefs.saveRole(role)
            prefs.saveLanguage(language)
            prefs.saveSeniorMode(seniorMode)
            prefs.setOnboardingCompleted(true)
            _currentRole.value = role

            val user = UserEntity(
                userId = "user_${System.currentTimeMillis()}",
                name = name.ifBlank { if (role == UserRole.VENDOR) "Subhash (Vendor)" else "Daily Commuter" },
                phone = phone.ifBlank { "9876543210" },
                role = role.name,
                languageCode = language.code,
                isSeniorMode = seniorMode,
                defaultTrain = train,
                defaultCoach = coach
            )
            repository.saveUser(user)
        }
    }

    fun sendHungerSignal(foodItem: FoodItem, seatNote: String) {
        viewModelScope.launch {
            val session = activeJourneySession.value
            val route = _activeRouteDetails.value

            if (session == null || route == null) {
                _alertBanner.value = "Please start a journey first to send signal to vendors onboard!"
                delay(3000)
                _alertBanner.value = null
                return@launch
            }

            val coach = _selectedCoach.value
            val userName = activeUser.value?.name ?: "Traveler"

            val req = repository.createAndDispatchFoodRequest(
                passengerName = userName,
                trainNumber = route.trainNumber,
                trainName = route.trainName,
                coachNumber = coach,
                seatDetail = seatNote,
                foodItem = foodItem
            )

            // Broadcast on instant event bus (< 50ms latency)
            _instantOrderEvents.tryEmit(req)

            val lang = currentLanguage.value
            _alertBanner.value = LocalizationManager.getString("hunger_signal_title", lang) + ": " + foodItem.nameEn + " ($coach)"
            triggerHapticNotification()

            delay(3500)
            _alertBanner.value = null
        }
    }

    fun verifyAndBoardCoach(vendorId: String, specialityItemId: String, targetCoach: String) {
        viewModelScope.launch {
            val route = _activeRouteDetails.value
            val trainNum = route?.trainNumber ?: "31821"

            val result = repository.checkCoachCollision(
                vendorId = vendorId,
                specialityItemId = specialityItemId,
                trainNumber = trainNum,
                targetCoachNumber = targetCoach
            )
            _collisionResult.value = result

            if (result is CollisionCheckResult.Allowed) {
                repository.updateVendorCoach(vendorId, targetCoach)
                _selectedCoach.value = targetCoach
                trainContextEngine.updateJourneyCoach(targetCoach)
                triggerHapticNotification()
            }
        }
    }

    fun clearCollisionAlert() {
        _collisionResult.value = null
    }

    fun vendorAcceptRequest(request: FoodRequestEntity, vendor: VendorEntity) {
        viewModelScope.launch {
            repository.acceptRequestByVendor(request.id, vendor.vendorId, vendor.name)
            _alertBanner.value = "Accepted order in ${request.coachNumber} for ${request.foodItemName}!"
            triggerHapticNotification()
            delay(3000)
            _alertBanner.value = null
        }
    }

    fun vendorDeliverAndCollect(request: FoodRequestEntity, vendor: VendorEntity) {
        viewModelScope.launch {
            val trainNum = _activeRouteDetails.value?.trainNumber ?: request.trainNumber
            repository.completeDeliveryAndRecordSale(
                requestId = request.id,
                vendorId = vendor.vendorId,
                foodItemName = request.foodItemName,
                amount = request.price.toDouble(),
                coachNumber = request.coachNumber,
                trainNumber = trainNum,
                buyerName = request.passengerName
            )
            _alertBanner.value = "Delivered & ₹${request.price} added to Today's Ledger!"
            triggerHapticNotification()
            delay(3000)
            _alertBanner.value = null
        }
    }

    fun recordManualSale(vendor: VendorEntity, foodName: String, amount: Double, coach: String) {
        viewModelScope.launch {
            val trainNum = _activeRouteDetails.value?.trainNumber ?: "31821"
            repository.completeDeliveryAndRecordSale(
                requestId = 0L,
                vendorId = vendor.vendorId,
                foodItemName = foodName,
                amount = amount,
                coachNumber = coach,
                trainNumber = trainNum,
                buyerName = "Walk-in passenger"
            )
            _alertBanner.value = "Sale of ₹${amount.toInt()} recorded in coach $coach!"
            triggerHapticNotification()
            delay(3000)
            _alertBanner.value = null
        }
    }

    fun addManualExpense(title: String, category: String, amount: Double, coach: String, note: String) {
        viewModelScope.launch {
            val trainNum = _activeRouteDetails.value?.trainNumber ?: "Local Commute"
            repository.addExpenseDirect(
                title = title,
                category = category,
                amount = amount,
                coach = coach,
                trainNumber = trainNum,
                note = note
            )
            _alertBanner.value = "Expense ₹${amount.toInt()} recorded in budget!"
            delay(3000)
            _alertBanner.value = null
        }
    }

    fun cancelRequest(requestId: Long) {
        viewModelScope.launch {
            repository.cancelRequest(requestId)
        }
    }

    fun triggerHapticNotification() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(80)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}
