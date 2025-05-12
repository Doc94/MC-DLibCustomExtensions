package dev.mrdoc.minecraft.dlibcustomextension.enchantments;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

/**
 * The general config for this module.
 */
@Getter
@ConfigSerializable
public class CustomEnchantmentConfig {

    /**
     * If enchantment only can be enabled by config.
     *
     * @return bool
     */
    @Comment(value = "If enchantments only can be enabled by this file, false for enable all enchantments. (Change this require restart server)", override = true)
    private boolean enabled = false;

    /**
     * Enchantments enabled by config.
     *
     * @return list
     */
    @Comment(value = "Enchantments enabled", override = true)
    private List<String> nameEnchantments = new ArrayList<>();


}
