@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationEntity

// Custom Color Palettes
private val DarkBackground = Color(0xFF17171C)
private val DarkSurface = Color(0xFF222431)
private val DarkNumberBg = Color(0xFF2E2F38)
private val DarkActionBg = Color(0xFF4E505F)
private val AccentColor = Color(0xFFFF9F0A)

private val LightBackground = Color(0xFFF1F2F3)
private val LightSurface = Color(0xFFFFFFFF)
private val LightNumberBg = Color(0xFFFFFFFF)
private val LightActionBg = Color(0xFFD2D3DA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    // Local state for Dark Mode override (defaults to true for premium look)
    var isDarkMode by rememberSaveable { mutableStateOf(true) }
    var isHistoryVisible by rememberSaveable { mutableStateOf(false) }

    val expression by viewModel.expression.collectAsState()
    val previewResult by viewModel.previewResult.collectAsState()
    val history by viewModel.history.collectAsState()

    // Active colors based on theme choice
    val backgroundColor = if (isDarkMode) DarkBackground else LightBackground
    val surfaceColor = if (isDarkMode) DarkSurface else LightSurface
    val primaryTextColor = if (isDarkMode) Color.White else Color(0xFF17171C)
    val secondaryTextColor = if (isDarkMode) Color(0xFFA5A5A5) else Color(0xFF747477)
    val numberKeyBg = if (isDarkMode) DarkNumberBg else LightNumberBg
    val numberKeyText = if (isDarkMode) Color.White else Color(0xFF17171C)
    val actionKeyBg = if (isDarkMode) DarkActionBg else LightActionBg
    val actionKeyText = if (isDarkMode) Color.White else Color(0xFF17171C)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("calculator_scaffold"),
        containerColor = backgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Action Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Калькулятор",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor,
                        modifier = Modifier.testTag("app_title_text")
                    )

                    Row {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isDarkMode = !isDarkMode
                            },
                            modifier = Modifier.testTag("theme_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkMode) "Светлая тема" else "Темная тема",
                                tint = primaryTextColor
                            )
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isHistoryVisible = !isHistoryVisible
                            },
                            modifier = Modifier.testTag("history_toggle_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "История вычислений",
                                tint = if (isHistoryVisible) AccentColor else primaryTextColor
                            )
                        }
                    }
                }

                // Calculation Display Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End
                ) {
                    val formulaScrollState = rememberScrollState()

                    // Auto-scroll expression to the end on change
                    LaunchedEffect(expression) {
                        formulaScrollState.animateScrollTo(formulaScrollState.maxValue)
                    }

                    // Selected Formula / Expression representation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(formulaScrollState),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = expression.ifEmpty { "0" },
                            fontSize = if (expression.length > 12) 36.sp else 48.sp,
                            fontWeight = FontWeight.Light,
                            color = primaryTextColor,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.testTag("expression_display_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Immediate Pre-Evaluation preview result (or final calculated result)
                    AnimatedVisibility(
                        visible = previewResult.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = previewResult,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (previewResult == "Ошибка" || previewResult == "Деление на 0") Color.Red else secondaryTextColor,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .testTag("preview_result_text")
                                .clickable(
                                    onClick = {
                                        // Simple tap to copy result to clipboard
                                        if (previewResult.isNotEmpty() && previewResult != "Ошибка" && previewResult != "Деление на 0") {
                                            clipboardManager.setText(AnnotatedString(previewResult))
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    }
                                )
                        )
                    }
                }

                // Keyboard Grid Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(surfaceColor)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Keyboard Row 1: C, ±, %, ⌫
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                text = "C",
                                onClick = { viewModel.onClear() },
                                backgroundColor = actionKeyBg,
                                contentColor = AccentColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_clear")
                            )

                            CalculatorButton(
                                text = "±",
                                onClick = { viewModel.onToggleSign() },
                                backgroundColor = actionKeyBg,
                                contentColor = actionKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_sign")
                            )

                            CalculatorButton(
                                text = "%",
                                onClick = { viewModel.onPercentage() },
                                backgroundColor = actionKeyBg,
                                contentColor = actionKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_percent")
                            )

                            CalculatorIconButton(
                                icon = Icons.Outlined.Backspace,
                                onClick = { viewModel.onBackspace() },
                                onLongClick = { viewModel.onClear() },
                                backgroundColor = actionKeyBg,
                                contentColor = actionKeyText,
                                contentDescription = "Стереть",
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_backspace")
                            )
                        }

                        // Keyboard Row 2: 7, 8, 9, ÷
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                text = "7",
                                onClick = { viewModel.onDigit("7") },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_digit_7")
                            )
                            CalculatorButton(
                                text = "8",
                                onClick = { viewModel.onDigit("8") },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_digit_8")
                            )
                            CalculatorButton(
                                text = "9",
                                onClick = { viewModel.onDigit("9") },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_digit_9")
                            )
                            CalculatorButton(
                                text = "÷",
                                onClick = { viewModel.onOperator("÷") },
                                backgroundColor = AccentColor,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_op_div")
                            )
                        }

                        // Keyboard Row 3: 4, 5, 6, ×
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                text = "4",
                                onClick = { viewModel.onDigit("4") },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_digit_4")
                            )
                            CalculatorButton(
                                text = "5",
                                onClick = { viewModel.onDigit("5") },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_digit_5")
                            )
                            CalculatorButton(
                                text = "6",
                                onClick = { viewModel.onDigit("6") },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_digit_6")
                            )
                            CalculatorButton(
                                text = "×",
                                onClick = { viewModel.onOperator("×") },
                                backgroundColor = AccentColor,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_op_mul")
                            )
                        }

                        // Keyboard Row 4: 1, 2, 3, -
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                text = "1",
                                onClick = { viewModel.onDigit("1") },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_digit_1")
                            )
                            CalculatorButton(
                                text = "2",
                                onClick = { viewModel.onDigit("2") },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_digit_2")
                            )
                            CalculatorButton(
                                text = "3",
                                onClick = { viewModel.onDigit("3") },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_digit_3")
                            )
                            CalculatorButton(
                                text = "-",
                                onClick = { viewModel.onOperator("-") },
                                backgroundColor = AccentColor,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_op_sub")
                            )
                        }

                        // Keyboard Row 5: 0, ., +, =
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CalculatorButton(
                                text = "0",
                                onClick = { viewModel.onDigit("0") },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(1.3f) // Slightly wider for symmetry
                                    .testTag("btn_digit_0")
                            )
                            CalculatorButton(
                                text = ".",
                                onClick = { viewModel.onDecimal() },
                                backgroundColor = numberKeyBg,
                                contentColor = numberKeyText,
                                modifier = Modifier
                                    .weight(0.9f)
                                    .testTag("btn_decimal")
                            )
                            CalculatorButton(
                                text = "+",
                                onClick = { viewModel.onOperator("+") },
                                backgroundColor = AccentColor,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(0.9f)
                                    .testTag("btn_op_add")
                            )
                            CalculatorButton(
                                text = "=",
                                onClick = { viewModel.onCalculate() },
                                backgroundColor = AccentColor,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(0.9f)
                                    .testTag("btn_calculate")
                            )
                        }
                    }
                }
            }

            // Expanding Sliding Overlay for History Pane
            AnimatedVisibility(
                visible = isHistoryVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f) // Cover up the display windows cleanly
                    .align(Alignment.TopCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("history_card_container"),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.96f)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Title header inside sheet
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "История",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor,
                                modifier = Modifier.testTag("history_title_text")
                            )

                            if (history.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onClearHistory()
                                    },
                                    modifier = Modifier.testTag("history_clear_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Очистить историю",
                                        tint = Color.Red.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = secondaryTextColor.copy(alpha = 0.2f)
                        )

                        if (history.isEmpty()) {
                            // Beautiful Empty State Illustration
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "История пока пуста",
                                    fontSize = 15.sp,
                                    color = secondaryTextColor,
                                    modifier = Modifier.testTag("history_empty_text")
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .testTag("history_lazy_list"),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(history, key = { it.id }) { calc ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    viewModel.onHistoryItemClicked(calc)
                                                    isHistoryVisible = false
                                                }
                                            )
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = calc.expression,
                                            fontSize = 15.sp,
                                            color = secondaryTextColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "= ${calc.result}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AccentColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
