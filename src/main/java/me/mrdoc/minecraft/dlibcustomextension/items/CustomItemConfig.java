package me.mrdoc.minecraft.dlibcustomextension.items;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@ConfigSerializable
public class CustomItemConfig {

    @Comment("If items only can be enabled by this file, false for enable all items. (Change this require restart server)")
    private boolean enabled = false;

    @Comment("Items enabled")
    private List<String> nameItems = new ArrayList<>();


}
