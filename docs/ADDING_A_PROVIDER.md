# Adding a notification provider

## Before writing code

Collect the exact Android package name, notification language and observed app version, one notification that must match, and one nearby notification that must not match. Anonymize names, merchants, IBANs, account identifiers, card digits, notification IDs, and other personal data while preserving the format.

## Add the parser

Create `src/main/java/io/github/transactionbridge/<Provider>NotificationParser.java` and implement `NotificationParser`.

- Reuse `ParserSupport` and `BigDecimal`.
- Return `null` for unrelated, incomplete, or invalid text.
- Require a positive amount and non-empty merchant.
- Use the supplied notification timestamp unless the text contains a more authoritative transaction time.
- Use a stable lowercase source such as `<provider>-notification`.
- Do not add Android, storage, or network dependencies.

## Register the provider

Add the exact Android package, lowercase setting key, label, and parser to `ParserRegistry.defaultRegistry()`. Do not guess a package name that was not supplied or verified.

## Test and document

Add focused positive, negative, and registry assertions to `src/test/java/io/github/transactionbridge/NotificationParserTest.java`. Cover only observed variants; do not invent permissive regex alternatives.

Update the supported-format list in `README.md`, then run:

```sh
./gradlew test
git diff --check
```

Inspect the final diff for personal information before opening a pull request.
