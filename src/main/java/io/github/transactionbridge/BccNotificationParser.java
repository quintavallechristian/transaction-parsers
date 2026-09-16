package io.github.transactionbridge;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BccNotificationParser implements NotificationParser {
    private static final Pattern PAYMENT = Pattern.compile(
            "^RelaxBanking Nuova richiesta su CartaBCC \\*[0-9]{3} di EUR ([0-9][0-9.,]*)$",
            Pattern.CASE_INSENSITIVE);

    @Override public Transaction parse(long occurredAt, String rawText) {
        String text = ParserSupport.normalize(rawText);
        Matcher matcher = PAYMENT.matcher(text);
        if (!matcher.matches()) return null;
        try {
            BigDecimal amount = ParserSupport.amount(matcher.group(1));
            return amount.signum() > 0
                    ? new Transaction(occurredAt, amount, "EUR", "CartaBCC", text, "bcc-notification")
                    : null;
        } catch (NumberFormatException invalidAmount) {
            return null;
        }
    }
}
