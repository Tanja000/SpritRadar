package com.lilapause.spritradar.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.net.URLEncoder

private data class AdSlotItem(
    val icon: String,
    val headline: String,
    val subline: String,
    val ctaText: String,
    val targetUrl: String? = null,
    val opensDonation: Boolean = false
)

private val DEFAULT_AD_SLOTS = listOf(
    AdSlotItem(
        icon = "🚗",
        headline = "KFZ-Versicherung vergleichen & bis zu 350 € sparen",
        subline = "Unabhängiger Tarifcheck für dein Auto • Partner-Angebot",
        ctaText = "Info ↗",
        targetUrl = "https://www.google.com/search?q=kfz+versicherung+vergleich"
    ),
    AdSlotItem(
        icon = "⛽",
        headline = "Eco-Tipp: Reifendruck +0,2 bar spart bis zu 3 % Sprit",
        subline = "Mehr Reichweite auf jeder Pendelstrecke • Mobilitäts-Partner",
        ctaText = "Mehr ↗",
        targetUrl = "https://www.adac.de/verkehr/tanken-kraftstoff-antrieb/tipps-zum-spritsparen/"
    ),
    AdSlotItem(
        icon = "☕",
        headline = "Gefällt dir SpritRadar? Unterstütze die Weiterentwicklung",
        subline = "Schon ab 2 € für einen Kaffee helfen beim Betrieb der App",
        ctaText = "Spenden",
        opensDonation = true
    ),
    AdSlotItem(
        icon = "🛠️",
        headline = "Pannenhilfe & Autoservice in deiner Nähe finden",
        subline = "Werkstatt- & Reifenservice entlang deiner Route",
        ctaText = "Öffnen ↗",
        targetUrl = "https://www.google.com/maps/search/Autowerkstatt/"
    )
)

/**
 * Kompaktes, nicht-störendes Werbefenster am unteren Bildschirmrand (52.dp Höhe).
 * Verdeckt durch Platzierung in der Scaffold-bottomBar keinerlei Bedienelemente oder Karteninhalte
 * und ist gemäß § 6 Abs. 1 DDG / Google Play Ads-Richtlinie eindeutig als „Anzeige“ gekennzeichnet.
 */
@Composable
fun NonIntrusiveAdBanner(
    onOpenDonationDialog: () -> Unit,
    onOpenAdInfoDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentAdIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(12_000L)
            currentAdIndex = (currentAdIndex + 1) % DEFAULT_AD_SLOTS.size
        }
    }

    val currentAd = DEFAULT_AD_SLOTS[currentAdIndex]

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        tonalElevation = 3.dp,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (currentAd.opensDonation) {
                        onOpenDonationDialog()
                    } else if (currentAd.targetUrl != null) {
                        openExternalBrowser(context, currentAd.targetUrl)
                    }
                }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Gesetzlich vorgeschriebene Kennzeichnung „Anzeige“ (§ 6 Abs. 1 DDG)
                Surface(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Anzeige",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = currentAd.icon,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentAd.headline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentAd.subline,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = currentAd.ctaText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Kleines Info-Symbol zur Werbekennzeichnung
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onOpenAdInfoDialog() }
                ) {
                    Text(
                        text = "ⓘ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Dialog für freiwillige Spenden zur Unterstützung von SpritRadar (Tanja Reize, tanjareize@web.de).
 */
@Composable
fun DonationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val amounts = listOf(2, 5, 10, 0) // 0 = freier Betrag
    var selectedAmount by remember { mutableIntStateOf(5) }
    var copiedMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "☕", fontSize = 24.sp)
                Column {
                    Text(
                        text = "SpritRadar unterstützen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Freiwillige Spende an Tanja Reize",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Hilft dir SpritRadar dabei, beim Tanken bares Geld zu sparen? Ich entwickle und pflege die App unabhängig. Über eine kleine freiwillige Spende (z. B. für einen Kaffee ☕) freue ich mich riesig!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Wähle einen Wunschbetrag:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    amounts.forEach { euro ->
                        val label = when (euro) {
                            2 -> "☕ 2 €"
                            5 -> "⛽ 5 €"
                            10 -> "🚀 10 €"
                            else -> "💝 Frei"
                        }
                        FilterChip(
                            selected = selectedAmount == euro,
                            onClick = { selectedAmount = euro },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedAmount == euro) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // 1. Direkt via PayPal spenden
                Button(
                    onClick = {
                        launchPayPalDonation(context, selectedAmount)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0070BA),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedAmount > 0) {
                            "💙 $selectedAmount € via PayPal spenden"
                        } else {
                            "💙 Freien Betrag via PayPal spenden"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // 2. E-Mail-Adresse für PayPal-App kopieren
                OutlinedButton(
                    onClick = {
                        copyToClipboard(context, "tanjareize@web.de")
                        copiedMessage = "✓ Empfänger-Adresse (tanjareize@web.de) in die Zwischenablage kopiert!"
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📋 PayPal-Empfänger kopieren (tanjareize@web.de)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (copiedMessage != null) {
                    Surface(
                        color = Color(0xFF16A34A).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = copiedMessage!!,
                            color = Color(0xFF16A34A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // 3. Alternative per E-Mail (Überweisung / Gutschein)
                OutlinedButton(
                    onClick = {
                        val subject = URLEncoder.encode("Spende für SpritRadar App", "UTF-8").replace("+", "%20")
                        openExternalBrowser(context, "mailto:tanjareize@web.de?subject=$subject")
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✉ Kontakt per E-Mail (IBAN / Überweisung)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rechtlicher Hinweis: Die Unterstützung erfolgt vollkommen freiwillig ohne Gegenleistung. Alle Funktionen von SpritRadar stehen dir auch ohne Spende dauerhaft und uneingeschränkt zur Verfügung. Zahlungen über PayPal erfolgen extern über PayPal (Europe) S.à r.l. et Cie, S.C.A.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}

private fun launchPayPalDonation(context: Context, amountEuro: Int) {
    val baseUrl = buildString {
        append("https://www.paypal.com/cgi-bin/webscr?cmd=_donations")
        append("&business=tanjareize%40web.de")
        append("&item_name=Freiwillige+Unterstuetzung+fuer+SpritRadar+App")
        append("&currency_code=EUR")
        append("&lc=DE")
        if (amountEuro > 0) {
            append("&amount=$amountEuro.00")
        }
    }
    openExternalBrowser(context, baseUrl)
}

private fun copyToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("PayPal E-Mail", text))
    } catch (_: Exception) {
        // Ignore
    }
}

private fun openExternalBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Ignore if no browser available
    }
}
