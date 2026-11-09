package com.backlogorder.ui.hud

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.backlogorder.domain.model.GameStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HudComponentsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun statusBadgeShowsStatusText() {
        composeTestRule.setContent {
            StatusBadge(status = GameStatus.PLAYING)
        }

        composeTestRule.onNodeWithText("PLAYING").assertIsDisplayed()
    }

    @Test
    fun emptyStateShowsTitleAndPrimaryAction() {
        composeTestRule.setContent {
            EmptyState(
                title = "No games yet",
                message = "Add something to start tracking it.",
                action = {
                    Button(onClick = {}) {
                        Text("Create list")
                    }
                },
            )
        }

        composeTestRule.onNodeWithText("No games yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create list").assertIsDisplayed()
    }

    @Test
    fun gameCardRendersScoreAndMetadata() {
        composeTestRule.setContent {
            GameCard(
                title = "Celeste",
                status = GameStatus.PLAYING,
                score = 88,
                meta = "Platformer • 5h",
            )
        }

        composeTestRule.onNodeWithText("Celeste").assertIsDisplayed()
        composeTestRule.onNodeWithText("Score 88.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Platformer • 5h").assertIsDisplayed()
    }
}
