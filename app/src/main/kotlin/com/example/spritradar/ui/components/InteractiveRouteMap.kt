package com.example.spritradar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spritradar.data.SpritRadarRepository
import com.example.spritradar.model.DetourOption
import com.example.spritradar.model.GeoPoint
import com.example.spritradar.model.StationEvaluation
import com.example.spritradar.model.VerdictLevel
import java.util.Locale
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private data class PinHitTarget(
    val stationId: String,
    val center: Offset,
    val radiusPx: Float
)

@Composable
fun InteractiveRouteMap(
    startPoint: GeoPoint,
    destPoint: GeoPoint,
    routePolyline: List<GeoPoint>,
    detourOption: DetourOption,
    evaluations: List<StationEvaluation>,
    selectedStationId: String?,
    onSelectStation: (String) -> Unit,
    onNavigateStation: (StationEvaluation) -> Unit,
    modifier: Modifier = Modifier
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    val pinTargets = remember { mutableStateListOf<PinHitTarget>() }

    val selectedEval = evaluations.firstOrNull { it.station.id == selectedStationId }
        ?: evaluations.minByOrNull { it.rankByNetCost }

    val allLatitudes = buildList {
        add(startPoint.lat)
        add(destPoint.lat)
        routePolyline.forEach { add(it.lat) }
        evaluations.forEach { add(it.station.lat) }
    }
    val allLongitudes = buildList {
        add(startPoint.lng)
        add(destPoint.lng)
        routePolyline.forEach { add(it.lng) }
        evaluations.forEach { add(it.station.lng) }
    }

    val minLat = allLatitudes.minOrNull() ?: 48.0
    val maxLat = allLatitudes.maxOrNull() ?: 49.0
    val minLng = allLongitudes.minOrNull() ?: 10.0
    val maxLng = allLongitudes.maxOrNull() ?: 11.0
    val midLatRad = Math.toRadians((minLat + maxLat) / 2.0)
    val lngScale = cos(midLatRad).coerceAtLeast(0.45)

    val latSpan = max(0.04, (maxLat - minLat) * 1.38)
    val lngSpan = max(0.05, (maxLng - minLng) * 1.38)
    val centerLat = (minLat + maxLat) / 2.0
    val centerLng = (minLng + maxLng) / 2.0

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(18.dp)
            ),
        color = Color(0xFF0F1923)
    ) {
        // Card expands downwards: Top = Unobstructed Map Canvas, Bottom = Full Vertical Station Details
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                zoom = (zoom * gestureZoom).coerceIn(0.8f, 4.5f)
                                panOffset += pan
                            }
                        }
                        .pointerInput(evaluations) {
                            detectTapGestures { tapOffset ->
                                val hit = pinTargets
                                    .map { target ->
                                        target to hypot(
                                            (tapOffset.x - target.center.x).toDouble(),
                                            (tapOffset.y - target.center.y).toDouble()
                                        )
                                    }
                                    .filter { (target, dist) -> dist <= target.radiusPx }
                                    .minByOrNull { it.second }
                                if (hit != null) {
                                    onSelectStation(hit.first.stationId)
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    val scaleX = (w * 0.80f) / (lngSpan * lngScale).toFloat()
                    val scaleY = (h * 0.74f) / latSpan.toFloat()
                    val baseScale = min(scaleX, scaleY) * zoom

                    fun toCanvas(lat: Double, lng: Double): Offset {
                        val dx = ((lng - centerLng) * lngScale).toFloat() * baseScale
                        val dy = -(lat - centerLat).toFloat() * baseScale
                        return Offset(
                            x = w / 2f + dx + panOffset.x,
                            y = h * 0.54f + dy + panOffset.y
                        )
                    }

                    // 1. Subtle Radar / Map Grid Background
                    val gridStep = 48.dp.toPx()
                    val gridColor = Color(0xFF1E2D3D)
                    var gx = (panOffset.x % gridStep)
                    while (gx < w) {
                        drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                        gx += gridStep
                    }
                    var gy = (panOffset.y % gridStep)
                    while (gy < h) {
                        drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                        gy += gridStep
                    }

                    // 2. Route Corridor & Polyline
                    if (routePolyline.size >= 2) {
                        val routePath = Path()
                        val firstPt = toCanvas(routePolyline.first().lat, routePolyline.first().lng)
                        routePath.moveTo(firstPt.x, firstPt.y)
                        for (i in 1 until routePolyline.size) {
                            val pt = toCanvas(routePolyline[i].lat, routePolyline[i].lng)
                            routePath.lineTo(pt.x, pt.y)
                        }

                        val corridorStrokePx = when (detourOption) {
                            DetourOption.KM_1 -> 22.dp.toPx()
                            DetourOption.KM_5 -> 36.dp.toPx()
                            DetourOption.KM_10 -> 50.dp.toPx()
                            DetourOption.KM_15 -> 64.dp.toPx()
                            DetourOption.KM_30 -> 82.dp.toPx()
                        } * zoom.coerceIn(0.8f, 1.8f)

                        drawPath(
                            path = routePath,
                            color = Color(0xFF38BDF8).copy(alpha = 0.13f),
                            style = Stroke(
                                width = corridorStrokePx,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        drawPath(
                            path = routePath,
                            color = Color(0xFF0EA5E9),
                            style = Stroke(
                                width = 5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // 3. Selected Station Detour Connector Line
                    if (selectedEval != null) {
                        val st = selectedEval.station
                        val routeAnchor = toCanvas(st.nearestRoutePoint.lat, st.nearestRoutePoint.lng)
                        val stationPos = toCanvas(st.lat, st.lng)

                        drawLine(
                            color = Color(0xFFFACC15),
                            start = routeAnchor,
                            end = stationPos,
                            strokeWidth = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                        drawCircle(
                            color = Color(0xFFFACC15),
                            radius = 4.5.dp.toPx(),
                            center = routeAnchor
                        )
                    }

                    // 4. Start (A) and Destination (B) Endpoints
                    val startPos = toCanvas(startPoint.lat, startPoint.lng)
                    val destPos = toCanvas(destPoint.lat, destPoint.lng)

                    drawCircle(Color(0xFF22C55E).copy(alpha = 0.28f), radius = 14.dp.toPx(), center = startPos)
                    drawCircle(Color(0xFF22C55E), radius = 7.dp.toPx(), center = startPos)

                    drawCircle(Color(0xFFF97316).copy(alpha = 0.28f), radius = 14.dp.toPx(), center = destPos)
                    drawCircle(Color(0xFFF97316), radius = 7.dp.toPx(), center = destPos)

                    val labelPaint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.WHITE
                        textSize = 11.sp.toPx()
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.DEFAULT,
                            android.graphics.Typeface.BOLD
                        )
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "START: ${startPoint.label.take(14)}",
                        startPos.x + 10.dp.toPx(),
                        startPos.y + 4.dp.toPx(),
                        labelPaint
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "ZIEL: ${destPoint.label.take(14)}",
                        destPos.x + 10.dp.toPx(),
                        destPos.y + 4.dp.toPx(),
                        labelPaint
                    )

                    // 5. Gas Station Price Pins
                    pinTargets.clear()
                    val orderedForDrawing = evaluations.sortedBy {
                        when {
                            it.station.id == selectedEval?.station?.id -> 2
                            it.rankByNetCost == 1 -> 1
                            else -> 0
                        }
                    }

                    val pinTextPaint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.WHITE
                        textSize = 11.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.DEFAULT,
                            android.graphics.Typeface.BOLD
                        )
                    }

                    for (eval in orderedForDrawing) {
                        val pos = toCanvas(eval.station.lat, eval.station.lng)
                        val isSelected = eval.station.id == selectedEval?.station?.id
                        val isBestNet = eval.rankByNetCost == 1

                        val pinBgColor = when (eval.verdictLevel) {
                            VerdictLevel.BEST_NET_CHOICE -> Color(0xFF16A34A)
                            VerdictLevel.WORTH_DETOUR -> Color(0xFF0D9488)
                            VerdictLevel.ON_ROUTE_BASELINE -> Color(0xFF2563EB)
                            VerdictLevel.DETOUR_TOO_EXPENSIVE -> Color(0xFFDC2626)
                        }

                        val pillW = if (isSelected || isBestNet) 76.dp.toPx() else 64.dp.toPx()
                        val pillH = if (isSelected) 28.dp.toPx() else 24.dp.toPx()
                        val topLeft = Offset(pos.x - pillW / 2f, pos.y - pillH - 6.dp.toPx())

                        drawLine(
                            color = if (isSelected) Color.White else pinBgColor,
                            start = pos,
                            end = Offset(pos.x, pos.y - 6.dp.toPx()),
                            strokeWidth = 2.5.dp.toPx()
                        )
                        drawCircle(
                            color = if (isSelected) Color.White else pinBgColor,
                            radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                            center = pos
                        )

                        if (isSelected) {
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(topLeft.x - 2.dp.toPx(), topLeft.y - 2.dp.toPx()),
                                size = Size(pillW + 4.dp.toPx(), pillH + 4.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                        drawRoundRect(
                            color = pinBgColor,
                            topLeft = topLeft,
                            size = Size(pillW, pillH),
                            cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                        )

                        val prefix = if (isBestNet) "★ " else ""
                        val label = "$prefix${eval.formattedLiterPriceMain}${eval.formattedPriceSuperscript}"
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            pos.x,
                            topLeft.y + pillH * 0.68f,
                            pinTextPaint
                        )

                        pinTargets.add(
                            PinHitTarget(
                                stationId = eval.station.id,
                                center = Offset(pos.x, topLeft.y + pillH / 2f),
                                radiusPx = max(pillW, pillH) * 0.65f
                            )
                        )
                    }
                }

                // Top Map Legend & Reset Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xCC0F172A),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendDot(Color(0xFF16A34A), "Netto-Sieger")
                            LegendDot(Color(0xFF2563EB), "Direkt")
                            LegendDot(Color(0xFFDC2626), "Umweg zu teuer")
                        }
                    }

                    Surface(
                        color = Color(0xCC1E293B),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable {
                            zoom = 1f
                            panOffset = Offset.Zero
                        }
                    ) {
                        Text(
                            text = "⟲ Zentrieren (${detourOption.label})",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Requirement #4 & #5: Selected Station Info BELOW the map (expanding downwards, all infos & button stacked vertically!)
            if (selectedEval != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF172331)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (selectedEval.rankByNetCost == 1) {
                                "★ Ausgewählte Tankstelle (#1 Netto-Sieger)"
                            } else {
                                "📍 Ausgewählte Tankstelle (#${selectedEval.rankByNetCost} Netto)"
                            },
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = selectedEval.station.name,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        if (selectedEval.station.fullAddress.isNotBlank()) {
                            Text(
                                text = "Adresse: ${selectedEval.station.fullAddress} (Route km ${selectedEval.station.distanceFromStartKm.roundToInt()})",
                                color = Color(0xFFCBD5E1),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "Preis (${selectedEval.fuelType.displayName}): ${selectedEval.formattedLiterPriceMain}${selectedEval.formattedPriceSuperscript} € / Liter",
                            color = Color(0xFF4ADE80),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Umweg: +${SpritRadarRepository.formatKm(selectedEval.station.detourKm)} (~${selectedEval.station.detourMinutes} Min. zusätzliche Fahrzeit)",
                            color = Color(0xFFE2E8F0),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Rechnung (${selectedEval.tankAmountLiters.roundToInt()} L): Tankfüllung ${SpritRadarRepository.formatEuro(selectedEval.grossFuelCostEuro)} + Umweg-Sprit (${String.format(Locale.GERMANY, "%.2f", selectedEval.detourExtraLiters)} L = ${SpritRadarRepository.formatEuro(selectedEval.detourFuelCostEuro)}) → Effektiv ${SpritRadarRepository.formatEuro(selectedEval.effectiveTotalCostEuro)}",
                            color = Color(0xFFE2E8F0),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "${selectedEval.recommendationHeadline} – ${selectedEval.recommendationMessage}",
                            color = Color(0xFFFDE047),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { onNavigateStation(selectedEval) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22C55E),
                                contentColor = Color(0xFF052E16)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "🧭 Mit Google Maps navigieren",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            color = Color(0xFFE2E8F0),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
