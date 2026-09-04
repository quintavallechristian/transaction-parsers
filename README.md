# Transaction Parsers

Pure Java parsers that turn supported Android payment notifications into validated transactions. The library has no Android or network dependencies.

This project is open source under the [MIT License](LICENSE), and contributions are welcome.

## Contribute

- **Developers:** add a parser by following [Adding a notification provider](docs/ADDING_A_PROVIDER.md), run `./gradlew test`, and open a pull request.
- **Non-developers:** [open a provider request](https://github.com/quintavallechristian/transaction-parsers/issues/new?title=Add%20support%20for%20%5Bprovider%5D) with the provider name, Android app version and package name, if known, plus an anonymized payment-notification screenshot or its exact text.

Never share real names, account identifiers, IBANs, card numbers, notification IDs, or other personal information. See [CONTRIBUTING.md](CONTRIBUTING.md) for the complete contribution and privacy requirements.

## Use

```kotlin
implementation("io.github.quintavallechristian:transaction-parsers:0.1.2")
```

Packages are published to GitHub Packages. Configure
`https://maven.pkg.github.com/quintavallechristian/transaction-parsers` with a GitHub username and a token with `read:packages`.

For a complete Android integration example, see [Transaction Bridge](https://github.com/quintavallechristian/android-transaction-bridge), an app that uses this library to parse payment notifications and deliver the resulting transactions to a webhook.

## Supported notification formats

Each parser receives a notification timestamp and raw text, normalizes whitespace, and returns `null` when the text does not match one of the supported formats. Android clients normally build the raw text by joining the notification title, text, and expanded text.

### Crypto.com

- Card payments: `12.50 EUR spent at Merchant Name`
- EUR deposits: `You successfully deposited EUR 100.00 into your EUR Account`

### IsyBank

- Direct debits: `È stato addebitato il pagamento di una domiciliazione di 12,50 € da parte di Merchant Name sul conto ... in data 15.08.2026`
- Instant or European transfers: `È stato inserito un bonifico istantaneo di 250,00 € dal conto ... in favore dell'IBAN IT... in data 15.08.2026 alle ore 10:30`

For transfers, the merchant is recorded as `Bonifico` followed by the IBAN. The date and time in the notification are used as the transaction time.

### ING

- Authorized card payments: `Operazione autorizzata: 12,50 euro, Merchant Name. Non sei stato tu?`
- Direct debits: `Addebito diretto di 12,50 euro richiesto da Creditor id. ABC123 Merchant Name: pagato!`

### Revolut

- Card payments: `Merchant Name Hai speso 12,50 €`
- Outgoing transfers: `Hai inviato 125 € a Recipient Name. Arriverà in pochi secondi`

An optional card-payment suffix such as `Saldo di ...` is accepted.

### BBVA

- Accepted card payments: `Il pagamento di 12,50 EUR in data Merchant Name effettuato con la tua carta 1234 è stato accettato.`

### HYPE

- Card payments: `Merchant Name, City 12,50 €`

### American Express

- Card payments: `Merchant Name 12,50 €`

### Advanzia

- Card payments: `Transazione con Carta Un pagamento di 12,50 € tramite la carta Mastercard che finisce con 1234 verso Merchant Name è stato effettuato correttamente.`

### Google Wallet

- Card payments: `Merchant Name 12,50 € con ... •••• 1234`

The final four card digits must exist in the card map supplied by the caller. Notifications from unknown cards are ignored.

The caller-provided card name becomes part of the transaction `source`, for example `google-wallet-personal-ing-notification` or `google-wallet-crypto-notification`. The parser never needs the full card number.

Google Wallet notifications are not reconciled with notifications from the underlying bank or card provider. If both sources report the same payment, they produce different `source` and transaction IDs and may therefore be treated as two separate transactions.

## Build

```sh
./gradlew test
```

Requires Java 17. See [CONTRIBUTING.md](CONTRIBUTING.md) and [Adding a notification provider](docs/ADDING_A_PROVIDER.md).
