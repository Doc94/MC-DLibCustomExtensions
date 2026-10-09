package dev.mrdoc.minecraft.dlibcustomextension.utils.persistence;

import net.kyori.adventure.key.Key;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public class KeyPersistentDataType implements PersistentDataType<String, Key> {

    public static KeyPersistentDataType KEY_CONTAINER = new KeyPersistentDataType();

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
