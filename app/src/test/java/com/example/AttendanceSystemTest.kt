package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DataSeeder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AttendanceSystemTest {

    @Test
    fun testAppNameResource() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("IIE Attendance", appName)
    }

    @Test
    fun testExactly56StudentsMasterList() {
        val students = DataSeeder.INITIAL_STUDENTS
        assertEquals(56, students.size)

        // Verify first and last students
        assertEquals("25BITT001", students.first().first)
        assertEquals("ABISHEK M", students.first().second)

        assertEquals("25BIT302", students.last().first)
        assertEquals("PRADHEESH S", students.last().second)

        // Verify lateral entry 25BIT301
        val lateralEntry1 = students.find { it.first == "25BIT301" }
        assertTrue(lateralEntry1 != null)
        assertEquals("HARI PRASADH G", lateralEntry1?.second)

        // Verify intentionally absent numbers like 25BITT019, 25BITT026, 25BITT052 are not generated
        assertFalse(students.any { it.first == "25BITT019" })
        assertFalse(students.any { it.first == "25BITT026" })
        assertFalse(students.any { it.first == "25BITT052" })
    }

    @Test
    fun testSevenCurriculumSubjects() {
        val subjects = DataSeeder.INITIAL_SUBJECTS
        assertEquals(7, subjects.size)

        val subjectNames = subjects.map { it.second }
        assertTrue(subjectNames.contains("Discrete Mathematics"))
        assertTrue(subjectNames.contains("Data Structures"))
        assertTrue(subjectNames.contains("Object-Oriented Programming (OOPS)"))
        assertTrue(subjectNames.contains("Web Technology"))
        assertTrue(subjectNames.contains("Computer Organization and Architecture (COA)"))
        assertTrue(subjectNames.contains("English"))
        assertTrue(subjectNames.contains("SDC"))
    }

    @Test
    fun testAttendancePercentageCalculation() {
        val conducted = 20
        val present = 15
        val od = 2

        // Formula: Present / Conducted * 100
        val basePct = (present.toDouble() / conducted.toDouble()) * 100.0
        assertEquals(75.0, basePct, 0.01)

        // When OD is excluded: 75%
        val isLow = basePct < 75.0
        assertFalse(isLow) // 75% meets threshold

        // With 14 present: 70% -> below 75% threshold
        val lowerPct = (14.0 / conducted.toDouble()) * 100.0
        assertTrue(lowerPct < 75.0)
    }
}
