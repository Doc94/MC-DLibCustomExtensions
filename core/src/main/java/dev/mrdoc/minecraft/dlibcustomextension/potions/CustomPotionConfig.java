package dev.mrdoc.minecraft.dlibcustomextension.potions;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

/**
 * The general config for this module.
 */
@ApiStatus.Internal
@Getter
@ConfigSerializable
public class CustomPotionConfig {

    /**
     * If potion only can be enabled by config.
     *
     * @return bool
     */
    @Comment(value = "If items only can be enabled by this file, false for enable all potions. (Change this require restart server)", override = true)
    private boolean enabled = false;

    /**
     * Potions enabled by config.
     *
     * @return list
     */
    @Comment(value = "Potions enabled", override = true)
    private List<String> namePotions = new ArrayList<>();


}
