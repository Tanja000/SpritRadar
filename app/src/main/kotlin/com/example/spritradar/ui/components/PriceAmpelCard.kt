package com.example.spritradar.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spritradar.data.SpritRadarRepository
import com.example.spritradar.model.HistoricalPriceAnalysis
import com.example.spritradar.model.TrafficLightColor

@Composable
fun PriceAmpelCard(
    analysis: HistoricalPriceAnalysis,
    simulatedHour: Int?,
    onSelectSimulatedHour: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val (containerBg, borderColor, accentColor) = when (analysis.light) {
        TrafficLightColor.GREEN -> Triple(
            Color(0xFF052E16).copy(alpha = 0.12f),
            Color(0xFF16A34A),
            Color(0xFF15803D)
        )
        TrafficLightColor.YELLOW -> Triple(
            Color(0xFF422006).copy(alpha = 0.12f),
            Color(0xFFD97706),
            Color(0xFFB45309)
        )
        TrafficLightColor.RED -> Triple(
            Color(0xFF450A0A).copy(alpha = 0.12f),
            Color(0xFFDC2626),
            Color(0xFFB91C1C)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .background(containerBg)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Visual Traffic Light Housing (Rot / Gelb / Grün)
                TrafficLightIndicator(activeLight = analysis.light)

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = accentColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = analysis.badgeText,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = if (expanded) "24h-Kurve ▲" else "24h-Kurve ▼",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = analysis.headline,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = analysis.recommendationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatMiniItem(
                            label = "Bestes Zeitfenster",
                            value = analysis.bestWindowLabel
                        )
                        StatMiniItem(
                            label = "Erw. Abendpreis",
                            value = SpritRadarRepository.formatPriceL(analysis.expectedEveningPrice)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Historischer 24h-Tagesverlauf (Tankerkönig MTS-K Kurve):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 24-Hour Intraday Sparkline Bar Chart
                    val minP = analysis.historicalDailyMinPrice
                    val maxP = (analysis.historicalDailyMaxPrice).coerceAtLeast(minP + 0.04)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        for (pt in analysis.hourlyCurve) {
                            val norm = ((pt.avgPrice - minP) / (maxP - minP)).toFloat().coerceIn(0.18f, 1.0f)
                            val barColor = when {
                                pt.isCurrentHour -> MaterialTheme.colorScheme.primary
                                pt.isBestWindow -> Color(0xFF16A34A)
                                norm > 0.72f -> Color(0xFFEF4444).copy(alpha = 0.75f)
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(norm * 0.78f)
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                        .background(barColor)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (pt.hour % 4 == 0 || pt.hour == 19) {
                                    Text(
                                        text = "${pt.hour}",
                                        fontSize = 8.sp,
                                        fontWeight = if (pt.isCurrentHour) FontWeight.Bold else FontWeight.Normal,
                                        color = if (pt.isCurrentHour) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Time-of-day simulator buttons: "Jetzt (Live)" on line 1, the others on line 2 below
                    Text(
                        text = "Ampel-Zeitpunkt testen:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = simulatedHour == null,
                                onClick = { onSelectSimulatedHour(null) },
                                label = { Text("Jetzt (Live)", fontSize = 12.sp) }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = simulatedHour == 7,
                                onClick = { onSelectSimulatedHour(7) },
                                label = { Text("07:00 🔴", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 14,
                                onClick = { onSelectSimulatedHour(14) },
                                label = { Text("14:00 🟡", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = simulatedHour == 19,
                                onClick = { onSelectSimulatedHour(19) },
                                label = { Text("19:00 🟢", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrafficLightIndicator(activeLight: TrafficLightColor) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E293B))
            .padding(horizontal = 7.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LightBulb(
            color = Color(0xFFEF4444),
            isActive = activeLight == TrafficLightColor.RED
        )
        LightBulb(
            color = Color(0xFFF59E0B),
            isActive = activeLight == TrafficLightColor.YELLOW
        )
        LightBulb(
            color = Color(0xFF22C55E),
            isActive = activeLight == TrafficLightColor.GREEN
        )
    }
}

@Composable
private fun LightBulb(color: Color, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(if (isActive) color else color.copy(alpha = 0.22f))
            .then(
                if (isActive) {
                    Modifier.border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                } else {
                    Modifier
                }
            )
    )
}

@Composable
private fun StatMiniItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
