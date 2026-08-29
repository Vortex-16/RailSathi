package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.TrainDiagnosticsSnapshot
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.NatureGreen
import com.example.ui.theme.RailNavy
import com.example.ui.theme.TerracottaAmber
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSurface

@Composable
fun TrainDiagnosticsDialog(
    diagnostics: TrainDiagnosticsSnapshot,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Diagnostics",
                        tint = RailNavy,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Location & Train Diagnostics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = RailNavy
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = CharcoalTextMuted)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Location Engine & GPS Metrics
                item {
                    DiagnosticsCard(title = "1. Location Engine State", icon = Icons.Default.GpsFixed) {
                        val loc = diagnostics.locationDiagnostics
                        DiagRow("Location Provider", loc.provider)
                        DiagRow("Coordinates", if (loc.latitude != null) "%.5f, %.5f".format(loc.latitude, loc.longitude) else "Unavailable")
                        DiagRow("Accuracy", "±${loc.accuracyMeters.toInt()} meters")
                        DiagRow("Location Age", "${loc.ageSeconds} seconds (${if (loc.ageSeconds <= 60) "Fresh" else "Stale"})")
                        DiagRow("Speed / Bearing", "${"%.1f".format(loc.speedMps * 3.6)} km/h • ${loc.bearingDegrees.toInt()}°")
                        DiagRow("GPS Provider", if (loc.isGpsEnabled) "Enabled (Hardware GPS)" else "Disabled")
                        DiagRow("Network Provider", if (loc.isNetworkEnabled) "Enabled (Cell/Wi-Fi)" else "Disabled")
                        DiagRow("Permission Type", loc.permissionType)
                        DiagRow("Quality Gate Status", if (loc.qualityGatePass) "PASSED (Fresh & Accurate)" else "REJECTED (Stale/Low Accuracy)", isGood = loc.qualityGatePass)
                    }
                }

                // Section 2: Station Detection
                item {
                    DiagnosticsCard(title = "2. Station Detection Engine", icon = Icons.Default.Info) {
                        DiagRow("Detected Station", diagnostics.detectedStation?.let { "${it.nameEn} (${it.code})" } ?: "None (Off-track)")
                        DiagRow("Distance to Station", if (diagnostics.stationDistanceMeters < 100000) "%.0f meters".format(diagnostics.stationDistanceMeters) else ">100 km")
                        DiagRow("Station Confidence", diagnostics.stationConfidence.name, isGood = diagnostics.stationConfidence.name == "HIGH")
                        DiagRow("At Station Geofence", if (diagnostics.isAtStation) "YES (<350m)" else "NO")
                        DiagRow("Current IST Time", diagnostics.currentIstTime.ifEmpty { "Syncing IST..." })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                modifier = Modifier.testTag("close_diagnostics_dialog_btn")
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun DiagnosticsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = WarmSurface),
        border = BorderStroke(1.dp, WarmBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = RailNavy, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RailNavy)
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String, isGood: Boolean? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = CharcoalTextMuted)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = when (isGood) {
                true -> NatureGreen
                false -> AlertRed
                null -> CharcoalText
            }
        )
    }
}
