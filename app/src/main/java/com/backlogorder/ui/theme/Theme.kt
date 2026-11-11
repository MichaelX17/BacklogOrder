package com.backlogorder.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.backlogorder.domain.model.ThemeId

@Immutable
data class HudColors(
    val accent: Color,
    val accentSoft: Color,
    val panel: Color,
    val background: Color,
    val surface: Color,
    val navButtonBg: Color,
    val text: Color,
    val textMuted: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
)

val LocalThemeId = compositionLocalOf<ThemeId?> { null }
val LocalIsDarkMode = compositionLocalOf { true }
val LocalCustomColorHex = compositionLocalOf { "7045F5" }

val LocalHudColors = compositionLocalOf {
    HudColors(
        accent = VioletPrimary,
        accentSoft = VioletBright,
        panel = NavySecondaryBg,
        background = NavyBackground,
        surface = NavySurface,
        navButtonBg = Color(0xFF292452),
        text = TextPrimaryDark,
        textMuted = TextMutedDark,
        success = StatusSuccess,
        warning = StatusWarning,
        danger = StatusDanger,
    )
}

@Composable
fun BacklogTheme(
    themeId: ThemeId? = null,
    darkTheme: Boolean = LocalIsDarkMode.current,
    customColorHex: String = LocalCustomColorHex.current,
    content: @Composable () -> Unit,
) {
    val activeThemeId = themeId ?: LocalThemeId.current ?: ThemeId.VIOLET
    val palette = androidx.compose.runtime.remember(activeThemeId, darkTheme, customColorHex) {
        rememberPalette(activeThemeId, darkTheme, customColorHex)
    }
    val baseScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            secondary = palette.accentSoft,
            tertiary = palette.accentSoft,
            background = palette.background,
            surface = palette.surface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = palette.text,
            onSurface = palette.text,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            secondary = palette.accentSoft,
            tertiary = palette.accentSoft,
            background = palette.background,
            surface = palette.surface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = palette.text,
            onSurface = palette.text,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalHudColors provides palette,
        LocalThemeId provides activeThemeId,
        LocalIsDarkMode provides darkTheme,
        LocalCustomColorHex provides customColorHex,
    ) {
        MaterialTheme(
            colorScheme = baseScheme,
            typography = Typography,
            content = content,
        )
    }
}

@Composable
fun backlogOrderTheme(
    themeId: ThemeId? = null,
    darkTheme: Boolean = LocalIsDarkMode.current,
    customColorHex: String = LocalCustomColorHex.current,
    content: @Composable () -> Unit,
) {
    BacklogTheme(themeId = themeId, darkTheme = darkTheme, customColorHex = customColorHex, content = content)
}

fun parseHexColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#").trim()
    return try {
        val colorInt = if (cleanHex.length == 6) {
            android.graphics.Color.parseColor("#FF$cleanHex")
        } else if (cleanHex.length == 8) {
            android.graphics.Color.parseColor("#$cleanHex")
        } else {
            0xFF7045F5.toInt()
        }
        Color(colorInt)
    } catch (e: Exception) {
        Color(0xFF7045F5)
    }
}

private fun rememberPalette(themeId: ThemeId, darkTheme: Boolean, customColorHex: String): HudColors = when (themeId) {
    ThemeId.VIOLET -> if (darkTheme) {
        HudColors(
            accent = VioletPrimary,
            accentSoft = VioletBright,
            panel = Color(0xFF1D193E),
            background = Color(0xFF111027),
            surface = Color(0xFF110E28),
            navButtonBg = Color(0xFF292452),
            text = TextPrimaryDark,
            textMuted = TextMutedDark,
            success = StatusSuccess,
            warning = StatusWarning,
            danger = StatusDanger,
        )
    } else {
        HudColors(
            accent = Color(0xFF6D28D9),
            accentSoft = Color(0xFFE9D5FF),
            panel = LightSecondaryBg,
            background = LightBackground,
            surface = Color(0xFFF3F0FC),
            navButtonBg = Color(0xFFE2DCF8),
            text = TextPrimaryLight,
            textMuted = TextMutedLight,
            success = Color(0xFF16A34A),
            warning = Color(0xFF0284C7),
            danger = Color(0xFFDC2626),
        )
    }

    ThemeId.EMERALD -> if (darkTheme) {
        HudColors(
            accent = Color(0xFF10B981),
            accentSoft = Color(0xFF6EE7B7),
            panel = Color(0xFF102A23),
            background = Color(0xFF091912),
            surface = Color(0xFF0A1C17),
            navButtonBg = Color(0xFF183D33),
            text = Color(0xFFE6FFFB),
            textMuted = Color(0xFF9AE6B4),
            success = StatusSuccess,
            warning = StatusWarning,
            danger = StatusDanger,
        )
    } else {
        HudColors(
            accent = Color(0xFF059669),
            accentSoft = Color(0xFFDCFCE7),
            panel = Color(0xFFEFFCF6),
            background = Color(0xFFF7FFFB),
            surface = Color(0xFFE2F7ED),
            navButtonBg = Color(0xFFCFEFE3),
            text = Color(0xFF0F172A),
            textMuted = Color(0xFF4D7C6A),
            success = Color(0xFF16A34A),
            warning = Color(0xFF0284C7),
            danger = Color(0xFFDC2626),
        )
    }

    ThemeId.CRIMSON -> if (darkTheme) {
        HudColors(
            accent = Color(0xFFEF4444),
            accentSoft = Color(0xFFFCA5A5),
            panel = Color(0xFF2B1115),
            background = Color(0xFF1F0C10),
            surface = Color(0xFF1A0A0D),
            navButtonBg = Color(0xFF3F191F),
            text = Color(0xFFFFF1F2),
            textMuted = Color(0xFFF9A8D4),
            success = StatusSuccess,
            warning = StatusWarning,
            danger = StatusDanger,
        )
    } else {
        HudColors(
            accent = Color(0xFFDC2626),
            accentSoft = Color(0xFFFEE2E2),
            panel = Color(0xFFFEF2F2),
            background = Color(0xFFFFFBFB),
            surface = Color(0xFFFDE8E8),
            navButtonBg = Color(0xFFF9D2D2),
            text = Color(0xFF1F2937),
            textMuted = Color(0xFF6B7280),
            success = Color(0xFF16A34A),
            warning = Color(0xFF0284C7),
            danger = Color(0xFFDC2626),
        )
    }

    ThemeId.CUSTOM -> {
        val parsedAccent = parseHexColor(customColorHex)
        val parsedAccentSoft = parsedAccent.copy(alpha = 0.7f)
        if (darkTheme) {
            HudColors(
                accent = parsedAccent,
                accentSoft = parsedAccentSoft,
                panel = Color(0xFF222228),
                background = Color(0xFF121214),
                surface = Color(0xFF141418),
                navButtonBg = Color(0xFF2D2D35),
                text = Color(0xFFF3F3F6),
                textMuted = Color(0xFFA0A0AB),
                success = StatusSuccess,
                warning = StatusWarning,
                danger = StatusDanger,
            )
        } else {
            HudColors(
                accent = parsedAccent,
                accentSoft = parsedAccentSoft,
                panel = LightSecondaryBg,
                background = LightBackground,
                surface = Color(0xFFEFEFF8),
                navButtonBg = Color(0xFFE0E0EA),
                text = TextPrimaryLight,
                textMuted = TextMutedLight,
                success = Color(0xFF16A34A),
                warning = Color(0xFF0284C7),
                danger = Color(0xFFDC2626),
            )
        }
    }
}
