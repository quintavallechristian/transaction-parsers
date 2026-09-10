package io.github.transactionbridge;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.sql.Types.TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ParserRegistryTest {
    @Test
    public void registryMapsOnlyKnownAndroidPackages() {
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
        assertProvider(registry, ParserRegistry.N26_PACKAGE, "n26", "N26");
        assertNull(registry.providerFor("com.example.other"));
    }

    private static void assertProvider(ParserRegistry registry, String packageName, String settingKey, String label) {
        ParserRegistry.Provider provider = registry.providerFor(packageName);
        assertEquals(packageName, provider.packageName);
        assertEquals(settingKey, provider.settingKey);
        assertEquals(label, provider.label);
    }
}
