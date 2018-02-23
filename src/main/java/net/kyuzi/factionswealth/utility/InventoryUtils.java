package net.kyuzi.factionswealth.utility;

import net.kyuzi.factionswealth.FactionsWealth;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

    public static double calculateChestValue(ItemStack[] contents) {
        double value = 0D;

        for (ItemStack itemStack : contents) {
            if (itemStack == null) {
                continue;
            }

            if (itemStack.getType() == Material.MOB_SPAWNER) {
                if (FactionsWealth.isHookedSilkSpawners()) {
                    int id = FactionsWealth.getInstance().getSilkSpawnersHook().getStoredSpawnerItemEntityID(itemStack);
                    EntityType entityType = EntityType.fromId(id);

                    if (entityType != null) {
                        value += FactionsWealth.getInstance().getSpawners().getOrDefault(entityType, 0D) * itemStack.getAmount();
                    }
                }
            } else if (itemStack.getType().isBlock()) {
                value += FactionsWealth.getInstance().getBlocks().getOrDefault(itemStack.getType(), 0D) * itemStack.getAmount();
            } else {
                value += FactionsWealth.getInstance().getItems().getOrDefault(itemStack.getType(), 0D) * itemStack.getAmount();
            }
        }

        return value;
    }

}
