package com.myetherwallet.mewwalletkit.bip.bip44

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigInteger

/**
 * Instrumented integration tests for [Network.findByChainId].
 *
 * These run on-device to confirm the fix for the reflection bug is effective
 * end-to-end (i.e. that the companion object initialises correctly on Android).
 */
class NetworkInstrumentedTest {

    @Test
    fun findByChainId_ethereum() {
        assertEquals(Network.ETHEREUM, Network.findByChainId(BigInteger.ONE))
    }

    @Test
    fun findByChainId_bitcoin() {
        assertEquals(Network.BITCOIN, Network.findByChainId(BigInteger.ZERO))
    }

    @Test
    fun findByChainId_ropsten() {
        assertEquals(Network.ROPSTEN, Network.findByChainId(BigInteger.valueOf(3)))
    }

    @Test
    fun findByChainId_goerli() {
        assertEquals(Network.GOERLI, Network.findByChainId(BigInteger.valueOf(5)))
    }

    @Test
    fun findByChainId_unknownChainId_returnsNull() {
        assertNull(Network.findByChainId(BigInteger.valueOf(Long.MAX_VALUE)))
    }

    @Test
    fun findByChainId_null_returnsNull() {
        assertNull(Network.findByChainId(null))
    }

    @Test
    fun findByChainId_ethereumClassic() {
        assertEquals(Network.ETHEREUM_CLASSIC, Network.findByChainId(BigInteger.valueOf(61)))
    }
}
