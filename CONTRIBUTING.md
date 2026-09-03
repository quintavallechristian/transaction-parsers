# Contributing

This repository is limited to payment-notification parsing and provider metadata. Android UI, notification collection, delivery, webhook, and persistence changes belong in their consuming applications.

New providers require a pure Java parser, an exact Android package mapping, one anonymized positive fixture, one nearby negative fixture, and a documented notification format. Never commit credentials, real names, card numbers, account identifiers, IBANs, notification IDs, or identifiable raw notification text.

Run `./gradlew test` before opening a pull request. Follow the complete [provider guide](docs/ADDING_A_PROVIDER.md).
