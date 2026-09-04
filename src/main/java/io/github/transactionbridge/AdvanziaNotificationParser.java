package io.github.transactionbridge;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AdvanziaNotificationParser implements NotificationParser {
    private static final Pattern PAYMENT = Pattern.compile(
            "^Transazione con Carta Un pagamento di ([0-9][0-9.,]*) € tramite la carta Mastercard "
                    + "che finisce con [0-9]{4} verso (?!.*\\bnon è stato effettuato correttamente\\.$)"
                    + "(.+) è stato effettuato correttamente\\.$",
            Pattern.CASE_INSENSITIVE);

    @Override public Transaction parse(long occurredAt, String rawText) {
        String text = ParserSupport.normalize(rawText);
        Matcher matcher = PAYMENT.matcher(text);
        if (!matcher.matches()) return null;
        try {
            BigDecimal amount = ParserSupport.amount(matcher.group(1));
            String merchant = matcher.group(2).trim();
            return amount.signum() > 0 && !merchant.isEmpty()
                    ? new Transaction(occurredAt, amount, "EUR", merchant, text, "advanzia-notification")
                    : null;
        } catch (NumberFormatException invalidAmount) {
            return null;
        }
    }
}
