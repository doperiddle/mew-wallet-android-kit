package com.myetherwallet.mewwalletkit.bip.bip44

import android.os.Parcelable
import com.myetherwallet.mewwalletkit.core.extension.eip55
import com.myetherwallet.mewwalletkit.core.extension.isHex
import com.myetherwallet.mewwalletkit.core.extension.toHexString
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Created by BArtWell on 23.05.2019.
 */

@Parcelize
class Address constructor(val address: String) : Parcelable {

    companion object {

        const val DEFAULT_API_CONTRACT = "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"

        fun create(data: ByteArray, prefix: String? = null) = create(data.toHexString(), prefix)

        fun create(address: String, prefix: String? = null) = createRaw(
            if (prefix != null && !address.startsWith(prefix)) {
                prefix + address
            } else {
                address
            }
        )

        fun createRaw(rawAddress: String) = Address(rawAddress)

        fun createDefault() = createRaw("")

        fun createEthereum(ethereumAddress: String) = if (ethereumAddress.length == 42 && ethereumAddress.isHex()) {
            ethereumAddress.eip55()?.let {
                Address(it)
            }
        } else {
            null
        }
    }

    fun isDefault() = address.isEmpty()

    /**
     * Returns true if [address] is a non-empty Ethereum address whose mixed-case encoding exactly
     * matches the EIP-55 checksum encoding.  Addresses that were created via [createEthereum] are
     * always normalised to EIP-55, so this is most useful when validating externally-supplied strings.
     */
    fun isChecksumValid(): Boolean {
        if (address.isEmpty()) return false
        val eip55 = address.eip55() ?: return false
        return address == eip55
    }

    override fun toString() = address

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Address

        if (address.lowercase(Locale.US) != other.address.lowercase(Locale.US)) return false

        return true
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }


}
