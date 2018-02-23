package net.kyuzi.factionswealth.hook;

import de.dustplanet.util.SilkUtil;

import net.kyuzi.factionswealth.exception.HookFailureException;

import org.bukkit.inventory.ItemStack;

public class SilkSpawnersHook {

    public SilkSpawnersHook() throws HookFailureException {
        try {
            Class.forName("de.dustplanet.util.SilkUtil");
        } catch (ClassNotFoundException ignored) {
            throw new HookFailureException("SilkUtil class not found!");
        }
    }

    public int getStoredSpawnerItemEntityID(ItemStack itemStack) {
        return SilkUtil.hookIntoSilkSpanwers().getStoredSpawnerItemEntityID(itemStack);
    }

}
