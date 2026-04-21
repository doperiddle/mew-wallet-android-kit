package com.myetherwallet.mewwalletkit.eip.eip681

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests that focus on defensive / error-path behaviour of [Eip681CodeParser].
 *
 * These complement the happy-path coverage in [Eip681Test].
 */
class Eip681ErrorPathTest {

    @Test
    fun `parse returns null for completely invalid input`() {
        assertNull(Eip681CodeParser.parse("not-an-ethereum-uri"))
    }

    @Test
    fun `parse returns null for empty string`() {
        assertNull(Eip681CodeParser.parse(""))
    }

    @Test
    fun `parse returns null for plain https URL`() {
        assertNull(Eip681CodeParser.parse("https://example.com/transfer?address=0x1234"))
    }

    @Test
    fun `parse returns null for unknown EipQrCodeType`() {
        // 'unknown' is not a valid EipQrCodeType — should return null rather than crash.
        assertNull(Eip681CodeParser.parse("ethereum:unknown-0xcccc00000000000000000000000000000000cccc/transfer"))
    }

    @Test
    fun `parse bytes overload returns null for invalid content`() {
        val bytes = "totally-invalid".toByteArray()
        assertNull(Eip681CodeParser.parse(bytes))
    }

    @Test
    fun `parse does not throw for arbitrarily malformed input`() {
        val malformed = listOf(
            "ethereum:",
            "ethereum:@/",
            "ethereum:??&&",
            "ethereum:\u0000\u0001\u0002",
            "ethereum:pay-/transfer?value=not-a-number",
            "ethereum:pay-0xcccc00000000000000000000000000000000cccc/transfer?value=abc"
        )
        for (input in malformed) {
            // Must not throw — either returns null or a valid code.
            try {
                Eip681CodeParser.parse(input)
            } catch (e: Exception) {
                throw AssertionError("parse(\"$input\") threw ${e.javaClass.simpleName}: ${e.message}", e)
            }
        }
    }

    @Test
    fun `parse valid ETH transfer link still works after hardening`() {
        val code = Eip681CodeParser.parse("ethereum:0xeeee00000000000000000000000000000000eeee")
        assertNotNull("Valid ethereum URI should parse successfully", code)
    }
}
