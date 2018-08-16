package com.example.alexandrevaz.hiwebviewsimulator


import android.graphics.Point
import android.os.RemoteException
import android.os.SystemClock
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.web.assertion.WebViewAssertions.webMatches
import android.support.test.espresso.web.sugar.Web.onWebView
import android.support.test.espresso.web.webdriver.DriverAtoms.*
import android.support.test.espresso.web.webdriver.Locator
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.UiDevice
import com.microsoft.appcenter.espresso.Factory
import com.microsoft.appcenter.espresso.ReportHelper
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.isEmptyString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    var mActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule<MainActivity>(MainActivity::class.java)

    @get:Rule
    val reportHelper: ReportHelper = Factory.getReportHelper()

    @Before
    fun setUp() {
        onWebView().forceJavascriptEnabled()
        wakeDeviceUp()
    }

    @Test
    fun initializeBot_fillSurvey_Test() {
        SystemClock.sleep(14000)
        wakeDeviceUp()

        onWebView()
                .reset()
                .withElement(findElement(Locator.CSS_SELECTOR, "input[name='nome_usuario']"))
                .check(webMatches(getText(), isEmptyString()))
                .perform(webKeys("Teste"))
                .withElement(findElement(Locator.CSS_SELECTOR, "input[name='email']"))
                .perform(webKeys("teste@teste.com"))
                .withElement(findElement(Locator.CSS_SELECTOR, "button[type='submit']"))
                .perform(webClick())

        SystemClock.sleep(10000)
        wakeDeviceUp()

        onWebView()
                .reset()
                .withElement(findElement(Locator.CSS_SELECTOR, ".message-intro-animate .bot-answer-slot"))
                .check(webMatches(getText(), containsString("Bem-vindo")))
    }

    @After
    fun tearDown() {
        reportHelper.label("Stopping app")
    }

    fun wakeDeviceUp() {
        val uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val coordinates: Array<Point> = arrayOf(
                Point(248, 1520),
                Point(248, 929),
                Point(796, 1520),
                Point(796, 929)
        )

        try {
            if (!uiDevice.isScreenOn) {
                uiDevice.wakeUp()
                uiDevice.swipe(coordinates, 10)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
}
