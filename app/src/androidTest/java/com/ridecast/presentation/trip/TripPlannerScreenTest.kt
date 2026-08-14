package com.ridecast.presentation.trip

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.ridecast.presentation.theme.RideCastTheme
import org.junit.Rule
import org.junit.Test

class TripPlannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screenShowsPlanYourRideTitle() {
        composeTestRule.setContent {
            RideCastTheme {
                androidx.compose.material3.Text("Plan Your Ride")
            }
        }
        composeTestRule.onNodeWithText("Plan Your Ride").assertIsDisplayed()
    }
}
