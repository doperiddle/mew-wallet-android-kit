package com.myetherwallet.mewwalletkit.bip.bip44

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigInteger

class NetworkTest {

    @Test
    fun `findByChainId returns Ethereum for chainId 1`() {
        assertEquals(Network.ETHEREUM, Network.findByChainId(BigInteger.ONE))
    }

    @Test
    fun `findByChainId returns Bitcoin for chainId 0`() {
        assertEquals(Network.BITCOIN, Network.findByChainId(BigInteger.ZERO))
    }

    @Test
    fun `findByChainId returns Ropsten for chainId 3`() {
        assertEquals(Network.ROPSTEN, Network.findByChainId(BigInteger.valueOf(3)))
    }

    @Test
    fun `findByChainId returns Goerli for chainId 5`() {
        assertEquals(Network.GOERLI, Network.findByChainId(BigInteger.valueOf(5)))
    }

    @Test
    fun `findByChainId returns null for unknown chainId`() {
        assertNull(Network.findByChainId(BigInteger.valueOf(99999)))
    }

    @Test
    fun `findByChainId returns null for null input`() {
        assertNull(Network.findByChainId(null))
    }

    @Test
    fun `deprecated findByChaidId delegates to findByChainId`() {
        @Suppress("DEPRECATION")
        assertEquals(Network.ETHEREUM, Network.findByChaidId(BigInteger.ONE))
    }
}
