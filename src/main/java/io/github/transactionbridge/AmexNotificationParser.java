package io.github.transactionbridge;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AmexNotificationParser implements NotificationParser {
    private static final Pattern PAYMENT = Pattern.compile("^(.+?) ([0-9][0-9.,]*) €$");
    private static final Pattern ITALIAN_PAYMENT = Pattern.compile(
            "^(?:Amex IT )?Hai speso €([0-9][0-9.,]*) presso (.+?) con la tua Carta che termina con [0-9]+\\.$");

    @Override public Transaction parse(long occurredAt, String rawText) {
        String text = ParserSupport.normalize(rawText);
        Matcher matcher = PAYMENT.matcher(text);
        if (!matcher.find()) {
            matcher = ITALIAN_PAYMENT.matcher(text);
            if (!matcher.find()) return null;
            return transaction(occurredAt, text, matcher.group(1), matcher.group(2));
        }
        return transaction(occurredAt, text, matcher.group(2), matcher.group(1));
    }

    private Transaction transaction(long occurredAt, String text, String amountText, String merchantText) {
        try {
            BigDecimal amount = ParserSupport.amount(amountText);
            String merchant = merchantText.trim();
            return amount.signum() > 0 && !merchant.isEmpty()
                    ? new Transaction(occurredAt, amount, "EUR", merchant, text, "amex-notification")
                    : null;
        } catch (NumberFormatException invalidAmount) {
            return null;
        }
    }
}
