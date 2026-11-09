package com.example.spritradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spritradar.data.SpritRadarRepository
import com.example.spritradar.model.StationEvaluation
import com.example.spritradar.model.VerdictLevel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StationCard(
    evaluation: StationEvaluation,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onNavigateDirect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBestNet = evaluation.rankByNetCost == 1

    val (accentColor, verdictBgColor) = when (evaluation.verdictLevel) {
        VerdictLevel.BEST_NET_CHOICE -> Color(0xFF16A34A) to Color(0xFF16A34A).copy(alpha = 0.11f)
        VerdictLevel.WORTH_DETOUR -> Color(0xFF0D9488) to Color(0xFF0D9488).copy(alpha = 0.10f)
        VerdictLevel.ON_ROUTE_BASELINE -> Color(0xFF2563EB) to Color(0xFF2563EB).copy(alpha = 0.10f)
        VerdictLevel.DETOUR_TOO_EXPENSIVE -> Color(0xFFDC2626) to Color(0xFFDC2626).copy(alpha = 0.10f)
    }

    val cardBorderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isBestNet -> Color(0xFF16A34A).copy(alpha = 0.75f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected || isBestNet) 2.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Rank & Detour Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = accentColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isBestNet) "★ #1 NETTO-SIEGER" else "#${evaluation.rankByNetCost} Netto",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "+${SpritRadarRepository.formatKm(evaluation.station.detourKm)} Umweg (~${evaluation.station.detourMinutes} Min.)",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // 2. Full Station Name (untruncated)
            Text(
                text = evaluation.station.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 3. Full Address & Route Position (untruncated)
            val subAddress = buildString {
                if (evaluation.station.fullAddress.isNotBlank()) {
                    append(evaluation.station.fullAddress)
                    append(" • ")
                }
                append("Route km ${evaluation.station.distanceFromStartKm.roundToInt()}")
            }
            Text(
                text = subAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 4. Liter Price Display
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preis (${evaluation.fuelType.displayName}):",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = evaluation.formattedLiterPriceMain,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = evaluation.formattedPriceSuperscript,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Text(
                            text = " €/L",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }

            // 5. Smart Calculator Breakdown (stacked cleanly)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Tankfüllung (${evaluation.tankAmountLiters.roundToInt()} L): ${SpritRadarRepository.formatEuro(evaluation.grossFuelCostEuro)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Umweg-Sprit (+${String.format(Locale.GERMANY, "%.2f", evaluation.detourExtraLiters)} L): +${SpritRadarRepository.formatEuro(evaluation.detourFuelCostEuro)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (evaluation.detourFuelCostEuro > 0.80) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Effektiv-Gesamtkosten: ${SpritRadarRepository.formatEuro(evaluation.effectiveTotalCostEuro)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }

            // 6. Smart Recommendation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(verdictBgColor)
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = evaluation.recommendationHeadline,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = evaluation.recommendationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 7. Single Full-Width Google Maps Navigation Button ("Als..." button removed per Requirement #5)
            Button(
                onClick = onNavigateDirect,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "🧭 Mit Google Maps navigieren",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
