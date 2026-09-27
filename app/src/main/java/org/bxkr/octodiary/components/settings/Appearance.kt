package org.bxkr.octodiary.components.settings

import android.os.Build
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.graphics.Typeface as AndroidTypeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.R
import org.bxkr.octodiary.colorSchemeLive
import org.bxkr.octodiary.components.SwitchPreference
import org.bxkr.octodiary.components.ThemeCard
import org.bxkr.octodiary.darkThemeLive
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save
import org.bxkr.octodiary.ui.theme.AppearanceSettings
import org.bxkr.octodiary.ui.theme.CustomColorScheme
import org.bxkr.octodiary.ui.theme.appearanceSettingsLive
import org.bxkr.octodiary.ui.theme.followSystemThemeLive
import org.bxkr.octodiary.ui.theme.save
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun Appearance() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val selectedTheme by colorSchemeLive.observeAsState(-1)
    val followSystem by followSystemThemeLive.observeAsState(true)
    val selectedDark by darkThemeLive.observeAsState(isSystemInDarkTheme())
    val darkTheme = if (followSystem) isSystemInDarkTheme() else selectedDark
    val appearance by appearanceSettingsLive.observeAsState(AppearanceSettings())
    val compactState = remember { mutableStateOf(appearance.compactLayout) }
    var fontMenuExpanded by remember { mutableStateOf(false) }
    var fontImporting by remember { mutableStateOf(false) }
    var fontImportFailed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            fontImporting = true
            fontImportFailed = false
            val importedFont = runCatching { withContext(Dispatchers.IO) { importCustomFont(context, uri) } }
            importedFont.onSuccess { (fontFile, displayName) ->
                val updated = (appearanceSettingsLive.value ?: appearance).copy(
                    fontFamily = 4,
                    customFontPath = fontFile.absolutePath,
                    customFontName = displayName
                )
                appearanceSettingsLive.value = updated
                updated.save(context)
            }.onFailure {
                fontImportFailed = true
            }
            fontImporting = false
        }
    }

    fun applyAppearance(updated: AppearanceSettings, persist: Boolean = true) {
        appearanceSettingsLive.value = updated
        if (persist) updated.save(context)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Category(stringResource(R.string.appearance_palette)) {
            LazyRow {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    item {
                        val scheme = MaterialTheme.colorScheme
                        ThemeCard(
                            selectedTheme == -1,
                            scheme.primary,
                            scheme.secondary,
                            scheme.surfaceVariant,
                            Modifier.padding(start = 8.dp),
                            true
                        ) {
                            colorSchemeLive.value = -1
                            context.mainPrefs.save("theme" to -1)
                        }
                    }
                }
                items(CustomColorScheme.values().toList()) { scheme ->
                    val colors = if (darkTheme) scheme.darkColorScheme else scheme.lightColorScheme
                    ThemeCard(
                        selectedTheme == scheme.ordinal,
                        colors.primary,
                        colors.secondary,
                        colors.surfaceVariant
                    ) {
                        colorSchemeLive.value = scheme.ordinal
                        context.mainPrefs.save("theme" to scheme.ordinal)
                    }
                }
            }
        }

        Category(stringResource(R.string.appearance_theme_mode)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = followSystem,
                    onClick = {
                        followSystemThemeLive.value = true
                        context.mainPrefs.save("follow_system_theme" to true)
                    },
                    label = { Text(stringResource(R.string.appearance_system)) }
                )
                FilterChip(
                    selected = !followSystem && !selectedDark,
                    onClick = {
                        followSystemThemeLive.value = false
                        darkThemeLive.value = false
                        context.mainPrefs.save("follow_system_theme" to false, "is_dark_theme" to false)
                    },
                    label = { Text(stringResource(R.string.appearance_light)) }
                )
                FilterChip(
                    selected = !followSystem && selectedDark,
                    onClick = {
                        followSystemThemeLive.value = false
                        darkThemeLive.value = true
                        context.mainPrefs.save("follow_system_theme" to false, "is_dark_theme" to true)
                    },
                    label = { Text(stringResource(R.string.appearance_dark)) }
                )
            }
        }

        Category(stringResource(R.string.appearance_typography)) {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.appearance_font_family), style = MaterialTheme.typography.bodyLarge)
                Column {
                    OutlinedButton(onClick = { fontMenuExpanded = true }) {
                        Text(fontFamilyTitle(appearance.fontFamily))
                    }
                    DropdownMenu(expanded = fontMenuExpanded, onDismissRequest = { fontMenuExpanded = false }) {
                        listOf(
                            R.string.appearance_font_system,
                            R.string.appearance_font_serif,
                            R.string.appearance_font_mono,
                            R.string.appearance_font_cursive,
                            R.string.appearance_font_custom
                        ).forEachIndexed { index, title ->
                            DropdownMenuItem(
                                text = { Text(stringResource(title)) },
                                onClick = {
                                    fontMenuExpanded = false
                                    if (index == 4 && appearance.customFontPath.isNullOrBlank()) {
                                        fontPicker.launch(arrayOf("*/*"))
                                    } else applyAppearance(appearance.copy(fontFamily = index))
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { fontPicker.launch(arrayOf("*/*")) },
                        enabled = !fontImporting
                    ) {
                        Text(
                            stringResource(
                                if (appearance.customFontName.isNullOrBlank()) R.string.appearance_font_upload
                                else R.string.appearance_font_change
                            )
                        )
                    }
                    if (!appearance.customFontPath.isNullOrBlank()) {
                        TextButton(onClick = {
                            applyAppearance(appearance.copy(fontFamily = 0, customFontPath = null, customFontName = null))
                        }) { Text(stringResource(R.string.appearance_font_remove)) }
                    }
                }
                Text(
                    stringResource(R.string.appearance_font_formats),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (fontImporting) Text(stringResource(R.string.appearance_font_loading))
                if (fontImportFailed) {
                    Text(stringResource(R.string.appearance_font_error), color = MaterialTheme.colorScheme.error)
                }
                appearance.customFontName?.let { fontName ->
                    Text(fontName, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                }

                AppearanceSlider(
                    title = stringResource(R.string.appearance_text_size),
                    value = appearance.textScale,
                    valueLabel = "${(appearance.textScale * 100).toInt()}%",
                    range = .8f..1.4f,
                    steps = 11,
                    onValueChange = { applyAppearance(appearanceSettingsLive.value!!.copy(textScale = it), false) },
                    onFinished = { appearanceSettingsLive.value?.save(context) }
                )
                AppearanceSlider(
                    title = stringResource(R.string.appearance_line_height),
                    value = appearance.lineHeightScale,
                    valueLabel = "${(appearance.lineHeightScale * 100).toInt()}%",
                    range = .8f..1.4f,
                    steps = 11,
                    onValueChange = { applyAppearance(appearanceSettingsLive.value!!.copy(lineHeightScale = it), false) },
                    onFinished = { appearanceSettingsLive.value?.save(context) }
                )
                AppearanceSlider(
                    title = stringResource(R.string.appearance_letter_spacing),
                    value = appearance.letterSpacingScale,
                    valueLabel = "${(appearance.letterSpacingScale * 100).toInt()}%",
                    range = 0f..2f,
                    steps = 9,
                    onValueChange = { applyAppearance(appearanceSettingsLive.value!!.copy(letterSpacingScale = it), false) },
                    onFinished = { appearanceSettingsLive.value?.save(context) }
                )
                AppearanceSlider(
                    title = stringResource(R.string.appearance_font_weight),
                    value = appearance.fontWeightBoost.toFloat(),
                    valueLabel = "+${appearance.fontWeightBoost}",
                    range = 0f..200f,
                    steps = 3,
                    onValueChange = { applyAppearance(appearanceSettingsLive.value!!.copy(fontWeightBoost = it.toInt()), false) },
                    onFinished = { appearanceSettingsLive.value?.save(context) }
                )
            }
        }

        Category(stringResource(R.string.appearance_layout)) {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppearanceSlider(
                    title = stringResource(R.string.appearance_corner_roundness),
                    value = appearance.cornerScale,
                    valueLabel = "${(appearance.cornerScale * 100).toInt()}%",
                    range = 0f..2f,
                    steps = 9,
                    onValueChange = { applyAppearance(appearanceSettingsLive.value!!.copy(cornerScale = it), false) },
                    onFinished = { appearanceSettingsLive.value?.save(context) }
                )
                SwitchPreference(
                    title = stringResource(R.string.appearance_compact),
                    description = stringResource(R.string.appearance_compact_desc),
                    listenState = compactState
                ) { enabled ->
                    compactState.value = enabled
                    applyAppearance(appearance.copy(compactLayout = enabled))
                }

                Text(stringResource(R.string.appearance_presets), style = MaterialTheme.typography.bodyLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        compactState.value = true
                        applyAppearance(appearance.copy(
                            textScale = .92f,
                            lineHeightScale = .92f,
                            letterSpacingScale = .9f,
                            cornerScale = .75f,
                            compactLayout = true
                        ))
                    }) { Text(stringResource(R.string.appearance_compact_preset)) }
                    Button(onClick = {
                        compactState.value = false
                        applyAppearance(appearance.copy(
                            textScale = 1.12f,
                            lineHeightScale = 1.16f,
                            letterSpacingScale = 1.15f,
                            cornerScale = 1.25f,
                            compactLayout = false
                        ))
                    }) { Text(stringResource(R.string.appearance_spacious_preset)) }
                }
            }
        }

        OutlinedButton(
            onClick = {
                compactState.value = false
                applyAppearance(AppearanceSettings())
                colorSchemeLive.value = -1
                followSystemThemeLive.value = true
                darkThemeLive.value = false
                context.mainPrefs.save(
                    "theme" to -1,
                    "follow_system_theme" to true,
                    "is_dark_theme" to null
                )
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) { Text(stringResource(R.string.appearance_reset)) }
    }
}

