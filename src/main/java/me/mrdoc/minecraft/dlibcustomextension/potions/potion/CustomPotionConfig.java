package me.mrdoc.minecraft.dlibcustomextension.potions.potion;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@ConfigSerializable
public class CustomPotionConfig {

    @Comment("If items only can be enabled by this file, false for enable all potions. (Change this require restart server)")
    private boolean enabled = false;

    @Comment("Potions enabled")
    private List<String> namePotions = new ArrayList<>();


}
