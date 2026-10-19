package com.davi.dev.gnixia.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.material3.Typography
import com.davi.dev.gnixia.R

// Downloadable fonts: Google Play Services downloads and caches the font, so it isn't
// bundled in the APK. The certificates verify the provider (res/values/font_certs.xml)
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val interTight = GoogleFont("Inter Tight")

// While the font downloads (or if it fails), the system default font is used
val InterTightFontFamily = FontFamily(
    Font(googleFont = interTight, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = interTight, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = interTight, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = interTight, fontProvider = provider, weight = FontWeight.Bold)
)

// Typography(defaultFontFamily = ...) only fills styles without a font family, and the Wear
// defaults already set one, so the font is applied to every style explicitly
internal val GnixiaTypography: Typography = Typography().run {
    val curvedFont = CurvedTextStyle(fontFamily = InterTightFontFamily)
    fun TextStyle.withFont() = copy(fontFamily = InterTightFontFamily)

    Typography(
        arcLarge = arcLarge.merge(curvedFont),
        arcMedium = arcMedium.merge(curvedFont),
        arcSmall = arcSmall.merge(curvedFont),
        displayLarge = displayLarge.withFont(),
        displayMedium = displayMedium.withFont(),
        displaySmall = displaySmall.withFont(),
        titleLarge = titleLarge.withFont(),
        titleMedium = titleMedium.withFont(),
        titleSmall = titleSmall.withFont(),
        labelLarge = labelLarge.withFont(),
        labelMedium = labelMedium.withFont(),
        labelSmall = labelSmall.withFont(),
        bodyLarge = bodyLarge.withFont(),
        bodyMedium = bodyMedium.withFont(),
        bodySmall = bodySmall.withFont(),
        bodyExtraSmall = bodyExtraSmall.withFont(),
        numeralExtraLarge = numeralExtraLarge.withFont(),
        numeralLarge = numeralLarge.withFont(),
        numeralMedium = numeralMedium.withFont(),
        numeralSmall = numeralSmall.withFont(),
        numeralExtraSmall = numeralExtraSmall.withFont()
    )
}