@Composable
private fun AppearanceSlider(
    title: String,
    value: Float,
    valueLabel: String,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    onFinished: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(valueLabel, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            onValueChangeFinished = onFinished,
            valueRange = range,
            steps = steps
        )
    }
}

@Composable
private fun fontFamilyTitle(index: Int) = stringResource(
    when (index) {
        1 -> R.string.appearance_font_serif
        2 -> R.string.appearance_font_mono
        3 -> R.string.appearance_font_cursive
        4 -> R.string.appearance_font_custom
        else -> R.string.appearance_font_system
    }
)

private fun importCustomFont(context: Context, uri: Uri): Pair<File, String> {
    val displayName = context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    } ?: "custom-font.ttf"
    val extension = displayName.substringAfterLast('.', "").lowercase()
    require(extension == "ttf" || extension == "otf") { "Unsupported font format" }

    val directory = File(context.filesDir, "custom-fonts").apply { mkdirs() }
    val temporaryFile = File(directory, "font-import.tmp")
    val fontFile = File(directory, "user-font.$extension")
    try {
        val input = requireNotNull(context.contentResolver.openInputStream(uri)) { "Cannot open font file" }
        input.use { source ->
            FileOutputStream(temporaryFile).use { destination ->
                val buffer = ByteArray(8192)
                var totalBytes = 0L
                while (true) {
                    val count = source.read(buffer)
                    if (count < 0) break
                    totalBytes += count
                    require(totalBytes <= 30L * 1024 * 1024) { "Font file is too large" }
                    destination.write(buffer, 0, count)
                }
                require(totalBytes > 0) { "Font file is empty" }
            }
        }
        AndroidTypeface.createFromFile(temporaryFile.absolutePath)
        if (fontFile.exists()) fontFile.delete()
        check(temporaryFile.renameTo(fontFile)) { "Cannot save font file" }
        directory.listFiles()?.filter { it.name.startsWith("user-font.") && it != fontFile }?.forEach { it.delete() }
        return fontFile to displayName.take(100)
    } catch (error: Exception) {
        temporaryFile.delete()
        throw error
    }
}
