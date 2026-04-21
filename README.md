# mew-wallet-android-kit

A Kotlin Android library that implements core Ethereum (and Bitcoin/Litecoin) cryptographic
primitives and wallet standards, powering [MyEtherWallet](https://www.myetherwallet.com/).

---

## Supported standards

| Standard | Description |
|---|---|
| BIP-39 | Mnemonic code for generating deterministic keys (12/15/18/21/24 words) |
| BIP-44 | Multi-account hierarchy for deterministic wallets (HD wallets) |
| EIP-55 | Mixed-case checksum address encoding |
| EIP-67 | Ethereum URI scheme (simple) |
| EIP-155 | Replay attack protection for transactions |
| EIP-681 | Ethereum URI with function-call and ABI parameter parsing |
| EIP-712 | Typed structured data hashing and signing (`eth_signTypedData`) |
| EIP-2930 | Optional access lists (type-1 transactions) |
| EIP-1559 | Fee market transactions (type-2 transactions) |

---

## Installation

Add JitPack to your root `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Then add the dependency to your module `build.gradle`:

```groovy
dependencies {
    implementation 'com.github.doperiddle:mew-wallet-android-kit:<version>'
}
```

---

## Usage

### Generate a new wallet

```kotlin
// Generate a random 24-word mnemonic + HD wallet (Ethereum mainnet by default)
val (bip39, wallet) = Wallet.generate()
println(bip39.mnemonic?.joinToString(" "))  // e.g. "abandon ability able …"
println(wallet.privateKey.address()?.address) // e.g. "0x5aAeb6…"
```

### Restore a wallet from a mnemonic

```kotlin
val words = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about".split(" ")
val (bip39, wallet) = Wallet.restore(words)
println(wallet.privateKey.address()?.address)
```

### Derive child accounts (BIP-44 path)

```kotlin
// Derive the first 5 accounts on the Ethereum path m/44'/60'/0'/0
for (index in 0 until 5) {
    val child = wallet.derive(Network.ETHEREUM.path, index)
    println("[$index] ${child.privateKey.address()?.address}")
}
```

### Sign a legacy (pre-EIP-1559) transaction

```kotlin
val privateKey = PrivateKey.createWithPrivateKey(
    "your_32_byte_private_key".hexToByteArray(),
    Network.ETHEREUM
)

val tx = LegacyTransaction(
    nonce   = "0x03",
    gasPrice = "0x3b9aca00",
    gasLimit = "0x5208",
    to      = Address.createEthereum("0xb414031Aa4838A69e27Cb2AE31E709Bcd674F0Cb"),
    value   = "0x64",
    data    = ByteArray(0)
)
tx.chainId = BigInteger.ONE  // mainnet

tx.sign(privateKey)
println(tx.serialize()?.toHexString())
```

### Sign an EIP-1559 (type-2) transaction

```kotlin
val tx = Eip1559Transaction(
    nonce               = "0x01",
    maxPriorityFeePerGas = "0x3b9aca00",
    maxFeePerGas        = "0x77359400",
    gasLimit            = "0x5208",
    to                  = Address.createEthereum("0xRecipient…"),
    value               = "0xde0b6b3a7640000",  // 1 ETH in wei
    data                = ByteArray(0),
    accessList          = null,
    chainId             = BigInteger.ONE.toByteArray()
)
tx.sign(privateKey)
println(tx.serialize()?.toHexString())
```

### Parse an EIP-681 payment URI

```kotlin
val uri = "ethereum:0xb60e8dd61c5d32be8058bb8eb970870f07233155?value=1e18"
val code = Eip681Code.create(uri)
println(code?.targetAddress?.address)  // 0xb60e8dd61c5d32be8058bb8eb970870f07233155
println(code?.value)                   // 1000000000000000000
```

### Hash and sign an EIP-712 typed-data message

```kotlin
val json = """{ "types": { … }, "domain": { … }, "message": { … } }"""

// Just the hash (for display or hardware-wallet signing)
val hash: ByteArray = Eip712Utils.getHash(json)

// Full signing with a software key
val signature: ByteArray? = Eip712Utils.sign(json, privateKey)
```

### Validate an EIP-55 checksummed address

```kotlin
val address = Address.createEthereum("0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed")
println(address?.isChecksumValid())   // true

val raw = Address.createRaw("0x5aaeb6053f3e94c9b9a09f33669435e7ef1beaed")
println(raw.isChecksumValid())        // false (all-lowercase)
```

---

## Architecture

```
mewwalletkit/
├── bip/
│   ├── bip39/   Mnemonic generation & entropy/seed derivation
│   └── bip44/   HD key derivation, address generation, multi-network support
├── core/
│   ├── data/    Bit/BitArray primitives; full RLP encoder
│   ├── extension/ Kotlin extension functions (ByteArray, BigInteger, String, …)
│   └── util/    HMAC-SHA512, PKCS5/PBKDF2, BitReader
└── eip/
    ├── eip155/  Abstract Transaction base + LegacyTransaction + EIP-155 signing
    ├── eip1559/ Type-2 fee-market transactions
    ├── eip2930/ Type-1 access-list transactions
    ├── eip67/   Simple Ethereum URI parsing
    ├── eip681/  Extended Ethereum URI with ABI function-call parsing
    └── eip712/  Typed structured data hashing and signing
```

The **secp256k1** module wraps prebuilt native `.so` libraries
(`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) for elliptic-curve operations.

---

## License

[MIT](LICENSE)
