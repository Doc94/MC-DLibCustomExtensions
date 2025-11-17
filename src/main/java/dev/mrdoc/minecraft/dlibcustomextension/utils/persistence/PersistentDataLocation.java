package dev.mrdoc.minecraft.dlibcustomextension.utils.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PersistentDataLocation implements PersistentDataType<String, Location> {

    public static PersistentDataLocation LOCATION_CONTAINER = new PersistentDataLocation();

    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public Class<Location> getComplexType() {
        return Location.class;
    }

    @Override
    public String toPrimitive(Location complex, PersistentDataAdapterContext context) {
        return new GsonBuilder().create().toJson(complex.serialize(), Map.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Location fromPrimitive(String primitive, PersistentDataAdapterContext context) {
        return Location.deserialize(new Gson().fromJson(primitive, Map.class));
    }
}
