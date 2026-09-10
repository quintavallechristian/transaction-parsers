package io.github.transactionbridge;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class N26NotificationParserTest {
    private static final long TIME = 1_786_000_000_000L;

    @Test
    public void shouldParseSupportedPurchases() {
        Transaction n26 = new N26NotificationParser().parse(TIME,
                "Your payment of €7.04 to EXAMPLE SHOP has been successfully processed.");
        assertEquals("7.04", n26.amount.toPlainString());
        assertEquals("EXAMPLE SHOP", n26.merchant);
        assertEquals("n26-notification", n26.source);
    }

    @Test
    public void shouldRejectUnrelatedText() {
        assertNull(new N26NotificationParser().parse(TIME,
                "SECURITY went up by 5% since the last closing price."));
        assertNull(new N26NotificationParser().parse(TIME,
                "John Smith has received your bank transfer of €3"));
    }
}
