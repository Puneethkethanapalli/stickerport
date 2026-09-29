package app.stickerport.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.stickerport.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
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
 * ## Why `createAndroidComposeRule<MainActivity>` and not `createComposeRule()`
 * T0.4 gave Home a `hiltViewModel()`. A ViewModel factory resolves the Hilt component from the
 * hosting Activity, and the bare `ComponentActivity` that `createComposeRule()` spins up is not
 * `@AndroidEntryPoint`, so it implements neither `GeneratedComponent` nor
 * `GeneratedComponentManager` and every test fails with:
 *
 *     Given component holder class androidx.activity.ComponentActivity does not implement
 *     interface dagger.hilt.internal.GeneratedComponent
 *
 * Hosting the **real** `MainActivity` fixes that and is the more honest test anyway: it exercises
 * the same Activity a user gets, `@AndroidEntryPoint` and all.
 *
 * The `HiltAndroidRule` must run **before** the Activity launches (`@get:Rule(order = 0)`), or the
 * component does not exist yet and the failure reads "The component was not created. Check that you
 * have added the HiltAndroidRule."
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StickerportNavHostTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    /**
     * The real Activity's controller.
     *
     * No `setContent` here: `MainActivity.onCreate` has already set the content, and calling
     * `setContent` again throws "MainActivity has already set content". Creating the controller
     * inline in the test and passing it to `StickerportApp` is not an option for the same reason —
     * there is no second place to install content.
     */
    /**
     * The real Activity's controller, or a clear failure.
     *
     * It is nullable because composition has not necessarily run when the rule hands the test its
     * Activity, so `waitUntil` is used to wait for it rather than reading it and force-unwrapping.
     */
    private val navController: NavHostController
        get() = requireNotNull(composeRule.activity.navController) {
            "MainActivity has not composed yet; wait for navController before navigating"
        }

    @Before
    fun inject() = hiltRule.inject()

    private fun launchApp() {
        // The Activity is already composed and showing Home; just wait for the controller and let
        // the first frame settle.
        composeRule.waitUntil { composeRule.activity.navController != null }
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
