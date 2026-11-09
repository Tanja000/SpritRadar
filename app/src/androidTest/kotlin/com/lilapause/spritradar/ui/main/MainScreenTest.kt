package com.lilapause.spritradar.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.lilapause.spritradar.ui.main.MainScreen]. */
class MainScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent { MainScreenPreview() }
    }

    @Test
    fun appTitle_andDetourOptions_exist() {
        composeTestRule.onNodeWithText("SpritRadar").assertExists()
        composeTestRule.onNodeWithText("10 km").assertExists()
        composeTestRule.onNodeWithText("30 km").assertExists()
    }
}
