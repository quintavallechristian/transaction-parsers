package io.github.transactionbridge;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class N26NotificationParser implements NotificationParser {
    private static final Pattern PAYMENT_NOTIFICATION_PATTERN = Pattern.compile(
            "^Your payment of €([0-9][0-9.,]*) to (.+?) has been successfully processed\\.$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public Transaction parse(long occurredAt, String rawText) {
        String text = ParserSupport.normalize(rawText);
        Matcher matcher = PAYMENT_NOTIFICATION_PATTERN.matcher(text);
        if (!matcher.find()) return null;
        try {
            BigDecimal amount = ParserSupport.amount(matcher.group(1));
            String merchant = matcher.group(2).trim();
            return amount.signum() > 0 && !merchant.isEmpty()
                    ? new Transaction(occurredAt, amount, "EUR", merchant, text, "n26-notification")
                    : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}