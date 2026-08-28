package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExpenseEntity
import com.example.data.local.UserEntity
import com.example.data.local.VendorEntity
import com.example.data.model.IndianLanguage
import com.example.data.model.UserRole
import com.example.ui.localization.LocalizationManager
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CharcoalTextMuted
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NatureGreen
import com.example.ui.theme.NatureGreenLight
import com.example.ui.theme.RailNavy
import com.example.ui.theme.TerracottaAmber
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSandBackground
import com.example.ui.theme.WarmSurface

@Composable
fun BudgetExpenseScreen(
    role: UserRole,
    user: UserEntity?,
    vendor: VendorEntity?,
    expenses: List<ExpenseEntity>,
    totalSpent: Double,
    language: IndianLanguage,
    isSeniorMode: Boolean,
    onAddExpense: (title: String, category: String, amount: Double, coach: String, note: String) -> Unit,
    onUpdateBudgetLimit: (Double) -> Unit = {},
    onResetBudget: () -> Unit = {}
) {
    val monthlyBudgetLimit = user?.monthlyBudgetLimit ?: 1500.0
    val budgetProgress = ((totalSpent / monthlyBudgetLimit).coerceIn(0.0, 1.0)).toFloat()
    val estimatedSavings = (totalSpent * 0.25).toInt() // Estimated ~25% saved vs fancy station stalls

    var showManualAddDialog by remember { mutableStateOf(false) }
    var showEditLimitDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var newLimitText by remember { mutableStateOf(monthlyBudgetLimit.toInt().toString()) }
    var newTitle by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var newCoach by remember { mutableStateOf(user?.defaultCoach ?: "C-4") }

    if (showEditLimitDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditLimitDialog = false },
            title = {
                Text("Edit Monthly Budget Limit", fontWeight = FontWeight.Bold, color = RailNavy)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Set your target monthly commute snack & travel budget (₹):", fontSize = 14.sp, color = CharcoalText)
                    OutlinedTextField(
                        value = newLimitText,
                        onValueChange = { newLimitText = it.filter { c -> c.isDigit() } },
                        label = { Text("Budget Limit (₹)") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_budget_limit_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = newLimitText.toDoubleOrNull() ?: 1500.0
                        onUpdateBudgetLimit(limit)
                        showEditLimitDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RailNavy),
                    modifier = Modifier.testTag("save_budget_limit_button")
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEditLimitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("Reset Monthly Budget", fontWeight = FontWeight.Bold, color = RailNavy)
            },
            text = {
                Text("Are you sure you want to reset your monthly commute budget limit to ₹1500?", fontSize = 14.sp, color = CharcoalText)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetBudget()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaAmber),
                    modifier = Modifier.testTag("confirm_reset_budget_button")
                ) {
                    Text("Reset Budget")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmSandBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Monthly Budget Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (role == UserRole.VENDOR) "Vendor Revenue Ledger" else LocalizationManager.getString("nav_budget", language),
                                    fontSize = if (isSeniorMode) 18.sp else 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RailNavy
                                )
                                Text(
                                    text = "Monthly Commute Snack Tracker",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NatureGreenLight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Active Month",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NatureGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Amount Spent vs Budget
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Spent This Month",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                                Text(
                                    text = "₹${totalSpent.toInt()}",
                                    fontSize = if (isSeniorMode) 28.sp else 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaAmber
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Monthly Budget Limit",
                                    fontSize = 12.sp,
                                    color = CharcoalTextMuted
                                )
                                Text(
                                    text = "₹${monthlyBudgetLimit.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { budgetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (budgetProgress > 0.85f) Color(0xFFEF4444) else NatureGreen,
                            trackColor = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.material3.OutlinedButton(
                                onClick = { showEditLimitDialog = true },
                                modifier = Modifier.weight(1f).testTag("edit_budget_limit_btn")
                            ) {
                                Text("Edit Limit", fontSize = 12.sp)
                            }

                            androidx.compose.material3.OutlinedButton(
                                onClick = { showResetConfirmDialog = true },
                                modifier = Modifier.weight(1f).testTag("reset_budget_btn"),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = TerracottaAmber)
                            ) {
                                Text("Reset", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Estimated Savings Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NatureGreenLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NatureGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(NatureGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "Savings",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Estimated Savings: ₹$estimatedSavings this month",
                                fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NatureGreen
                            )
                            Text(
                                text = "Buying fresh local train hawker snacks directly saves ~25% compared to railway station food stalls!",
                                fontSize = 11.sp,
                                color = CharcoalText
                            )
                        }
                    }
                }
            }

            // Quick Add Expense Row
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Quick Add Commute Snack Expense",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onAddExpense("Masala Chai", "Chai", 10.0, "C-4", "Quick cutting chai") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                                modifier = Modifier.weight(1f).testTag("quick_expense_chai")
                            ) {
                                Text("Chai ₹10", color = RailNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onAddExpense("Jhalmuri", "Snacks", 20.0, "C-4", "Spicy jhalmuri packet") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                                modifier = Modifier.weight(1f).testTag("quick_expense_muri")
                            ) {
                                Text("Jhalmuri ₹20", color = RailNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onAddExpense("Roasted Badam", "Nuts", 15.0, "C-4", "Garam badam thonga") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Badam ₹15", color = RailNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Expense History List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expense History & Receipts (${expenses.size})",
                        fontSize = if (isSeniorMode) 17.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RailNavy
                    )
                }
            }

            if (expenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = WarmSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expenses recorded yet. Order snacks or use quick-add buttons above!",
                                fontSize = 13.sp,
                                color = CharcoalTextMuted
                            )
                        }
                    }
                }
            } else {
                items(expenses) { exp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEF3C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = "Receipt",
                                        tint = TerracottaAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = exp.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isSeniorMode) 15.sp else 13.sp,
                                        color = CharcoalText
                                    )
                                    Text(
                                        text = "${exp.dateString} • Coach ${exp.coach}",
                                        fontSize = 11.sp,
                                        color = CharcoalTextMuted
                                    )
                                }
                            }

                            Text(
                                text = "₹${exp.amount.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isSeniorMode) 17.sp else 15.sp,
                                color = TerracottaAmber
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
