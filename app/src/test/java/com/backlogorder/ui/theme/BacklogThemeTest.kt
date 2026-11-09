package com.backlogorder.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import com.backlogorder.domain.model.ThemeId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BacklogThemeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun paletteChangesWithThemeSelection() {
        var capturedAccent: Color? = null

        composeTestRule.setContent {
            BacklogTheme(themeId = ThemeId.EMERALD, darkTheme = false) {
                capturedAccent = LocalHudColors.current.accent
            }
        }

        assertEquals(Color(0xFF059669), capturedAccent)
    }

    @Test
    fun nestedBacklogThemeInheritsParentThemeId() {
        var outerAccent: Color? = null
        var innerAccent: Color? = null

        composeTestRule.setContent {
            BacklogTheme(themeId = ThemeId.CRIMSON, darkTheme = false) {
                outerAccent = LocalHudColors.current.accent
                // Nested BacklogTheme call without explicit themeId parameter
                BacklogTheme {
                    innerAccent = LocalHudColors.current.accent
                }
            }
        }

        assertEquals(Color(0xFFDC2626), outerAccent)
        assertEquals(Color(0xFFDC2626), innerAccent)
    }
}
