package dev.mrdoc.minecraft.dlibcustomextension.utils.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PersistentDataLocation implements PersistentDataType<String, Location> {

    public static PersistentDataLocation LOCATION_CONTAINER = new PersistentDataLocation();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<Location> getComplexType() {
        return Location.class;
    }

    @Override
    public @NotNull String toPrimitive(@NotNull Location complex, @NotNull PersistentDataAdapterContext context) {
        return new GsonBuilder().create().toJson(complex.serialize(), Map.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Location fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return Location.deserialize(new Gson().fromJson(primitive, Map.class));
    }
}
