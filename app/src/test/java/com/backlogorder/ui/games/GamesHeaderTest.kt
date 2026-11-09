package com.backlogorder.ui.games

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.backlogorder.ui.theme.BacklogTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GamesHeaderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersTitleAndSearchField() {
        composeTestRule.setContent {
            BacklogTheme {
                GamesHeader(
                    title = "My Games",
                    searchQuery = "",
                    onSearchChange = {},
                )
            }
        }

        composeTestRule.onNodeWithText("My Games").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search games...").assertIsDisplayed()
    }

    @Test
    fun triggersOnSearchChange() {
        var query = ""
        composeTestRule.setContent {
            BacklogTheme {
                GamesHeader(
                    title = "My Games",
                    searchQuery = query,
                    onSearchChange = { query = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Search games...").performTextInput("Zelda")
        assertEquals("Zelda", query)
    }

    @Test
    fun backNavigationClickedTriggersCallback() {
        var backClicked = false
        composeTestRule.setContent {
            BacklogTheme {
                GamesHeader(
                    title = "My Games",
                    searchQuery = "",
                    onSearchChange = {},
                    onBack = { backClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back navigation").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back navigation").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun editTitleClickedTriggersCallback() {
        var editClicked = false
        composeTestRule.setContent {
            BacklogTheme {
                GamesHeader(
                    title = "Favorites",
                    searchQuery = "",
                    onSearchChange = {},
                    onEditTitle = { editClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Edit title").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Edit title").performClick()
        assertTrue(editClicked)
    }

    @Test
    fun addGameClickedTriggersCallback() {
        var addClicked = false
        composeTestRule.setContent {
            BacklogTheme {
                GamesHeader(
                    title = "Favorites",
                    searchQuery = "",
                    onSearchChange = {},
                    onAddGame = { addClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Add Game").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Game").performClick()
        assertTrue(addClicked)
    }

    @Test
    fun filterButtonRendersAndIndicatesActiveState() {
        var filterClicked = false
        composeTestRule.setContent {
            BacklogTheme {
                GamesHeader(
                    title = "Favorites",
                    searchQuery = "",
                    onSearchChange = {},
                    onFilter = { filterClicked = true },
                    isFilterActive = true,
                )
            }
        }

        composeTestRule.onNodeWithText("Filter •").assertIsDisplayed()
        composeTestRule.onNodeWithText("Filter •").performClick()
        assertTrue(filterClicked)
    }
}
