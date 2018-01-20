package net.kyuzi.factionswealth.utility;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class BukkitUtils {

    public static EntityType getEntityTypeFromString(String entityType) {
        for (EntityType type : EntityType.values()) {
            if (type.name().equalsIgnoreCase(entityType)) {
                return type;
            }
        }

        return null;
    }

    public static Material getMaterialFromString(String materialString) {
        boolean isId;

        try {
            Integer.parseInt(materialString);
            isId = true;
        } catch (NumberFormatException e) {
            isId = false;
        }

        return isId ? Material.getMaterial(Integer.parseInt(materialString)) : Material.getMaterial(materialString);
    }

}
