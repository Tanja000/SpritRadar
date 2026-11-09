package com.lilapause.spritradar.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.delay

/**
 * Offizielle In-App-Produkt-IDs (SKUs) für die Google Play Console unter
 * „Monetarisierung → Produkte → In-App-Produkte“.
 */
enum class SupportTier(
    val productId: String,
    val emoji: String,
    val title: String,
    val defaultPriceFormatted: String
) {
    COFFEE_2(
        productId = "support_coffee_2",
        emoji = "☕",
        title = "Kleiner Kaffee",
        defaultPriceFormatted = "2,00 €"
    ),
    FUEL_5(
        productId = "support_fuel_5",
        emoji = "⛽",
        title = "Tank-Bonus",
        defaultPriceFormatted = "5,00 €"
    ),
    BOOST_10(
        productId = "support_boost_10",
        emoji = "🚀",
        title = "Super-Support",
        defaultPriceFormatted = "10,00 €"
    )
}

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
        subline = "Bequem & sicher per Google Play In-App-Unterstützung",
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
 * Kompaktes, nicht-störendes Werbefenster am unteren Bildschirmrand.
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
 * Offizieller Google Play In-App Billing Unterstützungs-/Spenden-Dialog.
 * Erfüllt zu 100 % die Google Play Zahlungsrichtlinie (Google Play Payments Policy), da ausschließlich
 * das offizielle Google Play Abrechnungssystem (Google Pay, Kredit-/Debitkarte, Klarna, Handyrechnung,
 * Play-Guthaben) über BillingClient genutzt wird.
 */
