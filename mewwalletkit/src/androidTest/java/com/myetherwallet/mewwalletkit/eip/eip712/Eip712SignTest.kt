package com.myetherwallet.mewwalletkit.eip.eip712

import com.myetherwallet.mewwalletkit.bip.bip44.Network
import com.myetherwallet.mewwalletkit.bip.bip44.PrivateKey
import com.myetherwallet.mewwalletkit.core.extension.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for [Eip712Utils.sign] — verifies the new dedicated signing entry-point
 * produces the same signatures as the manual hash-then-sign approach.
 */
class Eip712SignTest {

    // EIP-712 "Mail" typed-message from the official EIP-712 test vectors.
    private val typedMessage = """
        {
   "types":{
      "EIP712Domain":[
         {"name":"name","type":"string"},
         {"name":"version","type":"string"},
         {"name":"chainId","type":"uint256"},
         {"name":"verifyingContract","type":"address"}
      ],
      "Person":[
         {"name":"name","type":"string"},
         {"name":"wallet","type":"address"}
      ],
      "Mail":[
         {"name":"from","type":"Person"},
         {"name":"to","type":"Person"},
         {"name":"contents","type":"string"}
      ]
   },
   "primaryType":"Mail",
   "domain":{
      "name":"Ether Mail",
      "version":"1",
      "chainId":1,
      "verifyingContract":"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"
   },
   "message":{
      "from":{"name":"Cow","wallet":"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826"},
      "to":{"name":"Bob","wallet":"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB"},
      "contents":"Hello, Bob!"
   }
}
    """

    // Expected normalized (r || s || v+27) hex signature produced with the "cow" key.
    private val expectedSignature =
        "0x4355c47d63924e8a72e509b65029052eb6c299d53a04e167c5775fd466751c9d07299936d304c153f6443dfa05f40ff007d72911b6f72307f996231605b915621c"

    private val cowPrivateKey: ByteArray get() = "cow".toByteArray().keccak256()

    /**
     * Normalises a raw 65-byte recoverable signature (r || s || recovery-bit) to the
     * Ethereum convention used in the existing tests (r || s || v+27).
     */
    private fun normalise(raw: ByteArray): String {
        val serialized = raw.secp256k1SerializeSignature()!!
        val rs = serialized.copyOfRange(0, 64)
        val v = (serialized[64] + 27).toByte()
        return (rs + v).toHexString().addHexPrefix()
    }

    @Test
    fun `sign produces the same result as manual hash-then-sign`() {
        val hash = Eip712Utils.getHash(typedMessage)
        val manualSignature = normalise(hash.secp256k1RecoverableSign(cowPrivateKey)!!)
        assertEquals(expectedSignature, manualSignature)
    }

    @Test
    fun `Eip712Utils sign with PrivateKey returns expected signature`() {
        val privateKey = PrivateKey.createWithPrivateKey(cowPrivateKey, Network.ETHEREUM)
        val raw = Eip712Utils.sign(typedMessage, privateKey)
        assertNotNull("sign() must not return null for a valid message and key", raw)
        assertEquals(expectedSignature, normalise(raw!!))
    }

    @Test
    fun `getHash is deterministic across repeated calls`() {
        val hash1 = Eip712Utils.getHash(typedMessage)
        val hash2 = Eip712Utils.getHash(typedMessage)
        assertEquals(hash1.toHexString(), hash2.toHexString())
    }

    @Test
    fun `getHash from InputStream matches getHash from String`() {
        val bytes = typedMessage.trimIndent().toByteArray(Charsets.UTF_8)
        val hashFromString = Eip712Utils.getHash(typedMessage)
        val hashFromStream = bytes.inputStream().use { Eip712Utils.getHash(it) }
        assertEquals(hashFromString.toHexString(), hashFromStream.toHexString())
    }
}
