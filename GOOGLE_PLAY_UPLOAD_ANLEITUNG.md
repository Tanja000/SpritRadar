# Google Play Console – Upload-Checkliste & Ausfüllhilfe für SpritRadar

**App-Name:** SpritRadar  
**Paketname (Application ID):** `com.lilapause.spritradar`  
**Version:** `1.0` (`versionCode = 1`)  
**Target SDK:** Android 16 (API 36) / Min SDK: Android 7.0 (API 24)

---

## 1. Dateien für den Upload im Hauptverzeichnis (`SpritRadar/`)

1. **[`SpritRadar.aab`](file:///c:/Users/tanja/.gemini/antigravity/projects/SpritRadar/SpritRadar.aab)** *(WICHTIG für Google Play Console!)*  
   Signiertes **Android App Bundle (`.aab`)** für den direkten Upload in der Google Play Console unter *„Produktion“ → „Neuen Release erstellen“* (bzw. *„Interner Test“ / „Geschlossener Test“*).
2. **[`SpritRadar.apk`](file:///c:/Users/tanja/.gemini/antigravity/projects/SpritRadar/SpritRadar.apk)**  
   Signierte Release-APK zur direkten Installation auf dem eigenen Smartphone.
3. **[`index.html`](file:///c:/Users/tanja/.gemini/antigravity/projects/SpritRadar/index.html)**  
   Vollständige **Datenschutzerklärung (DSGVO)** & **Impressum (§ 5 DDG)** mit deiner Adresse (*Tanja Reize, Wilhelm-Busch-Str. 41, 81477 München, tanjareize@web.de*).
   - **Hinweis für Google Play:** Google Play verlangt im Abschnitt *„App-Inhalte → Datenschutzerklärung“* eine öffentlich erreichbare `https://`-URL. Lade die Datei `index.html` einfach kostenlos auf deinen Webspace, GitHub Pages oder Netlify hoch und trage den Link dort ein.
4. **Store-Grafiken im Ordner `playstore_assets/`:**
   - [`playstore-icon-512x512.png`](file:///c:/Users/tanja/.gemini/antigravity/projects/SpritRadar/playstore_assets/playstore-icon-512x512.png) – Hochauflösendes App-Symbol (512 × 512 px)
   - [`playstore-feature-graphic-1024x500.png`](file:///c:/Users/tanja/.gemini/antigravity/projects/SpritRadar/playstore_assets/playstore-feature-graphic-1024x500.png) – Funktionsgrafik / Feature Graphic (1024 × 500 px)
5. **Upload-Keystore (unbedingt sicher aufbewahren!):**
   - Datei: [`spritradar-upload-keystore.jks`](file:///c:/Users/tanja/.gemini/antigravity/projects/SpritRadar/spritradar-upload-keystore.jks)
   - Zugangsdaten in [`keystore.properties`](file:///c:/Users/tanja/.gemini/antigravity/projects/SpritRadar/keystore.properties):
     - **Key Alias:** `spritradar_upload`
     - **Store- / Key-Passwort:** `SpritRadar2026!`

---

## 2. Kopiervorlagen für den Haupteintrag im Google Play Store

### App-Name (max. 30 Zeichen)
```text
SpritRadar: Günstig tanken
```

### Kurzbeschreibung (max. 80 Zeichen)
```text
Günstigste Tankstellen auf deiner Route inkl. Umweg-Rechner & 24h-Preis-Ampel.
```

### Vollständige Beschreibung (max. 4.000 Zeichen)
```text
Mit SpritRadar findest du sekundenschnell die wirklich günstigste Tankstelle entlang deiner Route oder in deiner Umgebung – inklusive ehrlicher Umweg- und Verbrauchsberechnung!

WARUM SPRITRADAR?
Eine Tankstelle ist 2 Cent pro Liter günstiger, liegt aber 8 km abseits deiner Strecke? SpritRadar rechnet deinen individuellen Fahrzeugverbrauch und deine Tankmenge automatisch gegen die Umwegkosten auf und zeigt dir sofort deine echte Netto-Ersparnis in Euro.

DIE WICHTIGSTEN FUNKTIONEN:
• Tankstellen entlang deiner Route: Gib Start und Ziel ein (oder nutze deinen aktuellen GPS-Standort) und wähle deinen maximalen Umweg (1, 5, 10, 15 oder 30 km).
• Integrierter Umweg- & Verbrauchsrechner: Berücksichtigt deinen Durchschnittsverbrauch (L/100 km) und deine geplante Tankmenge für Super E10, Super E5 und Diesel.
• Historische 24h-Preis-Ampel: Sieh auf einen Blick (Rot / Gelb / Grün), ob die Preise aktuell überdurchschnittlich hoch sind oder ob du das Bestpreis-Zeitfenster erreicht hast.
• Übersichtliche Routenkarte: Interaktive Karte mit deiner Strecke, allen Tankstellen-Markern und detaillierter Ersparnis-Analyse.
• 1-Klick-Navigation: Starte die Zielführung zur gewählten Tankstelle direkt mit Google Maps.
• Minimalistisch & Privatsphäre-freundlich: Kein Nutzerkonto, keine Werbung, kein Tracking im Hintergrund.

DATENQUELLE & AKTUALITÄT:
Alle Kraftstoffpreise basieren auf den offiziellen Echtzeitdaten von Tankerkönig / Markttransparenzstelle für Kraftstoffe (MTS-K) unter der Lizenz CC BY 4.0 und werden alle 5 Minuten aktualisiert.
```

---

## 3. Ausfüllhilfe für „App-Inhalte“ (App Content) in der Play Console

1. **Datenschutzerklärung:** URL zu deiner hochgeladenen `index.html` eintragen.
2. **Werbung (Ads):** *„Nein, meine App enthält keine Werbung.“*
3. **App-Zugriff:** *„Alle Funktionen sind ohne besonderen Zugriff verfügbar“* (kein Login nötig).
4. **Inhaltseinstufung (IARC):** Kategorie *„Dienstprogramme, Produktivität, Kommunikation oder Sonstiges“* → Alle Fragen zu Gewalt/Jugendschutz/Glücksspiel mit *„Nein“* beantworten (ergibt **USK 0 / PEGI 3**).
5. **Zielgruppe und Inhalte:** Zielaltersgruppe **18 und älter** (da die App für Autofahrer konzipiert ist; *„Nein“* bei „Könnte die App unbeabsichtigt Kinder ansprechen?“).
6. **Nachrichten-App / Finanzfunktionen / Gesundheit / Behörden-App:** Jeweils *„Nein“*.
7. **Formular „Datensicherheit“ (Data Safety):**
   - *Erhebt oder teilt deine App erforderliche Nutzerdaten?* → **Ja** (nur Standort bei Nutzung der GPS-Funktion zur Routen-/Tankstellensuche im Vordergrund, wird **nicht** dauerhaft gespeichert und **nicht** zu Werbezwecken geteilt).
   - *Werden alle erhobenen Nutzerdaten während der Übertragung verschlüsselt?* → **Ja** (HTTPS).
   - *Bietest du Nutzern eine Möglichkeit, das Löschen ihrer Daten zu beantragen?* → **Nicht erforderlich, da keine Nutzerkonten erstellt werden und Standortdaten nur flüchtig (ephemer) für die unmittelbare Suchanfrage verarbeitet werden.**
   - **Datentypen auswählen:** *Standort → Ungefährer Standort & Genauer Standort*:
     - *Erhoben / Verarbeitet:* Ja, **nur flüchtig (ephemer)** verarbeitet.
     - *Optional:* Ja (Nutzer können Orte auch manuell eingeben).
     - *Zweck:* **App-Funktionalität** (Ermittlung des Startpunkts für Tankstellen entlang der Route).
