package me.mrdoc.minecraft.dlibcustomextension.enchantments;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@ConfigSerializable
public class CustomEnchantmentConfig {

    @Comment("If enchantments only can be enabled by this file, false for enable all enchantments. (Change this require restart server)")
    private boolean enabled = false;

    @Comment("Enchantments enabled")
    private List<String> nameEnchantments = new ArrayList<>();


}