@Composable
fun DonationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTier by remember { mutableStateOf(SupportTier.FUEL_5) }
    var productDetailsMap by remember { mutableStateOf<Map<String, ProductDetails>>(emptyMap()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }

    val billingClient = remember(context) {
        var clientRef: BillingClient? = null
        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (!purchases.isNullOrEmpty()) {
                        clientRef?.let { client ->
                            purchases.forEach { purchase ->
                                consumeSupportPurchaseIfNeeded(client, purchase)
                            }
                        }
                        isSuccessMessage = true
                        statusMessage = "❤️ Vielen herzlichen Dank für deine Unterstützung über Google Play!"
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    isSuccessMessage = false
                    statusMessage = "Vorgang abgebrochen – du kannst SpritRadar jederzeit weiter kostenlos nutzen."
                }
                else -> {
                    isSuccessMessage = false
                    statusMessage = "Google Play Abrechnung: ${billingResult.debugMessage.ifBlank { "Produkt wird nach Veröffentlichung im Play Store aktiv." }}"
                }
            }
        }

        BillingClient.newBuilder(context.applicationContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()
            .also { clientRef = it }
    }

    DisposableEffect(billingClient) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productList = SupportTier.entries.map { tier ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(tier.productId)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    }
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build()

                    billingClient.queryProductDetailsAsync(params) { result, detailsList ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK && detailsList.isNotEmpty()) {
                            productDetailsMap = detailsList.associateBy { it.productId }
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Reconnects on next action if needed
            }
        })

        onDispose {
            try {
                billingClient.endConnection()
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

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
                        text = "Offizieller Google Play In-App-Support",
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
                    text = "Hilft dir SpritRadar dabei, beim Tanken bares Geld zu sparen? Mit einem kleinen freiwilligen Beitrag über den Google Play Store unterstützt du die Weiterentwicklung und Pflege der App!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "1. Unterstützungsstufe wählen:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SupportTier.entries.forEach { tier ->
                        val livePrice = productDetailsMap[tier.productId]
                            ?.oneTimePurchaseOfferDetails
                            ?.formattedPrice
                            ?: tier.defaultPriceFormatted

                        FilterChip(
                            selected = selectedTier == tier,
                            onClick = {
                                selectedTier = tier
                                statusMessage = null
                            },
                            label = {
                                Text(
                                    text = "${tier.emoji} $livePrice",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTier == tier) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // Detail-Box zur gewählten Stufe
                val activePrice = productDetailsMap[selectedTier.productId]
                    ?.oneTimePurchaseOfferDetails
                    ?.formattedPrice
                    ?: selectedTier.defaultPriceFormatted

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
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
                        Column {
                            Text(
                                text = "${selectedTier.emoji} ${selectedTier.title}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Einmaliger Beitrag (kein Abo)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = activePrice,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Offizieller Google Play Billing Button
                Button(
                    onClick = {
                        launchGooglePlayBillingPurchase(
                            context = context,
                            billingClient = billingClient,
                            tier = selectedTier,
                            cachedDetails = productDetailsMap[selectedTier.productId],
                            onStatusUpdate = { msg, success ->
                                statusMessage = msg
                                isSuccessMessage = success
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF01875F), // Google Play Grün
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Über Google Play unterstützen ($activePrice)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Übersicht der im Google Play Store zugelassenen Zahlungsmethoden
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "✓ Zugelassene Google Play Zahlungsmethoden:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Google Pay & Google Play-Guthaben / Geschenkkarten\n• Kredit- & Debitkarte (Visa, Mastercard)\n• Klarna & Mobilfunkabrechnung (Handyrechnung)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (statusMessage != null) {
                    val accent = if (isSuccessMessage) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                    Surface(
                        color = accent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage!!,
                            color = accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Text(
                    text = "Hinweis: Die Abrechnung erfolgt sicher und ausschließlich über das offizielle Abrechnungssystem des Google Play Stores (Google Ireland Limited). Alle Funktionen von SpritRadar sind auch ohne Unterstützung uneingeschränkt nutzbar.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}

private fun launchGooglePlayBillingPurchase(
    context: Context,
    billingClient: BillingClient,
    tier: SupportTier,
    cachedDetails: ProductDetails?,
    onStatusUpdate: (String, Boolean) -> Unit
) {
    val activity = context.findActivity()
    if (activity == null) {
        onStatusUpdate("Google Play Kaufdialog konnte nicht initialisiert werden.", false)
        return
    }

    if (!billingClient.isReady) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAndLaunchProduct(activity, billingClient, tier, onStatusUpdate)
                } else {
                    onStatusUpdate(
                        "Google Play Store Abrechnung auf diesem Gerät aktuell nicht verbunden (Code ${result.responseCode}). Sobald die App über Google Play installiert ist, öffnet sich hier das Google Play Zahlungsfenster.",
                        false
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                onStatusUpdate("Verbindung zum Google Play Store unterbrochen.", false)
            }
        })
        return
    }

    if (cachedDetails != null) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(cachedDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    } else {
        queryAndLaunchProduct(activity, billingClient, tier, onStatusUpdate)
    }
}

private fun queryAndLaunchProduct(
    activity: Activity,
    billingClient: BillingClient,
    tier: SupportTier,
    onStatusUpdate: (String, Boolean) -> Unit
) {
    val productList = listOf(
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId(tier.productId)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
    )
    val params = QueryProductDetailsParams.newBuilder()
        .setProductList(productList)
        .build()

    billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
        val details = productDetailsList.firstOrNull()
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && details != null) {
            activity.runOnUiThread {
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(details)
                                .build()
                        )
                    )
                    .build()
                billingClient.launchBillingFlow(activity, flowParams)
            }
        } else {
            activity.runOnUiThread {
                onStatusUpdate(
                    "Google Play Billing bereit (Produkt-ID: ‚${tier.productId}‘). Sobald die App in der Google Play Console veröffentlicht und das In-App-Produkt ‚${tier.productId}‘ aktiviert wurde, öffnet sich hier das offizielle Google Play Zahlungsfenster.",
                    false
                )
            }
        }
    }
}

internal fun consumeSupportPurchaseIfNeeded(
    billingClient: BillingClient,
    purchase: Purchase
) {
    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.consumeAsync(consumeParams) { _, _ -> }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
