package com.example.campus_attendance.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailValidatorTest {

    @Test
    fun isValidCollegeEmail_validMinRegNo() {
        assertTrue(isValidCollegeEmail("231801370001@cutmap.ac.in"))
    }

    @Test
    fun isValidCollegeEmail_validMaxRegNo() {
        assertTrue(isValidCollegeEmail("231801370095@cutmap.ac.in"))
    }

    @Test
    fun isValidCollegeEmail_invalidBelowMinRegNo() {
        assertFalse(isValidCollegeEmail("231801370000@cutmap.ac.in"))
    }

    @Test
    fun isValidCollegeEmail_invalidAboveMaxRegNo() {
        assertFalse(isValidCollegeEmail("231801370096@cutmap.ac.in"))
    }

    @Test
    fun isValidCollegeEmail_invalidDomain() {
        assertFalse(isValidCollegeEmail("231801370001@gmail.com"))
    }

    @Test
    fun isValidCollegeEmail_invalidElevenDigitNumber() {
        assertFalse(isValidCollegeEmail("23180137000@cutmap.ac.in"))
    }

    @Test
    fun isValidCollegeEmail_uppercaseDomainInput() {
        assertTrue(isValidCollegeEmail("231801370001@CUTMAP.AC.IN"))
    }

    @Test
    fun isValidCollegeEmail_inputWithSpaces() {
        assertTrue(isValidCollegeEmail(" 231801370001@cutmap.ac.in "))
    }

    @Test
    fun extractRegNo_validEmail() {
        assertEquals("231801370001", extractRegNo("231801370001@cutmap.ac.in"))
        assertEquals("231801370095", extractRegNo(" 231801370095@CUTMAP.AC.IN "))
    }

    @Test
    fun extractRegNo_invalidEmail() {
        assertNull(extractRegNo("231801370000@cutmap.ac.in"))
        assertNull(extractRegNo("invalid@gmail.com"))
    }
}
