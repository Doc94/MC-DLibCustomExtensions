package dev.mrdoc.minecraft.dlibcustomextension.utils.persistence;

import net.kyori.adventure.key.Key;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PersistentDataKey implements PersistentDataType<String, Key> {

    public static PersistentDataKey KEY_CONTAINER = new PersistentDataKey();

    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public Class<Key> getComplexType() {
        return Key.class;
    }

    @Override
    public String toPrimitive(Key complex, PersistentDataAdapterContext context) {
        return complex.asString();
    }

    @Override
    public Key fromPrimitive(String primitive, PersistentDataAdapterContext context) {
        return Key.key(primitive);
    }

}
