package me.mrdoc.minecraft.dlibcustomextension.i18n;

import com.github.fracpete.romannumerals4j.RomanNumeralFormat;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;
import net.kyori.adventure.util.UTF8ResourceBundleControl;

public class TranslatesManager {

    private static TranslatesManager INSTANCE;

    public static void load() {
        if (INSTANCE == null) {
            INSTANCE = new TranslatesManager();
        }
    }

    public static TranslatesManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TranslatesManager();
        }
        return INSTANCE;
    }

    private final TranslationStore.StringBased<MessageFormat> translationStore;

    private TranslatesManager() {
        final Set<Locale> locales = Set.of(Locale.US, Locale.of("es"));

        this.translationStore = TranslationStore.messageFormat(Key.key("dlibcustomextensions:translates"));

        locales.forEach(locale -> {
            ResourceBundle bundle = ResourceBundle.getBundle("dlibcustomextensions.lang.LangBundle", locale, UTF8ResourceBundleControl.get());
            this.translationStore.registerAll(locale, bundle, true);
            this.patchEnchantmentLevels(locale);
        });

        GlobalTranslator.translator().addSource(this.translationStore);
    }

    private void patchEnchantmentLevels(Locale locale) {
        RomanNumeralFormat romanNumeralFormat = new RomanNumeralFormat();
        for (int level = 1; level <= 100; level++) {
            this.translationStore.register("enchantment.level.".concat(Integer.toString(level)), locale, new MessageFormat(romanNumeralFormat.format(level)));
        }
    }

}
