package com.example.data.engine

import com.example.data.model.LiveTrainStatus
import com.example.data.model.RailwayStation
import com.example.data.model.StationDeparture
import com.example.data.model.TrainCandidate
import com.example.data.model.TrainStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class FilteredTrainReport(
    val upcomingTrains: List<TrainCandidate>,
    val filteredDepartedTrains: List<FilteredDepartedItem>,
    val currentIstTimeFormatted: String
)

data class FilteredDepartedItem(
    val trainNumber: String,
    val trainName: String,
    val departureTime: String,
    val reason: String,
    val status: TrainStatus
)

object UpcomingTrainFilter {

    private val IST_TIMEZONE: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    // 1-minute grace tolerance for platform boarding before marking strictly departed
    const val GRACE_PERIOD_MINUTES = 1

    /**
     * Filters candidate trains to ONLY return those departing at or after the current IST time.
     * Trains that already departed according to timetable or live status are rejected.
     */
    fun filterUpcomingTrains(
        candidates: List<TrainCandidate>,
        referenceIstEpochMs: Long = System.currentTimeMillis(),
        liveStatuses: Map<String, LiveTrainStatus> = emptyMap()
    ): FilteredTrainReport {
        val istCal = Calendar.getInstance(IST_TIMEZONE).apply {
            timeInMillis = referenceIstEpochMs
        }
        val currentHour = istCal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = istCal.get(Calendar.MINUTE)
        val currentMinutesOfDay = currentHour * 60 + currentMinute

        val istTimeFormat = SimpleDateFormat("HH:mm:ss 'IST'", Locale.ENGLISH).apply {
            timeZone = IST_TIMEZONE
        }
        val currentTimeFormatted = istTimeFormat.format(Date(referenceIstEpochMs))

        val upcomingList = mutableListOf<TrainCandidate>()
        val departedList = mutableListOf<FilteredDepartedItem>()

        for (candidate in candidates) {
            val live = liveStatuses[candidate.trainNumber]
            val (status, departureMin, reason) = evaluateTrainDepartureStatus(
                candidate = candidate,
                currentMinutesOfDay = currentMinutesOfDay,
                liveStatus = live
            )

            when (status) {
                TrainStatus.UPCOMING, TrainStatus.BOARDING_SOON, TrainStatus.DELAYED -> {
                    upcomingList.add(candidate)
                }
                TrainStatus.DEPARTED -> {
                    departedList.add(
                        FilteredDepartedItem(
                            trainNumber = candidate.trainNumber,
                            trainName = candidate.trainName,
                            departureTime = candidate.departureTime,
                            reason = reason,
                            status = TrainStatus.DEPARTED
                        )
                    )
                }
                TrainStatus.CANCELLED -> {
                    departedList.add(
                        FilteredDepartedItem(
                            trainNumber = candidate.trainNumber,
                            trainName = candidate.trainName,
                            departureTime = candidate.departureTime,
                            reason = "Cancelled by Railways",
                            status = TrainStatus.CANCELLED
                        )
                    )
                }
                TrainStatus.UNKNOWN -> {
                    // Retain if cannot determine past status with certainty
                    upcomingList.add(candidate)
                }
            }
        }

        // Sort upcoming trains by departure time ascending
        val sortedUpcoming = upcomingList.sortedBy { candidate ->
            val parsedMin = parseMinutesOfDay(candidate.departureTime) ?: 9999
            // Handle midnight rollover: if current is late night (>= 22:00) and train is early morning (< 04:00), add 1440
            if (currentMinutesOfDay >= 22 * 60 && parsedMin < 4 * 60) {
                parsedMin + 1440
            } else {
                parsedMin
            }
        }

        return FilteredTrainReport(
            upcomingTrains = sortedUpcoming,
            filteredDepartedTrains = departedList,
            currentIstTimeFormatted = currentTimeFormatted
        )
    }

    /**
     * Evaluates a single candidate train against the current IST time and live board info.
     */
    fun evaluateTrainDepartureStatus(
        candidate: TrainCandidate,
        currentMinutesOfDay: Int,
        liveStatus: LiveTrainStatus?
    ): Triple<TrainStatus, Int, String> {
        val parsedDepartureMinutes = parseMinutesOfDay(candidate.departureTime)
            ?: return Triple(TrainStatus.UPCOMING, currentMinutesOfDay, "Unspecified departure")

        // Check if live status overrides static schedule
        if (liveStatus != null) {
            if (liveStatus.statusSummary.contains("Departed", ignoreCase = true)) {
                return Triple(TrainStatus.DEPARTED, parsedDepartureMinutes, "Live board: Departed")
            }
            if (liveStatus.statusSummary.contains("Cancelled", ignoreCase = true)) {
                return Triple(TrainStatus.CANCELLED, parsedDepartureMinutes, "Live board: Cancelled")
            }
        }

        // Calculate time delta in minutes accounting for midnight rollover
        var departureMin = parsedDepartureMinutes
        if (currentMinutesOfDay >= 22 * 60 && parsedDepartureMinutes < 4 * 60) {
            // Train is next day early morning (e.g. 00:15 when now is 23:50)
            departureMin += 1440
        } else if (currentMinutesOfDay < 4 * 60 && parsedDepartureMinutes >= 22 * 60) {
            // Train was yesterday late night
            departureMin -= 1440
        }

        val diffMinutes = departureMin - currentMinutesOfDay

        return when {
            diffMinutes < -GRACE_PERIOD_MINUTES -> {
                Triple(
                    TrainStatus.DEPARTED,
                    departureMin,
                    "Departed ${-diffMinutes}m ago (Scheduled ${candidate.departureTime})"
                )
            }
            diffMinutes in -GRACE_PERIOD_MINUTES..10 -> {
                Triple(
                    TrainStatus.BOARDING_SOON,
                    departureMin,
                    if (diffMinutes <= 0) "Boarding now" else "Departing in ${diffMinutes}m"
                )
            }
            liveStatus != null && liveStatus.delayMinutes > 0 -> {
                val estDepartureMin = departureMin + liveStatus.delayMinutes
                if (estDepartureMin < currentMinutesOfDay) {
                    Triple(
                        TrainStatus.DEPARTED,
                        estDepartureMin,
                        "Departed with delay (Est ${candidate.departureTime} +${liveStatus.delayMinutes}m)"
                    )
                } else {
                    Triple(
                        TrainStatus.DELAYED,
                        estDepartureMin,
                        "Delayed by ${liveStatus.delayMinutes}m (Est in ${estDepartureMin - currentMinutesOfDay}m)"
                    )
                }
            }
            else -> {
                Triple(
                    TrainStatus.UPCOMING,
                    departureMin,
                    "Upcoming in ${diffMinutes}m"
                )
            }
        }
    }

    /**
     * Parses "HH:mm" into minutes from midnight (0 to 1439).
     */
    fun parseMinutesOfDay(timeStr: String): Int? {
        val parts = timeStr.trim().split(":")
        if (parts.size < 2) return null
        val h = parts[0].trim().toIntOrNull() ?: return null
        val m = parts[1].trim().take(2).toIntOrNull() ?: return null
        return (h % 24) * 60 + (m % 60)
    }
}
