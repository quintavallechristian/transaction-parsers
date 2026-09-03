# Transaction Parsers

Pure Java parsers that turn supported Android payment notifications into validated transactions. The library has no Android or network dependencies.

## Use

```kotlin
implementation("io.github.quintavallechristian:transaction-parsers:0.1.1")
```

Packages are published to GitHub Packages. Configure
`https://maven.pkg.github.com/quintavallechristian/transaction-parsers` with a GitHub username and a token with `read:packages`.

## Supported formats

- **Crypto.com:** card payments and processed EUR deposits.
- **IsyBank:** direct debits and outgoing instant or European transfers.
- **ING:** authorized card payments and paid direct debits.
- **Revolut:** card payments and completed outgoing transfers.
- **BBVA:** accepted card payments.
- **HYPE:** card payments.
- **American Express:** card payments.
- **Google Wallet:** card payments whose final four card digits exist in the supplied card map.

Parsers normalize whitespace and return `null` for unrelated or invalid notifications. Google Wallet uses the caller-provided card name in the transaction `source`; it does not reconcile duplicate notifications from the underlying provider.

## Build

```sh
./gradlew test
```

Requires Java 17. See [CONTRIBUTING.md](CONTRIBUTING.md) and [Adding a notification provider](docs/ADDING_A_PROVIDER.md).
