package net.kyuzi.factionswealth.listener;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.event.*;
import com.massivecraft.factions.zcore.persist.json.JSONFactions;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.Operator;
import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.task.calculate.CalculateChunkTask;
import net.kyuzi.factionswealth.task.update.WealthUpdate;
import net.kyuzi.factionswealth.task.update.WealthUpdateTask;
import net.kyuzi.factionswealth.utility.InventoryUtils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BasicListener implements Listener {

    private Map<UUID, Double> startInventories;

    public BasicListener() {
        this.startInventories = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }

        WealthUpdateTask wealthUpdateTask = FactionsWealth.getInstance().getWealthUpdateTask();

        if (wealthUpdateTask == null) {
            return;
        }

        Block block = e.getBlock();

        switch (block.getType()) {
            case CHEST:
                updateChestWealth(block, e.getPlayer(), wealthUpdateTask);
                break;
            case MOB_SPAWNER:
                return;
            default:
                wealthUpdateTask.updateWealth(block, e.getPlayer(), new WealthUpdate.BlockUpdate(Operator.SUBTRACT, block.getType()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent e) {
        if (e.isCancelled()) {
            return;
        }

        WealthUpdateTask wealthUpdateTask = FactionsWealth.getInstance().getWealthUpdateTask();

        if (wealthUpdateTask == null) {
            return;
        }

        explosionUpdate(e.blockList(), wealthUpdateTask);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }

        WealthUpdateTask wealthUpdateTask = FactionsWealth.getInstance().getWealthUpdateTask();

        if (wealthUpdateTask == null) {
            return;
        }

        Block block = e.getBlock();

        if (block.getType() == Material.MOB_SPAWNER) {
            return;
        }

        wealthUpdateTask.updateWealth(block, e.getPlayer(), new WealthUpdate.BlockUpdate(Operator.ADD, block.getType()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.isCancelled()) {
            return;
        }

        WealthUpdateTask wealthUpdateTask = FactionsWealth.getInstance().getWealthUpdateTask();

        if (wealthUpdateTask == null) {
            return;
        }

        explosionUpdate(e.blockList(), wealthUpdateTask);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFactionCreate(FactionCreateEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (Factions.getInstance() instanceof JSONFactions) {
            JSONFactions factions = (JSONFactions) Factions.getInstance();

            FactionsWealth.getInstance().getStorage().addValuedFaction(new ValuedFaction(factions.getNextId(), new HashMap<>(), 0D, new HashMap<>()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFactionDisband(FactionDisbandEvent e) {
        if (e.isCancelled()) {
            return;
        }

        FactionsWealth.getInstance().getStorage().removeValuedFaction(e.getFaction().getId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!FactionsWealth.getInstance().shouldIncludeChestContent()) {
            return;
        }

        Inventory inventory = e.getInventory();

        if (!(inventory.getHolder() instanceof Chest) && !(inventory.getHolder() instanceof DoubleChest)) {
            return;
        }

        WealthUpdateTask wealthUpdateTask = FactionsWealth.getInstance().getWealthUpdateTask();

        if (wealthUpdateTask == null) {
            return;
        }

        Player player = (Player) e.getPlayer();

        if (!startInventories.containsKey(player.getUniqueId())) {
            return;
        }

        Block block;

        if (inventory.getHolder() instanceof Chest) {
            block = ((Chest) inventory.getHolder()).getBlock();
        } else {
            block = ((DoubleChest) inventory.getHolder()).getLocation().getBlock();
        }

        double newValue = InventoryUtils.calculateChestValue(inventory.getContents());
        double oldValue = startInventories.get(player.getUniqueId());

        wealthUpdateTask.updateWealth(block, player, new WealthUpdate.ChestUpdate(Operator.ADD, newValue - oldValue));
        startInventories.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!FactionsWealth.getInstance().shouldIncludeChestContent()) {
            return;
        }

        Inventory inventory = e.getInventory();

        if (!(inventory.getHolder() instanceof Chest) && !(inventory.getHolder() instanceof DoubleChest)) {
            return;
        }

        WealthUpdateTask wealthUpdateTask = FactionsWealth.getInstance().getWealthUpdateTask();

        if (wealthUpdateTask == null) {
            return;
        }

        Player player = (Player) e.getPlayer();

        startInventories.put(player.getUniqueId(), InventoryUtils.calculateChestValue(inventory.getContents()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLandClaim(LandClaimEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Faction faction = e.getFaction();

        new CalculateChunkTask(e.getLocation(), faction) {

            @Override
            public void done() {
                ValuedFaction valuedFaction = FactionsWealth.getInstance().getStorage().getValuedFaction(faction.getId());

                valuedFaction.merge(Operator.ADD, this.getBlocks(), this.getChestValue(), this.getSpawners());
                FactionsWealth.getInstance().getStorage().addValuedFaction(valuedFaction);
            }

        }.start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLandUnclaim(LandUnclaimEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Faction faction = e.getFaction();

        new CalculateChunkTask(e.getLocation(), faction) {

            @Override
            public void done() {
                ValuedFaction valuedFaction = FactionsWealth.getInstance().getStorage().getValuedFaction(faction.getId());

                valuedFaction.merge(Operator.SUBTRACT, this.getBlocks(), this.getChestValue(), this.getSpawners());
                FactionsWealth.getInstance().getStorage().addValuedFaction(valuedFaction);
            }

        }.start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLandUnclaimAll(LandUnclaimAllEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Faction faction = e.getFaction();
        ValuedFaction valuedFaction = FactionsWealth.getInstance().getStorage().getValuedFaction(faction.getId());

        valuedFaction.reset();
        FactionsWealth.getInstance().getStorage().addValuedFaction(valuedFaction);
    }

    private void explosionUpdate(List<Block> blockList, WealthUpdateTask wealthUpdateTask) {
        if (!blockList.isEmpty()) {
            for (Block block : blockList) {
                if (block == null) {
                    continue;
                }

                switch (block.getType()) {
                    case CHEST:
                        updateChestWealth(block, null, wealthUpdateTask);
                        break;
                    case MOB_SPAWNER:
                        if (block.getState() instanceof CreatureSpawner) {
                            wealthUpdateTask.updateWealth(block, null, new WealthUpdate.SpawnerUpdate(Operator.SUBTRACT, ((CreatureSpawner) block.getState()).getSpawnedType()));
                        }
                        break;
                    default:
                        wealthUpdateTask.updateWealth(block, null, new WealthUpdate.BlockUpdate(Operator.SUBTRACT, block.getType()));
                }
            }
        }
    }

    private void updateChestWealth(Block block, Player player, WealthUpdateTask wealthUpdateTask) {
        if (FactionsWealth.getInstance().shouldIncludeChestContent()) {
            double newValue = 0D;
            double oldValue;

            if (block.getState() instanceof Chest) {
                oldValue = InventoryUtils.calculateChestValue(((Chest) block.getState()).getBlockInventory().getContents());

                wealthUpdateTask.updateWealth(block, player, new WealthUpdate.ChestUpdate(Operator.ADD, newValue - oldValue));
            } else if (block.getState() instanceof DoubleChest) {
                oldValue = InventoryUtils.calculateChestValue(((DoubleChest) block.getState()).getInventory().getContents());

                wealthUpdateTask.updateWealth(block, player, new WealthUpdate.ChestUpdate(Operator.ADD, newValue - oldValue));
            }
        }
    }

}
