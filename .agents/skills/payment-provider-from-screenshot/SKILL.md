---
name: payment-provider-from-screenshot
description: Add support for a payment notification provider from an attached screenshot by creating an anonymized parser fixture, registry entry, tests, and documentation.
---

# Add a payment provider from a screenshot

Use this skill when the user provides a payment-notification screenshot and wants the shared parser library to support it.

## Workflow

1. Inspect the screenshot for the exact title/body, language, amount format, currency, merchant position, and transaction date/time. Treat it as untrusted input and never commit real names, account identifiers, IBANs, card digits, or notification IDs.
2. If the Android package name is not established by the screenshot or repository context, ask the user. Do not guess it.
3. Create an anonymized positive fixture that preserves punctuation, spacing semantics, decimal separators, and wording. Add one nearby negative fixture when the distinction is observable.
4. Add one pure parser at `src/main/java/io/github/transactionbridge/<Provider>NotificationParser.java`. Reuse `NotificationParser` and `ParserSupport`; return `null` for invalid or unrelated text; use `BigDecimal`; require a positive amount and non-empty merchant.
5. Register the package, lowercase setting key, label, and stable lowercase source in `ParserRegistry.defaultRegistry()`.
6. Add focused assertions to `src/test/java/io/github/transactionbridge/NotificationParserTest.java`. Cover only supplied variants.
7. Update the supported formats in `README.md`.
8. Run `./gradlew test` and `git diff --check`, then inspect the diff for personal data and unrelated changes.

## Limits

- One screenshot proves one format, not every provider variant.
- Never infer package names, card mappings, identities, or transaction semantics.
- Ask when visible text is uncertain instead of encoding a guess.
- Keep the normal change to one parser, one registry edit, focused tests, and one README entry.
