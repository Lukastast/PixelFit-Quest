package com.pixelfitquest.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.pixelfitquest.feature.health.QuestCenterHeader
import com.pixelfitquest.testing.ScreenshotTestBase
import com.pixelfitquest.ui.theme.PixelFitQuestTheme
import com.pixelfitquest.ui.theme.PlatinumWhite
import com.pixelfitquest.ui.theme.SlateDeep
import org.junit.Assert.*
import org.junit.Test
import org.robolectric.annotation.Config

class AppScaffoldNavigationTest : ScreenshotTestBase() {

    @Composable
    private fun TestNavShell(isLandscape: Boolean, onNavReady: (NavHostController) -> Unit) {
        val navController = rememberNavController().also(onNavReady)
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val currentTabArg = backStackEntry.observedTabArg()
        val host: @Composable (Modifier) -> Unit = { mod ->
            NavHost(navController = navController, startDestination = HOME_SCREEN, modifier = mod) {
                composable(HOME_SCREEN) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Screen: Home", color = PlatinumWhite) }
                }
                composable(
                    route = "$WORKOUTS_HISTORY_SCREEN?tab={tab}",
                    arguments = listOf(navArgument("tab") { type = NavType.StringType; nullable = true; defaultValue = null }),
                ) { entry ->
                    val tab = entry.observedTabArg() ?: "templates"
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Screen: Workouts ($tab)", color = PlatinumWhite)
                        // Mirrors WorkoutsHistoryScreen's in-screen tab buttons -> onTabSelected wiring in pixelFitGraph.
                        Text("In-screen History", color = PlatinumWhite, modifier = Modifier.clickable { entry.setWorkoutsTab("history") })
                    }
                }
                composable(HEALTH_CENTER_SCREEN) {
                    Column(Modifier.fillMaxSize()) {
                        QuestCenterHeader(
                            coins = 240, level = 14, isCompact = isLandscape,
                            onOpenSettings = { navController.navigate(SETTINGS_SCREEN) },
                            onOpenCustomization = { navController.navigate(CUSTOMIZATION_SCREEN) },
                        )
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Screen: Health", color = PlatinumWhite)
                        }
                    }
                }
                composable(SETTINGS_SCREEN) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Screen: Settings", color = PlatinumWhite) }
                }
                composable(CUSTOMIZATION_SCREEN) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Screen: Customization", color = PlatinumWhite) }
                }
                composable(WORKOUT_CUSTOMIZATION_SCREEN) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Screen: WorkoutBuilder", color = PlatinumWhite) }
                }
            }
        }
        if (isLandscape) {
            Row(Modifier.fillMaxSize().background(SlateDeep)) {
                host(Modifier.weight(1f).fillMaxHeight())
                PixelFitNavRail(currentRoute, { navController.navigateToBottomNav(it.route) }, selectedTabArg = currentTabArg)
            }
        } else {
            Column(Modifier.fillMaxSize().background(SlateDeep)) {
                host(Modifier.weight(1f).fillMaxWidth())
                PixelFitNavBar(currentRoute, { navController.navigateToBottomNav(it.route) }, selectedTabArg = currentTabArg)
            }
        }
    }

    private fun verifyFourTabsAndNavigation(screenshotName: String, isLandscape: Boolean) {
        var controller: NavHostController? = null
        composeTestRule.setContent { PixelFitQuestTheme { TestNavShell(isLandscape) { controller = it } } }
        captureScreenshot(screenshotName)

        composeTestRule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).assertCountEquals(4)
        composeTestRule.onNodeWithContentDescription("Settings").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Customization").assertDoesNotExist()
        listOf("Home", "Templates", "History", "Health").forEach { label ->
            val node = composeTestRule.onNodeWithContentDescription(label)
            node.assertExists().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
            assertFalse(SemanticsProperties.Text in node.fetchSemanticsNode().config)
        }

        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
        composeTestRule.onNodeWithText("Screen: Home").assertExists()

        composeTestRule.onNodeWithContentDescription("Templates").performClick()
        composeTestRule.onNodeWithContentDescription("Templates").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Home").assertIsNotSelected()
        composeTestRule.onNodeWithText("Screen: Workouts (templates)").assertExists()

        composeTestRule.runOnIdle { controller!!.navigate(WORKOUT_CUSTOMIZATION_SCREEN) }
        composeTestRule.onNodeWithText("Screen: WorkoutBuilder").assertExists()
        composeTestRule.onNodeWithContentDescription("Templates").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Templates").performClick()
        composeTestRule.onNodeWithText("Screen: Workouts (templates)").assertExists()
        composeTestRule.onNodeWithContentDescription("Templates").assertIsSelected()

        composeTestRule.onNodeWithContentDescription("History").performClick()
        composeTestRule.onNodeWithContentDescription("History").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Templates").assertIsNotSelected()
        composeTestRule.onNodeWithText("Screen: Workouts (history)").assertExists()

        composeTestRule.onNodeWithContentDescription("Templates").performClick()
        composeTestRule.onNodeWithContentDescription("Templates").assertIsSelected()
        composeTestRule.onNodeWithText("Screen: Workouts (templates)").assertExists()

        // In-screen tab switch updates the bar highlight without re-navigating; re-tapping the bar keeps it
        composeTestRule.onNodeWithText("In-screen History").performClick()
        composeTestRule.onNodeWithText("Screen: Workouts (history)").assertExists()
        composeTestRule.onNodeWithContentDescription("History").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Templates").assertIsNotSelected()
        composeTestRule.onNodeWithContentDescription("Templates").performClick()
        composeTestRule.onNodeWithText("Screen: Workouts (templates)").assertExists()
        composeTestRule.onNodeWithContentDescription("Templates").assertIsSelected()

        composeTestRule.onNodeWithContentDescription("Health").performClick()
        composeTestRule.onNodeWithContentDescription("Health").assertIsSelected()
        composeTestRule.onNodeWithText("Screen: Health").assertExists()

        // Health -> gear -> Settings -> tap Health tab -> Health
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Screen: Settings").assertExists()
        composeTestRule.onNodeWithContentDescription("Health").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Health").performClick()
        composeTestRule.onNodeWithText("Screen: Health").assertExists()
        composeTestRule.onNodeWithContentDescription("Health").assertIsSelected()

        // Health -> hanger -> Customization -> tap Health tab -> Health
        composeTestRule.onNodeWithContentDescription("Customize character").performClick()
        composeTestRule.onNodeWithText("Screen: Customization").assertExists()
        composeTestRule.onNodeWithContentDescription("Health").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Health").performClick()
        composeTestRule.onNodeWithText("Screen: Health").assertExists()
        composeTestRule.onNodeWithContentDescription("Health").assertIsSelected()

        // Health -> gear -> Settings -> tap Home -> tap Health -> lands on Health (not Settings)
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Screen: Settings").assertExists()
        composeTestRule.onNodeWithContentDescription("Home").performClick()
        composeTestRule.onNodeWithText("Screen: Home").assertExists()
        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
        composeTestRule.onNodeWithContentDescription("Health").performClick()
        composeTestRule.onNodeWithText("Screen: Health").assertExists()
        composeTestRule.onNodeWithContentDescription("Health").assertIsSelected()

        composeTestRule.runOnIdle { controller!!.popBackStack() }
        composeTestRule.onNodeWithContentDescription("Home").assertIsSelected()
        composeTestRule.onNodeWithText("Screen: Home").assertExists()
    }

    @Test @Config(qualifiers = QUALIFIER_PORTRAIT)
    fun appNav_portrait_fullScreenAndNavigation() = verifyFourTabsAndNavigation("AppNav_portrait", isLandscape = false)

    @Test @Config(qualifiers = QUALIFIER_LANDSCAPE)
    fun appNav_landscape_fullScreenAndNavigation() = verifyFourTabsAndNavigation("AppNav_landscape", isLandscape = true)

    @Test
    fun selectedBottomNavItem_andShouldShowBottomBar_mapRoutesCorrectly() {
        assertEquals(listOf(BottomNavItem.Home, BottomNavItem.Templates, BottomNavItem.History, BottomNavItem.Health), BottomNavItem.items)
        assertEquals(BottomNavItem.Home, selectedBottomNavItem(HOME_SCREEN))
        assertEquals(BottomNavItem.Templates, selectedBottomNavItem(WORKOUTS_HISTORY_SCREEN, null))
        assertEquals(BottomNavItem.Templates, selectedBottomNavItem("$WORKOUTS_HISTORY_SCREEN?tab={tab}", "templates"))
        assertEquals(BottomNavItem.History, selectedBottomNavItem("$WORKOUTS_HISTORY_SCREEN?tab={tab}", "history"))
        assertEquals(BottomNavItem.History, selectedBottomNavItem(HISTORY_ROUTE))
        assertEquals(BottomNavItem.Templates, selectedBottomNavItem(WORKOUT_CUSTOMIZATION_SCREEN))
        assertEquals(BottomNavItem.History, selectedBottomNavItem(PROGRESS_SCREEN))
        assertEquals(BottomNavItem.Health, selectedBottomNavItem(HEALTH_CENTER_SCREEN))
        assertEquals(BottomNavItem.Health, selectedBottomNavItem(SETTINGS_SCREEN))
        assertEquals(BottomNavItem.Health, selectedBottomNavItem(CUSTOMIZATION_SCREEN))
        assertNull(selectedBottomNavItem(SPLASH_SCREEN))
        listOf(
            HOME_SCREEN, TEMPLATES_ROUTE, HISTORY_ROUTE, HEALTH_CENTER_SCREEN,
            SETTINGS_SCREEN, CUSTOMIZATION_SCREEN, PROGRESS_SCREEN, WORKOUT_CUSTOMIZATION_SCREEN,
        ).forEach { assertTrue(shouldShowBottomBar(it)) }
        listOf(SPLASH_SCREEN, INTRO_SCREEN, "$WORKOUT_SCREEN/plan/name", null).forEach { assertFalse(shouldShowBottomBar(it)) }
    }
}
