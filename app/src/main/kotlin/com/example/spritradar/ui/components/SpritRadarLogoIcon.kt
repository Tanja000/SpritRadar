package com.example.spritradar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SpritRadarLogoIcon(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(size * 0.26f)
    Canvas(
        modifier = modifier
            .size(size)
            .clip(shape)
            .border(
                width = 1.2.dp,
                color = Color(0xFF22D3EE).copy(alpha = 0.65f),
                shape = shape
            )
    ) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)

        // 1. Deep Navy to Emerald Gradient Background
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF040D1A),
                    Color(0xFF0A2239),
                    Color(0xFF064E3B)
                ),
                start = Offset.Zero,
                end = Offset(w, h)
            )
        )

        // 2. Radar Grid Rings & Crosshair
        drawCircle(
            color = Color(0xFF1E3A5F),
            radius = w * 0.44f,
            center = center,
            style = Stroke(width = w * 0.025f)
        )
        drawCircle(
            color = Color(0xFF22D3EE).copy(alpha = 0.55f),
            radius = w * 0.30f,
            center = center,
            style = Stroke(width = w * 0.03f)
        )

        // 3. Glowing Radar Sweep Sector (Top-Right)
        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF10B981).copy(alpha = 0.55f),
                    Color(0xFF38BDF8).copy(alpha = 0.12f)
                ),
                start = center,
                end = Offset(w * 0.85f, h * 0.15f)
            ),
            startAngle = -90f,
            sweepAngle = 58f,
            useCenter = true,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = Size(w * 0.84f, h * 0.84f)
        )

        // Radar Sweep Needle
        drawLine(
            color = Color(0xFF4ADE80),
            start = center,
            end = Offset(w * 0.78f, h * 0.25f),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round
        )

        // 4. Modern Fuel Pump Body
        val pumpLeft = w * 0.24f
        val pumpTop = h * 0.28f
        val pumpW = w * 0.34f
        val pumpH = h * 0.48f
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
            ),
            topLeft = Offset(pumpLeft, pumpTop),
            size = Size(pumpW, pumpH),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )

        // Pump Digital Display Window
        drawRoundRect(
            color = Color(0xFF082F49),
            topLeft = Offset(pumpLeft + w * 0.04f, pumpTop + h * 0.05f),
            size = Size(pumpW - w * 0.08f, pumpH * 0.32f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )

        // Green Price Line inside Display
        drawLine(
            color = Color(0xFF4ADE80),
            start = Offset(pumpLeft + w * 0.08f, pumpTop + h * 0.13f),
            end = Offset(pumpLeft + pumpW - w * 0.10f, pumpTop + h * 0.13f),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round
        )

        // Golden Fuel Drop inside lower pump body
        drawCircle(
            color = Color(0xFFFACC15),
            radius = w * 0.065f,
            center = Offset(pumpLeft + pumpW / 2f, pumpTop + pumpH * 0.70f)
        )

        // Fuel Hose on right side of pump
        val hosePath = Path().apply {
            moveTo(pumpLeft + pumpW, pumpTop + pumpH * 0.30f)
            cubicTo(
                w * 0.70f, pumpTop + pumpH * 0.32f,
                w * 0.70f, pumpTop + pumpH * 0.85f,
                w * 0.74f, pumpTop + pumpH * 0.75f
            )
            lineTo(w * 0.74f, pumpTop + pumpH * 0.38f)
        }
        drawPath(
            path = hosePath,
            color = Color(0xFFE2E8F0),
            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
        )

        // 5. Glowing Green Radar Target Blip (Top-Right)
        val blipCenter = Offset(w * 0.73f, h * 0.24f)
        drawCircle(
            color = Color(0xFF16A34A),
            radius = w * 0.11f,
            center = blipCenter
        )
        drawCircle(
            color = Color(0xFF4ADE80),
            radius = w * 0.08f,
            center = blipCenter
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.038f,
            center = blipCenter
        )
    }
}
