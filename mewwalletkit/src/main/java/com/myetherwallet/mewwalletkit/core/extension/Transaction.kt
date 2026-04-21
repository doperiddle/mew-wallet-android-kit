package com.myetherwallet.mewwalletkit.core.extension

import com.myetherwallet.mewwalletkit.bip.bip44.PrivateKey
import com.myetherwallet.mewwalletkit.bip.bip44.exception.InternalErrorException
import com.myetherwallet.mewwalletkit.core.data.rlp.RlpTransaction
import com.myetherwallet.mewwalletkit.eip.eip155.Transaction
import com.myetherwallet.mewwalletkit.eip.eip155.TransactionSignature
import com.myetherwallet.mewwalletkit.eip.eip155.exception.InvalidChainIdException
import com.myetherwallet.mewwalletkit.eip.eip155.exception.InvalidPrivateKeyException
import com.myetherwallet.mewwalletkit.eip.eip155.exception.InvalidPublicKeyException
import com.myetherwallet.mewwalletkit.eip.eip155.exception.InvalidSignatureException

/**
 * Created by BArtWell on 13.06.2019.
 */

fun Transaction.encode() = RlpTransaction(this).rlpEncode()

fun Transaction.sign(key: PrivateKey, extraEntropy: Boolean = false) {
    if (this.chainId == null) {
        this.chainId = key.network.chainId
    }
    val chainId = this.chainId ?: throw InvalidChainIdException()
    val publicKeyData = key.publicKey()?.data() ?: throw InvalidPublicKeyException()
    val signature = this.eip155sign(key, extraEntropy)
    val serializedSignature = signature.first ?: throw InvalidSignatureException()
    val transactionSignature = TransactionSignature(serializedSignature, chainId)
    val recoveredPublicKey = transactionSignature.recoverPublicKey(this) ?: throw InvalidSignatureException()
    if (!publicKeyData.secureCompare(recoveredPublicKey)) {
        throw InvalidPublicKeyException()
    }
    this.signature = transactionSignature
}

/**
 * Produces a raw EIP-155 ECDSA signature for this transaction.
 *
 * When [extraEntropy] is **false** (the default) the underlying secp256k1 implementation uses
 * RFC 6979 deterministic nonce derivation, so the signature is deterministic and the loop below
 * will always succeed on the very first iteration (or not at all if the key or hash are invalid).
 *
 * When [extraEntropy] is **true** a fresh random 32-byte salt is added to the nonce derivation on
 * every iteration, which provides additional protection against fault attacks at the cost of
 * non-determinism. In that case the loop retries until the recovered public key matches, which
 * should happen almost immediately in practice.
 *
 * @return A pair of (serialised compact signature, raw recoverable signature) or (null, null).
 */
fun Transaction.eip155sign(privateKey: PrivateKey, extraEntropy: Boolean = false): Pair<ByteArray?, ByteArray?> {
    val privateKeyData = privateKey.data()
    if (!privateKeyData.secp256k1Verify()) {
        throw InvalidPrivateKeyException()
    }
    if (this.chainId == null) {
        throw InvalidChainIdException()
    }
    this.signature = null
    val publicKey = privateKey.publicKey(true)?.data() ?: throw InvalidPublicKeyException()
    val hash = this.hash(this.chainId, true) ?: throw InternalErrorException()
    // With extraEntropy=false this loop always terminates on the first iteration because the
    // signature is deterministic.  With extraEntropy=true it retries with fresh randomness until
    // the recovered public key matches (should be the first attempt in the vast majority of cases).
    for (i in 0 until 1024) {
        val signature = hash.secp256k1RecoverableSign(privateKeyData, extraEntropy) ?: continue
        val recoveredPublicKey = signature.secp256k1RecoverPublicKey(hash, true) ?: continue
        if (!recoveredPublicKey.secureCompare(publicKey)) {
            continue
        }
        val serializedSignature = signature.secp256k1SerializeSignature() ?: continue
        return Pair(serializedSignature, signature)
    }
    return Pair(null, null)
}
