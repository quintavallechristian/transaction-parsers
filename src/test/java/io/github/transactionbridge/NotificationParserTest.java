package io.github.transactionbridge;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class NotificationParserTest {
    private static final long TIME = 1_786_000_000_000L;

    @Test public void parsesSupportedPurchasesAndRejectsUnrelatedText() {
        Transaction ing = new IngNotificationParser().parse(TIME,
                "Operazione autorizzata: 24.61 euro, Example Market. Non sei stato tu? Blocca subito la carta.");
        assertEquals("24.61", ing.amount.toPlainString());
        assertEquals("Example Market", ing.merchant);

        Transaction directDebit = new IngNotificationParser().parse(TIME,
                "Addebito diretto di 7.99 euro richiesto da Creditor id. IT00ZZZ**************** "
                        + "EXAMPLE MOBILE: pagato! Non ti risulta? Contattaci subito.");
        assertEquals("7.99", directDebit.amount.toPlainString());
        assertEquals("EXAMPLE MOBILE", directDebit.merchant);
        assertEquals("ing-notification", directDebit.source);
        assertNull(new IngNotificationParser().parse(TIME,
                "Addebito diretto di 7.99 euro richiesto da EXAMPLE MOBILE: in elaborazione."));

        Transaction instantTransfer = new IngNotificationParser().parse(TIME,
                "Bonifico istantaneo OK! Bonifico istantaneo di 700 euro: fatto! "
                        + "Non sei stato tu? Contattaci subito.");
        assertEquals("700", instantTransfer.amount.toPlainString());
        assertEquals("Bonifico istantaneo", instantTransfer.merchant);
        assertEquals("ing-notification", instantTransfer.source);
        assertNull(new IngNotificationParser().parse(TIME,
                "Bonifico istantaneo di 700 euro: in elaborazione."));
        assertNull(new IngNotificationParser().parse(TIME, "Saldo disponibile: 24.61 euro"));

        Transaction crypto = new CryptoComNotificationParser().parse(TIME,
                "€93.30 EUR spent at Example Shop\nYou earned €2.74 EUR of rewards");
        assertEquals("93.30", crypto.amount.toPlainString());
        assertEquals("EUR", crypto.currency);
        assertNull(new CryptoComNotificationParser().parse(TIME, "You received 17.45 EUR"));

        Transaction revolut = new RevolutNotificationParser().parse(TIME,
                "Example Petrol ⛽ Hai speso 28,80 € Saldo di EUR: 669,78 €");
        assertEquals("28.80", revolut.amount.toPlainString());
        Transaction revolutBankTransfer = new RevolutNotificationParser().parse(TIME,
                "Example Capital Bank GmbH 📅 Hai pagato 200 € Saldo: 143,70 €");
        assertEquals("200", revolutBankTransfer.amount.toPlainString());
        assertEquals("Example Capital Bank GmbH", revolutBankTransfer.merchant);
        assertNull(new RevolutNotificationParser().parse(TIME,
                "Hai ricevuto 28,80 € da Another Person"));

        Transaction transfer = new RevolutNotificationParser().parse(TIME,
                "Denaro inviato ✅ Hai inviato 125 € a EXAMPLE CHARITY. Arriverà in pochi secondi");
        assertEquals("125", transfer.amount.toPlainString());
        assertEquals("EXAMPLE CHARITY", transfer.merchant);
        assertEquals("revolut-notification", transfer.source);

        Transaction bbva = new BbvaNotificationParser().parse(TIME,
                "Pagamento accettato 👍 💳 Il pagamento di 12,50 EUR in data EXAMPLE SHOP "
                        + "effettuato con la tua carta 1234 è stato accettato.");
        assertEquals("12.50", bbva.amount.toPlainString());
        assertEquals("EUR", bbva.currency);
        assertEquals("EXAMPLE SHOP", bbva.merchant);
        assertEquals("bbva-notification", bbva.source);
        assertNull(new BbvaNotificationParser().parse(TIME,
                "Il pagamento di 12,50 EUR in data EXAMPLE SHOP effettuato con la tua carta 1234 è stato rifiutato."));

        Transaction hype = new HypeNotificationParser().parse(TIME, "EXAMPLE SHOP, EXAMPLE CITY 12,50 €");
        assertEquals("12.50", hype.amount.toPlainString());
        assertEquals("EUR", hype.currency);
        assertEquals("EXAMPLE SHOP, EXAMPLE CITY", hype.merchant);
        assertEquals("hype-notification", hype.source);
        assertNull(new HypeNotificationParser().parse(TIME, "12,50 €"));

        Transaction amex = new AmexNotificationParser().parse(TIME, "EXAMPLE HOTEL 12,50 €");
        assertEquals("12.50", amex.amount.toPlainString());
        assertEquals("EUR", amex.currency);
        assertEquals("EXAMPLE HOTEL", amex.merchant);
        assertEquals("amex-notification", amex.source);
        assertNull(new AmexNotificationParser().parse(TIME, "12,50 €"));
        Transaction amexItalian = new AmexNotificationParser().parse(TIME,
                "Amex IT Hai speso €15,00 presso EXAMPLE PIZZERIA con la tua Carta che termina con 1234.");
        assertEquals("15.00", amexItalian.amount.toPlainString());
        assertEquals("EXAMPLE PIZZERIA", amexItalian.merchant);
        assertEquals("amex-notification", amexItalian.source);
        assertNull(new AmexNotificationParser().parse(TIME,
                "Hai speso €15,00 presso EXAMPLE PIZZERIA con la tua Carta che termina con 1234"));

        Transaction advanzia = new AdvanziaNotificationParser().parse(TIME,
                "Transazione con Carta Un pagamento di 9,16 € tramite la carta Mastercard che finisce "
                        + "con 1234 verso EXAMPLE MARKET S.R.L. è stato effettuato correttamente.");
        assertEquals("9.16", advanzia.amount.toPlainString());
        assertEquals("EUR", advanzia.currency);
        assertEquals("EXAMPLE MARKET S.R.L.", advanzia.merchant);
        assertEquals("advanzia-notification", advanzia.source);
        assertNull(new AdvanziaNotificationParser().parse(TIME,
                "Transazione con Carta Un pagamento di 9,16 € tramite la carta Mastercard che finisce "
                        + "con 1234 verso EXAMPLE MARKET S.R.L. non è stato effettuato correttamente."));
    }

    @Test public void parsesBuddyBankItalianDebitCardPayment() {
        Transaction transaction = new BuddyBankNotificationParser().parse(TIME,
                "Pagamento con carta di debito UniCredit segnala: autorizzato pagamento 4,43 EUR "
                        + "carta di debito XX0000 c/o EXAMPLE MARKET CITY 10/09/26 14:50 . Per info o blocco 000000000");
        assertEquals("4.43", transaction.amount.toPlainString());
        assertEquals("EUR", transaction.currency);
        assertEquals("EXAMPLE MARKET CITY", transaction.merchant);
        assertEquals("buddybank-notification", transaction.source);
        assertNull(new BuddyBankNotificationParser().parse(TIME,
                "Pagamento con carta di debito UniCredit segnala: rifiutato pagamento 4,43 EUR "
                        + "carta di debito XX0000 c/o EXAMPLE MARKET CITY 10/09/26 14:50 . Per info o blocco 000000000"));
    }

    @Test public void parsesBccCardRequest() {
        Transaction transaction = new BccNotificationParser().parse(TIME,
                "RelaxBanking Nuova richiesta su CartaBCC *000 di EUR 76,00");
        assertEquals("76.00", transaction.amount.toPlainString());
        assertEquals("EUR", transaction.currency);
        assertEquals("CartaBCC", transaction.merchant);
        assertEquals("bcc-notification", transaction.source);
        assertNull(new BccNotificationParser().parse(TIME,
                "RelaxBanking Richiesta rifiutata su CartaBCC *000 di EUR 76,00"));
	}

    @Test public void parsesUniCreditItalianDebitCardPayment() {
        Transaction transaction = new UniCreditNotificationParser().parse(TIME,
                "Pagamento con carta di debito UniCredit segnala: autorizzato pagamento 12,34 EUR "
                        + "carta di debito XX0000 c/o EXAMPLE SUPERMARKET 10/09/26 14:50 . "
                        + "Per info o blocco 000000000");
        assertEquals("12.34", transaction.amount.toPlainString());
        assertEquals("EUR", transaction.currency);
        assertEquals("EXAMPLE SUPERMARKET", transaction.merchant);
        assertEquals("unicredit-notification", transaction.source);
        assertNull(new UniCreditNotificationParser().parse(TIME,
                "Pagamento con carta di debito UniCredit segnala: rifiutato pagamento 12,34 EUR "
                        + "carta di debito XX0000 c/o EXAMPLE SUPERMARKET 10/09/26 14:50 . "
                        + "Per info o blocco 000000000"));
    }

    @Test public void parsesIsyBankDateAndMaskedTransferWithoutOwnerNames() {
        Transaction debit = new IsyBankNotificationParser().parse(TIME,
                "E' stato addebitato il pagamento di una domiciliazione di 28,89 € da parte di EXAMPLE PROVIDER "
                        + "sul conto xxx421 in data 10.08.2026");
        assertEquals("28.89", debit.amount.toPlainString());
        assertEquals("EXAMPLE PROVIDER", debit.merchant);

        Transaction transfer = new IsyBankNotificationParser().parse(TIME,
                "È stato inserito un bonifico istantaneo di 30,00 € dal conto xxx421 in favore dell'IBAN DE*** "
                        + "in data 13.08.2026 alle ore 17:14.");
        assertEquals("Bonifico DE***", transfer.merchant);

        Transaction european = new IsyBankNotificationParser().parse(TIME,
                "E' stato inserito il pagamento di un bonifico europeo di 128,50 € dal conto xxx421 " +
                        "in favore dell'IBAN DE*** il 26.08.2026 alle ore 17:59 con data di addebito il 27.08.2026.");
        assertEquals("128.50", european.amount.toPlainString());
        assertEquals("Bonifico DE***", european.merchant);

        assertNull(new IsyBankNotificationParser().parse(TIME,
                "Il saldo del conto xxx421 e' 128,50 EUR"));
    }

    @Test public void isyBankKeepsDeliveryRetriesStableButTreatsNewPostsSeparately() {
        String text = "E' stato addebitato il pagamento di una domiciliazione di 28,89 € da parte di "
                + "EXAMPLE PROVIDER sul conto xxx421 in data 10.08.2026";
        IsyBankNotificationParser parser = new IsyBankNotificationParser();
        Transaction first = parser.parse(TIME, text);

        assertEquals(first.id, parser.parse(TIME, text).id);
        org.junit.Assert.assertNotEquals(first.id, parser.parse(TIME + 1, text).id);
    }

    @Test public void parsesWalletOnlyWhenConfiguredCardMatches() {
        Map<String, String> cards = new HashMap<>();
        cards.put("1501", "Example Card");
        GoogleWalletNotificationParser parser = new GoogleWalletNotificationParser(cards);
        Transaction transaction = parser.parse(TIME, "Example Market 24,61 € con Carta Visa ••1501");
        assertEquals("24.61", transaction.amount.toPlainString());
        assertEquals("google-wallet-example-card-notification", transaction.source);
        assertNull(parser.parse(TIME, "Example Market 24,61 € con Carta Visa ••9999"));

        Map<String, String> cryptoCard = new HashMap<>();
        cryptoCard.put("1352", "Crypto.com");
        assertEquals("google-wallet-crypto-notification",
                new GoogleWalletNotificationParser(cryptoCard)
                        .parse(TIME, "Example Market 24,61 € con Carta Visa ••1352").source);
    }

    @Test public void registryMapsOnlyKnownAndroidPackages() {
        Map<String, String> cards = new HashMap<>();
        cards.put("1501", "Example Card");
        ParserRegistry registry = ParserRegistry.defaultRegistry(cards);
        ParserRegistry.Provider ing = registry.providerFor(ParserRegistry.ING_PACKAGE);
        assertEquals("ING", ing.label);
        assertEquals("ing", ing.settingKey);
        assertEquals("ing-notification", ing.parser.parse(TIME,
                "Operazione autorizzata: 1,25 euro, Example Market.").source);
        assertProvider(registry, ParserRegistry.CRYPTO_COM_PACKAGE, "crypto.com", "Crypto.com");
        assertProvider(registry, ParserRegistry.ISYBANK_PACKAGE, "isybank", "IsyBank");
        assertProvider(registry, ParserRegistry.GOOGLE_WALLET_PACKAGE, "google_wallet", "Google Wallet");
        assertProvider(registry, ParserRegistry.REVOLUT_PACKAGE, "revolut", "Revolut");
        assertProvider(registry, ParserRegistry.BBVA_PACKAGE, "bbva", "BBVA");
        assertProvider(registry, ParserRegistry.HYPE_PACKAGE, "hype", "HYPE");
        assertProvider(registry, ParserRegistry.AMEX_PACKAGE, "amex", "American Express");
        assertProvider(registry, ParserRegistry.ADVANZIA_PACKAGE, "advanzia", "Advanzia");
        assertProvider(registry, ParserRegistry.BUDDYBANK_PACKAGE, "buddybank", "buddybank");
        assertProvider(registry, ParserRegistry.BCC_PACKAGE, "bcc", "BCC");
        assertProvider(registry, ParserRegistry.UNICREDIT_PACKAGE, "unicredit", "UniCredit");
        assertNull(registry.providerFor("com.example.other"));
    }

    private static void assertProvider(ParserRegistry registry, String packageName, String settingKey, String label) {
        ParserRegistry.Provider provider = registry.providerFor(packageName);
        assertEquals(packageName, provider.packageName);
        assertEquals(settingKey, provider.settingKey);
        assertEquals(label, provider.label);
    }

}
