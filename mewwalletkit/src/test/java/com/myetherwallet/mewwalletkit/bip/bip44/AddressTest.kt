package com.myetherwallet.mewwalletkit.bip.bip44

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressTest {

    @Test
    fun `isChecksumValid returns true for correctly checksummed EIP-55 address`() {
        // Reference: EIP-55 test vectors (with 0x prefix)
        val validAddresses = listOf(
            "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed",
            "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359",
            "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB",
            "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb"
        )
        for (addr in validAddresses) {
            assertTrue("Expected valid checksum for $addr", Address.createRaw(addr).isChecksumValid())
        }
    }

    @Test
    fun `isChecksumValid returns false for all-lowercase address`() {
        val lower = "0x5aaeb6053f3e94c9b9a09f33669435e7ef1beaed"
        assertFalse(Address.createRaw(lower).isChecksumValid())
    }

    @Test
    fun `isChecksumValid returns false for all-uppercase address`() {
        val upper = "0x5AAEB6053F3E94C9B9A09F33669435E7EF1BEAED"
        assertFalse(Address.createRaw(upper).isChecksumValid())
    }

    @Test
    fun `isChecksumValid returns false for wrong mixed-case address`() {
        // Flip one character's case to break the checksum
        val wrong = "0x5aaeb6053F3E94C9b9A09f33669435E7Ef1BeAed"
        assertFalse(Address.createRaw(wrong).isChecksumValid())
    }

    @Test
    fun `isChecksumValid returns false for empty address`() {
        assertFalse(Address.createDefault().isChecksumValid())
    }

    @Test
    fun `createEthereum normalises to valid EIP-55 checksum`() {
        val lower = "0x5aaeb6053f3e94c9b9a09f33669435e7ef1beaed"
        val address = Address.createEthereum(lower)
        assertTrue("createEthereum should produce a checksummed address", address?.isChecksumValid() == true)
    }
}
