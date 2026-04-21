package com.myetherwallet.mewwalletkit.core.data.rlp

import com.myetherwallet.mewwalletkit.eip.eip155.Transaction
import java.math.BigInteger

/**
 * Created by BArtWell on 19.06.2019.
 */

internal class RlpTransaction(private val value: Transaction) : Rlp {

    override fun rlpEncode(offset: Byte?): ByteArray? {
        // Pass the transaction's own chainId so that EIP-1559 / EIP-2930 transactions encode
        // correctly.  LegacyTransaction.rlpData() already falls back to this.chainId when the
        // argument is null, so passing it explicitly is safe for all subtypes.
        return value.rlpData(value.chainId).rlpEncode()
    }

    override fun toString(): String {
        return value.toString()
    }
}
