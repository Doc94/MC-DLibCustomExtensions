package me.mrdoc.minecraft.dlibcustomextension.potions.potion;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@ConfigSerializable
public class CustomPotionConfig {

    @Comment(value = "If items only can be enabled by this file, false for enable all potions. (Change this require restart server)", override = true)
    private boolean enabled = false;

    @Comment(value = "Potions enabled", override = true)
    private List<String> namePotions = new ArrayList<>();


}
