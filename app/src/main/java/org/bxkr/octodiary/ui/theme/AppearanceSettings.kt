package org.bxkr.octodiary.ui.theme

import android.content.Context
import android.graphics.Typeface as AndroidTypeface
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.MutableLiveData
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save

data class AppearanceSettings(
    val fontFamily: Int = 0,
    val textScale: Float = 1f,
    val lineHeightScale: Float = 1f,
    val letterSpacingScale: Float = 1f,
    val fontWeightBoost: Int = 0,
    val cornerScale: Float = 1f,
    val compactLayout: Boolean = false,
    val customFontPath: String? = null,
    val customFontName: String? = null
)

val appearanceSettingsLive = MutableLiveData(AppearanceSettings())
val followSystemThemeLive = MutableLiveData(true)

val LocalCompactLayout = staticCompositionLocalOf { false }

fun readAppearanceSettings(context: Context) = AppearanceSettings(
    fontFamily = context.mainPrefs.get<Int>("appearance_font_family") ?: 0,
    textScale = context.mainPrefs.get<Float>("appearance_text_scale") ?: 1f,
    lineHeightScale = context.mainPrefs.get<Float>("appearance_line_height") ?: 1f,
    letterSpacingScale = context.mainPrefs.get<Float>("appearance_letter_spacing") ?: 1f,
    fontWeightBoost = context.mainPrefs.get<Int>("appearance_font_weight") ?: 0,
    cornerScale = context.mainPrefs.get<Float>("appearance_corner_scale") ?: 1f,
    compactLayout = context.mainPrefs.get<Boolean>("appearance_compact_layout") ?: false,
    customFontPath = context.mainPrefs.get<String>("appearance_custom_font_path"),
    customFontName = context.mainPrefs.get<String>("appearance_custom_font_name")
)

fun AppearanceSettings.save(context: Context) {
    context.mainPrefs.save(
        "appearance_font_family" to fontFamily,
        "appearance_text_scale" to textScale,
        "appearance_line_height" to lineHeightScale,
        "appearance_letter_spacing" to letterSpacingScale,
        "appearance_font_weight" to fontWeightBoost,
        "appearance_corner_scale" to cornerScale,
        "appearance_compact_layout" to compactLayout,
        "appearance_custom_font_path" to customFontPath,
        "appearance_custom_font_name" to customFontName
    )
}

fun AppearanceSettings.fontFamily(): FontFamily {
    if (fontFamily == 4 && !customFontPath.isNullOrBlank()) {
        runCatching { FontFamily(AndroidTypeface.createFromFile(customFontPath)) }
            .getOrNull()?.let { return it }
    }
    return when (fontFamily) {
        1 -> FontFamily.Serif
        2 -> FontFamily.Monospace
        3 -> FontFamily.Cursive
        else -> FontFamily.Default
    }
}
