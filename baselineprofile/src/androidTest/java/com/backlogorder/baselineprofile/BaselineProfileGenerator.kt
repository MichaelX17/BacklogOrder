package com.backlogorder.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = "com.backlogorder",
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.text("Dashboard")), 5_000)
        }
    }
}
