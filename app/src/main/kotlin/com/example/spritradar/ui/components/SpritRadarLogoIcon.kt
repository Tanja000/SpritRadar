package com.example.spritradar.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.spritradar.R

@Composable
fun SpritRadarLogoIcon(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_spritradar_icon),
        contentDescription = "SpritRadar Icon",
        modifier = modifier.size(size)
    )
}
