# Adding a notification provider

This guide explains how to add a provider for a payment notification format supported by an Android app. Providers should be added for every relevant notification type, including notifications that may also be reported by another source.

## AI way

You can ask the `payment-provider-from-screenshot` skill included in this repository to add the provider from an attached payment-notification screenshot. The skill extracts the visible format, creates anonymized fixtures, adds the parser and registry entry, writes focused tests, and updates the README.

The screenshot must be sufficient to identify the notification structure. Provide the Android package name separately if it is not visible or already known; the skill must not guess it. Review the generated diff and run the verification commands below before committing. Real names, account identifiers, IBANs, card digits, and notification IDs must never be committed.

## Manual way

Follow the steps below when implementing the provider manually.

### Before writing code

Collect:

- the provider's Android package name, such as `com.example.bank`;
- the notification language and app version observed;
- one payment notification that should match;
- one similar notification that must not match.

Anonymize every fixture before committing it. Replace names, merchants, IBANs, account identifiers, card digits, and other personal data while preserving punctuation and structure. Never commit credentials or a real notification dump.

### 1. Add the parser

Create:

```text
src/main/java/io/github/transactionbridge/ExampleBankNotificationParser.java
```

Implement the pure `NotificationParser` interface:

```java
package io.github.transactionbridge;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExampleBankNotificationParser implements NotificationParser {
    private static final Pattern PAYMENT = Pattern.compile(
            "Payment of ([0-9][0-9.,]*) EUR at (.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override public Transaction parse(long occurredAt, String rawText) {
        String text = ParserSupport.normalize(rawText);
        Matcher match = PAYMENT.matcher(text);
        if (!match.find()) return null;
        try {
            BigDecimal amount = ParserSupport.amount(match.group(1));
            String merchant = match.group(2).trim();
            return amount.signum() > 0 && !merchant.isEmpty()
                    ? new Transaction(occurredAt, amount, "EUR", merchant, text,
                    "example-bank-notification")
                    : null;
        } catch (NumberFormatException invalidAmount) {
            return null;
        }
    }
}
```

Parser rules:

- return `null` for unrelated, incomplete, or invalid notifications;
- do not depend on Android classes, storage, network, or settings;
- use `BigDecimal` and the existing `ParserSupport` helpers for amounts;
- require a positive amount and non-empty merchant;
- use the supplied notification timestamp unless the notification contains a more authoritative transaction time;
- choose a stable lowercase `source`, normally `<provider>-notification`.

Do not change `Transaction` or the public parser contract unless the new observed format requires it.

### 2. Register the Android package

Edit `ParserRegistry.java`:

```java
public static final String EXAMPLE_BANK_PACKAGE = "com.example.bank";
```

Then register the package, setting key, label, and parser in `defaultRegistry()`:

```java
registry.register(EXAMPLE_BANK_PACKAGE, "example_bank", "Example Bank",
        new ExampleBankNotificationParser());
```

Do not guess a package name that was not supplied or verified. Registration makes the provider available to library consumers; they may use the setting key and label metadata to build their own settings UI.

The setting key (`example_bank`) is separate from the transaction source (`example-bank-notification`).

### 3. Add tests

Edit:

```text
src/test/java/io/github/transactionbridge/NotificationParserTest.java
```

Add at least:

```java
Transaction payment = new ExampleBankNotificationParser().parse(TIME,
        "Payment of 12,50 EUR at Example Market");
assertEquals("12.50", payment.amount.toPlainString());
assertEquals("Example Market", payment.merchant);
assertEquals("example-bank-notification", payment.source);

assertNull(new ExampleBankNotificationParser().parse(TIME,
        "Your available balance is 12,50 EUR"));
```

Also verify the registry mapping:

```java
ParserRegistry.Provider provider = registry.providerFor(ParserRegistry.EXAMPLE_BANK_PACKAGE);
assertEquals("example_bank", provider.settingKey);
assertEquals("Example Bank", provider.label);
```

Use more fixtures only when they represent real format variants. Do not add speculative regex alternatives.

### 4. Document and verify

Update the supported-provider list in `README.md`. Record the notification language and app version observed in the pull request.

Run:

```sh
./gradlew test
git diff --check
```

Before opening the pull request, search the diff for personal information and confirm that the parser still rejects the negative fixture.

### Files changed for a normal provider

| File                                 | Change                                           |
| ------------------------------------ | ------------------------------------------------ |
| `ExampleBankNotificationParser.java` | New pure parser                                  |
| `ParserRegistry.java`                | Package, setting, label, and parser registration |
| `NotificationParserTest.java`        | Positive, negative, and registry checks          |
| `README.md`                          | Supported notification examples                  |

That is the complete path. Parsing, normalization, transaction IDs, and registry metadata are shared by the library; storage and delivery remain the responsibility of each consuming app.
