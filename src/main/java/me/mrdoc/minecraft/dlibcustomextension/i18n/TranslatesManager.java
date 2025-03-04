package me.mrdoc.minecraft.dlibcustomextension.i18n;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
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

    private TranslatesManager() {
        final Set<Locale> locales = Set.of(Locale.US, Locale.of("es"));

        TranslationRegistry registry = TranslationRegistry.create(Key.key("dlibcustomextensions:translates"));

        locales.forEach(locale -> {
            ResourceBundle bundle = ResourceBundle.getBundle("dlibcustomextensions.lang.LangBundle", locale, UTF8ResourceBundleControl.get());
            registry.registerAll(locale, bundle, true);
        });

        GlobalTranslator.translator().addSource(registry);
    }

}
