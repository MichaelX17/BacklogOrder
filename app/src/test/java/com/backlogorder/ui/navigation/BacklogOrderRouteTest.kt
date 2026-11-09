package com.backlogorder.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class BacklogOrderRouteTest {
    @Test
    fun routeDefinitionsAreStable() {
        assertEquals("onboarding", BacklogOrderRoute.Onboarding.route)
        assertEquals("home", BacklogOrderRoute.Home.route)
        assertEquals("search", BacklogOrderRoute.Search.route)
        assertEquals("settings", BacklogOrderRoute.Settings.route)
        assertEquals("manual_game", BacklogOrderRoute.Companion.manualGameRoute())
        assertEquals("manual_game?listId=abc", BacklogOrderRoute.Companion.manualGameRoute("abc"))
    }
}
