package org.bxkr.octodiary.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

fun Typography.withAppearance(settings: AppearanceSettings): Typography {
    val family = settings.fontFamily()
    fun adjust(style: TextStyle) = style.copy(
        fontFamily = family,
        fontSize = style.fontSize * settings.textScale,
        lineHeight = style.lineHeight * settings.lineHeightScale,
        letterSpacing = style.letterSpacing * settings.letterSpacingScale,
        fontWeight = FontWeight((style.fontWeight ?: FontWeight.Normal).weight
            .plus(settings.fontWeightBoost).coerceIn(100, 900))
    )

    return copy(
        displayLarge = adjust(displayLarge),
        displayMedium = adjust(displayMedium),
        displaySmall = adjust(displaySmall),
        headlineLarge = adjust(headlineLarge),
        headlineMedium = adjust(headlineMedium),
        headlineSmall = adjust(headlineSmall),
        titleLarge = adjust(titleLarge),
        titleMedium = adjust(titleMedium),
        titleSmall = adjust(titleSmall),
        bodyLarge = adjust(bodyLarge),
        bodyMedium = adjust(bodyMedium),
        bodySmall = adjust(bodySmall),
        labelLarge = adjust(labelLarge),
        labelMedium = adjust(labelMedium),
        labelSmall = adjust(labelSmall)
    )
}
