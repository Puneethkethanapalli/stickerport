package app.stickerport.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.stickerport.ui.components.StickerportApp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * T0.3's acceptance test: **every route composes, and its arguments survive the trip.**
 *
 * A navigation skeleton can fail in ways a build cannot catch — a route that resolves to nothing, a
 * `toRoute<T>()` that returns defaults, an argument that silently arrives empty. So each test
 * asserts on the *rendered argument text*, not merely "no exception was thrown".
 *
 * `StickerportApp` owns a single `Scaffold`, so `Scaffold` does not need a `Scaffold` test tag here
 * and the screen content is reachable directly.
 */
@RunWith(AndroidJUnit4::class)
class StickerportNavHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: NavHostController

    private fun launchApp() {
        composeRule.setContent {
            StickerportApp(navController = rememberNavController().also { navController = it })
        }
        composeRule.waitForIdle()
    }

    private fun navigateAndAssert(vararg expectedTexts: String) {
        composeRule.waitForIdle()
        expectedTexts.forEach { text ->
            composeRule.onNodeWithText(text).assertIsDisplayed()
        }
    }

    @Test
    fun startsOnHome() {
        launchApp()

        // §12.1: Home is the start destination and its top bar shows the app name.
        composeRule.onNodeWithText("Stickerport").assertIsDisplayed()
        // §12.1: no bot token yet, so the setup banner and the disabled paste field are both there.
        composeRule.onNodeWithText("Set up your bot token to start importing.").assertIsDisplayed()
    }

    @Test
    fun homeToLibrary() {
        launchApp()

        // Matched as a *substring* on purpose: the empty-state copy is a whole sentence, and an
        // exact match on its first four words would be a test that breaks when the copy is edited.
        composeRule.onNodeWithText("Nothing here yet", substring = true).assertDoesNotExist()

        composeRule.onNodeWithText("Library").performClick()

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Nothing here yet", substring = true).assertIsDisplayed()
    }

    @Test
    fun homeToSettings() {
        launchApp()

        // The top bar's actions are icon buttons, so they carry a contentDescription, not text.
        // onNodeWithText finds nothing here — a good reminder that "the title says Settings" and
        // "the Settings icon is tappable" are two different things.
        composeRule.onNodeWithContentDescription("Settings").performClick()

        composeRule.waitForIdle()
        // §12.6 rows are all present.
        composeRule.onNodeWithText("Bot token").assertIsDisplayed()
        composeRule.onNodeWithText("Quality preset").assertIsDisplayed()
    }

    @Test
    fun homeToOnboarding() {
        launchApp()

        composeRule.onNodeWithText("Onboarding").performClick()

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Welcome").assertIsDisplayed()
        composeRule.onNodeWithText("Get started").assertIsDisplayed()
    }

    @Test
    fun previewCarriesItsSetName() {
        launchApp()

        composeRule.runOnUiThread { navController.navigate(Preview(setName = "PackByPack_Animals")) }
        navigateAndAssert("Import Preview", "setName = PackByPack_Animals")
    }

    @Test
    fun progressCarriesItsImportId() {
        launchApp()

        composeRule.runOnUiThread { navController.navigate(Progress(importId = 42L)) }
        navigateAndAssert("Progress", "importId = 42")
    }

    @Test
    fun resultCarriesItsImportId() {
        launchApp()

        composeRule.runOnUiThread { navController.navigate(Result(importId = 7L)) }
        navigateAndAssert("Result", "importId = 7")
    }

    @Test
    fun packDetailCarriesItsPackId() {
        launchApp()

        val packId = "3f1c9a2e-6b54-4d18-9f77-0c2b5a8e41d3"
        composeRule.runOnUiThread { navController.navigate(PackDetail(packId = packId)) }
        navigateAndAssert("Pack Detail", "packId = $packId")
    }

    /**
     * The round trip that a per-route test cannot cover: navigate Home → Result → back, and confirm
     * the back stack actually unwound rather than leaving Result on top.
     */
    @Test
    fun backReturnsToHome() {
        launchApp()

        composeRule.runOnUiThread { navController.navigate(Result(importId = 3L)) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Result").assertIsDisplayed()

        composeRule.runOnUiThread { assertTrue(navController.popBackStack()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Stickerport").assertIsDisplayed()
    }
}
