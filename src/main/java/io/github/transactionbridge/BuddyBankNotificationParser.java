package io.github.transactionbridge;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BuddyBankNotificationParser implements NotificationParser {
    private static final Pattern PAYMENT = Pattern.compile(
            "Pagamento con carta di debito UniCredit segnala: autorizzato pagamento ([0-9][0-9.,]*) EUR "
                    + "carta di debito XX[0-9]{4} c/o (.+?) [0-9]{2}/[0-9]{2}/[0-9]{2} [0-9]{2}:[0-9]{2} \\."
                    + " Per info o blocco [0-9]+", Pattern.CASE_INSENSITIVE);

    @Override public Transaction parse(long occurredAt, String rawText) {
        String text = ParserSupport.normalize(rawText);
        Matcher matcher = PAYMENT.matcher(text);
        if (!matcher.matches()) return null;
        try {
            BigDecimal amount = ParserSupport.amount(matcher.group(1));
            String merchant = matcher.group(2).trim();
            return amount.signum() > 0 && !merchant.isEmpty()
                    ? new Transaction(occurredAt, amount, "EUR", merchant, text, "buddybank-notification")
                    : null;
        } catch (NumberFormatException invalidAmount) {
            return null;
        }
    }
}
