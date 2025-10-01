/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat
import androidx.compose.material3.TextField
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.size


// Currency data class with flag drawable resource
data class Currency(val code: String, val name: String, val rate: Double, val flagResId: Int)

// Conversion history data class
data class ConversionHistory(
    val fromAmount: Double,
    val fromCurrency: Currency,
    val toAmount: Double,
    val toCurrency: Currency,
    val timestamp: String
)

// Exchange rates relative to USD (1 USD = rate in target currency)
val currencies = listOf(
    Currency("USD", "US Dollar", 1.0, R.drawable.flag_of_the_united_states),
    Currency("EUR", "Euro", 0.85, R.drawable.flag_of_europe_svg),
    Currency("JPY", "Japanese Yen", 110.0, R.drawable.flag_of_japan_svg),
    Currency("HKD", "Hong Kong Dollar", 7.8, R.drawable.flag_of_hong_kong_svg),
    Currency("GBP", "British Pound", 0.73, R.drawable.flag_of_the_united_kingdom_svg),
    Currency("CAD", "Canadian Dollar", 1.25, R.drawable.flag_of_canada)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            
            TipTimeTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TipTimeLayout(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }
}

@Composable
fun TipTimeLayout(
    isDarkTheme: Boolean = false,
    onThemeChange: () -> Unit = {}
) {
    var amountInput by remember { mutableStateOf("") }
    var baseCurrency by remember { mutableStateOf(currencies[0]) } // USD default
    var targetCurrency by remember { mutableStateOf(currencies[1]) } // EUR default
    var isBaseDropdownExpanded by remember { mutableStateOf(false) }
    var isTargetDropdownExpanded by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    var conversionHistory by remember { mutableStateOf(listOf<ConversionHistory>()) }
    var decimalPlaces by remember { mutableStateOf(2) } // Default to 2 decimal places
    var isDecimalDropdownExpanded by remember { mutableStateOf(false) }
    
    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000)
        }
    }
    
    val amount = amountInput.toDoubleOrNull() ?: 0.0
    // Show converted amount in real-time for display only
    val convertedAmount = if (amount > 0) {
        convertBetweenCurrencies(amount, baseCurrency, targetCurrency, decimalPlaces)
    } else ""
    
    // Function to perform conversion and add to history
    fun performConversion() {
        if (amount > 0) {
            val result = convertBetweenCurrencies(amount, baseCurrency, targetCurrency, decimalPlaces)
            if (result.isNotEmpty()) {
                val currentTimestamp = currentTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm:ss"))
                val newEntry = ConversionHistory(
                    fromAmount = amount,
                    fromCurrency = baseCurrency,
                    toAmount = parseConvertedAmount(result),
                    toCurrency = targetCurrency,
                    timestamp = currentTimestamp
                )
                conversionHistory = (listOf(newEntry) + conversionHistory).take(5)
            }
        }
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header with title and theme toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Currency Converter",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Theme toggle section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDarkTheme) "🌙" else "☀️",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeChange() }
                )
            }
        }
        
        // Real-time date and time display
        Text(
            text = currentTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - HH:mm:ss")),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(alignment = Alignment.Start)
        )
        
        // Decimal precision selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Decimal Places:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                modifier = Modifier
                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                    .clickable { isDecimalDropdownExpanded = true }
                    .padding(8.dp)
                    .width(60.dp)
            ) {
                Text(
                    text = decimalPlaces.toString(),
                    modifier = Modifier.weight(1f)
                )
                Text(text = "▼", color = Color.Gray)
            }
            
            DropdownMenu(
                expanded = isDecimalDropdownExpanded,
                onDismissRequest = { isDecimalDropdownExpanded = false }
            ) {
                listOf(0, 2, 4, 6).forEach { places ->
                    DropdownMenuItem(
                        text = { Text("$places decimal places") },
                        onClick = {
                            decimalPlaces = places
                            isDecimalDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        EditNumberField(
            value = amountInput,
            onValueChange = { newValue ->
                // Allow only valid numeric input: Core functionality Task 4
                if (newValue.isEmpty() || (newValue.toDoubleOrNull() != null && newValue.toDouble() >= 0)) {
                    amountInput = newValue
                }
            },
            baseCurrency = baseCurrency,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        )
        
        // Currency Selection Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Base Currency Dropdown
            Column {
                Text("From:", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .clickable { isBaseDropdownExpanded = true }
                        .padding(8.dp)
                        .width(120.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        FlagImage(
                            flagResId = baseCurrency.flagResId,
                            modifier = Modifier.size(20.dp).padding(end = 4.dp)
                        )
                        Text(text = baseCurrency.code)
                    }
                    Text(text = "▼", color = Color.Gray)
                }
                DropdownMenu(
                    expanded = isBaseDropdownExpanded,
                    onDismissRequest = { isBaseDropdownExpanded = false }
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FlagImage(
                                        flagResId = currency.flagResId,
                                        modifier = Modifier.size(16.dp).padding(end = 8.dp)
                                    )
                                    Text("${currency.code}")
                                }
                            },
                            onClick = {
                                baseCurrency = currency
                                isBaseDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Swap Button
            Button(
                onClick = {
                    val temp = baseCurrency
                    baseCurrency = targetCurrency
                    targetCurrency = temp
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("⇄")
            }
            
            // Target Currency Dropdown
            Column {
                Text("To:", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .clickable { isTargetDropdownExpanded = true }
                        .padding(8.dp)
                        .width(120.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        FlagImage(
                            flagResId = targetCurrency.flagResId,
                            modifier = Modifier.size(20.dp).padding(end = 4.dp)
                        )
                        Text(text = targetCurrency.code)
                    }
                    Text(text = "▼", color = Color.Gray)
                }
                DropdownMenu(
                    expanded = isTargetDropdownExpanded,
                    onDismissRequest = { isTargetDropdownExpanded = false }
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FlagImage(
                                        flagResId = currency.flagResId,
                                        modifier = Modifier.size(16.dp).padding(end = 8.dp)
                                    )
                                    Text("${currency.code}")
                                }
                            },
                            onClick = {
                                targetCurrency = currency
                                isTargetDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
        
        // Save to history Button
        Button(
            onClick = { performConversion() },
            enabled = amount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Save to history",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // Show converted amount with flag in Text composable (real-time preview)
        if (convertedAmount.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Preview:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    FlagImage(
                        flagResId = targetCurrency.flagResId,
                        modifier = Modifier.size(32.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = convertedAmount,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        } else {
            Text(
                text = "Enter amount to convert",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Conversion History
        if (conversionHistory.isNotEmpty()) {
            Text(
                text = "Recent Conversions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .align(alignment = Alignment.Start)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                items(conversionHistory) { history ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FlagImage(
                                    flagResId = history.fromCurrency.flagResId,
                                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                )
                                Text(
                                    text = "${String.format("%.${decimalPlaces}f", history.fromAmount)} ${history.fromCurrency.code}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = " → ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                FlagImage(
                                    flagResId = history.toCurrency.flagResId,
                                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                )
                                Text(
                                    text = "${String.format("%.${decimalPlaces}f", history.toAmount)} ${history.toCurrency.code}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(
                                text = history.timestamp,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun FlagImage(
    flagResId: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = flagResId),
        contentDescription = "Flag",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun EditNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    baseCurrency: Currency,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text("Amount in ${baseCurrency.code}") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

/**
 * Converts amount between any two currencies using USD as base with decimal precision
 */
private fun convertBetweenCurrencies(amount: Double, fromCurrency: Currency, toCurrency: Currency, decimalPlaces: Int): String {
    // Convert to USD first, then to target currency
    val usdAmount = amount / fromCurrency.rate
    val convertedAmount = usdAmount * toCurrency.rate
    return String.format("%.${decimalPlaces}f %s", convertedAmount, toCurrency.code)
}

/**
 * Helper function to parse converted amount from string
 */
private fun parseConvertedAmount(convertedString: String): Double {
    return convertedString.split(" ")[0].toDoubleOrNull() ?: 0.0
}

@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        TipTimeLayout()
    }
}

