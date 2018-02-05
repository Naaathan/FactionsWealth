package net.kyuzi.factionswealth.listener;

import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerPlaceEvent;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.utility.Operator;
import net.kyuzi.factionswealth.task.update.WealthUpdate;
import net.kyuzi.factionswealth.task.update.WealthUpdateTask;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SilkSpawnersListener implements Listener {

    @EventHandler
    public void onSpawnerBreak(SilkSpawnersSpawnerBreakEvent e) {
        WealthUpdateTask wealthUpdateTask = FactionsWealth.getInstance().getWealthUpdateTask();

        if (wealthUpdateTask == null) {
            return;
        }

        wealthUpdateTask.updateWealth(e.getBlock(), e.getPlayer(), new WealthUpdate.SpawnerUpdate(Operator.SUBTRACT, e.getSpawner().getSpawnedType()));
    }

    @EventHandler
    public void onSpawnerChange(SilkSpawnersSpawnerChangeEvent e) {
        WealthUpdateTask wealthUpdateTask = FactionsWealth.getInstance().getWealthUpdateTask();

        if (wealthUpdateTask == null) {
            return;
        }

        Block block = e.getBlock();
        EntityType spawnerNew = EntityType.fromId(e.getEntityID());
        EntityType spawnerOld = EntityType.fromId(e.getOldEntityID());

        if (block != null) {
            if (spawnerNew != null) {
                wealthUpdateTask.updateWealth(block, e.getPlayer(), new WealthUpdate.SpawnerUpdate(Operator.ADD, spawnerNew));
            }

            if (spawnerOld != null) {
                wealthUpdateTask.updateWealth(block, e.getPlayer(), new WealthUpdate.SpawnerUpdate(Operator.SUBTRACT, spawnerOld));
            }
        }
    }

    @EventHandler
    public void onSpawnerPlace(SilkSpawnersSpawnerPlaceEvent e) {
        WealthUpdateTask wealthUpdateTask = FactionsWealth.getInstance().getWealthUpdateTask();

        if (wealthUpdateTask == null) {
            return;
        }

        EntityType spawnerType = EntityType.fromId(e.getEntityID());

        if (spawnerType == null) {
            return;
        }

        wealthUpdateTask.updateWealth(e.getBlock(), e.getPlayer(), new WealthUpdate.SpawnerUpdate(Operator.ADD, spawnerType));
    }

}
