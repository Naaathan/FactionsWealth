package net.kyuzi.factionswealth.task.calculate;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.task.Task;
import net.kyuzi.factionswealth.utility.InventoryUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CalculateChunkTask extends Task {

    private Map<Material, Integer> blocks;
    private List<Block> chestBlocks;
    private double chestValue;
    private FLocation claim;
    private Faction faction;
    private Map<EntityType, Integer> spawners;

    public CalculateChunkTask(FLocation claim, Faction faction) {
        super(true);
        this.blocks = new HashMap<>();
        this.chestBlocks = new ArrayList<>();
        this.chestValue = 0D;
        this.claim = claim;
        this.faction = faction;
        this.spawners = new HashMap<>();
    }

    public Map<Material, Integer> getBlocks() {
        return blocks;
    }

    public double getChestValue() {
        return chestValue;
    }

    public Faction getFaction() {
        return faction;
    }

    public Map<EntityType, Integer> getSpawners() {
        return spawners;
    }

    @Override
    public void run() {
        World world = claim.getWorld();

        if (world == null) {
            return;
        }

        Chunk chunk = world.getChunkAt((int) claim.getX(), (int) claim.getZ());

        if (chunk == null) {
            return;
        }

        ChunkSnapshot snapshot = chunk.getChunkSnapshot();

        for (int y = 0; y < world.getMaxHeight(); y++) {
            if (snapshot.isSectionEmpty(y >> 4)) {
                y += 15;
                continue;
            }

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);

                    if (block == null) {
                        continue;
                    }

                    if (block.getState() instanceof Chest) {
                        if (FactionsWealth.getInstance().shouldIncludeChestContent()) {
                            chestValue += InventoryUtils.calculateChestValue(((Chest) block.getState()).getBlockInventory().getContents());
                        }
                    } else if (block.getState() instanceof CreatureSpawner) {
                        EntityType entityType = ((CreatureSpawner) block.getState()).getSpawnedType();
                        spawners.put(entityType, spawners.getOrDefault(entityType, 0) + 1);
                    } else if (block.getState() instanceof DoubleChest) {
                        if (FactionsWealth.getInstance().shouldIncludeChestContent()) {
                            if (!hasAddedChest(block)) {
                                chestBlocks.add(block);
                                chestValue += InventoryUtils.calculateChestValue(((DoubleChest) block.getState()).getInventory().getContents());
                            }
                        }
                    } else {
                        Material type = block.getType();
                        blocks.put(type, blocks.getOrDefault(type, 0) + 1);
                    }
                }
            }
        }

        complete = true;
        done();
    }

    private boolean hasAddedChest(Block block) {
        if (!chestBlocks.isEmpty()) {
            for (Block chestBlock : chestBlocks) {
                if (block.getType() == chestBlock.getType() && block.getY() == chestBlock.getY()) {
                    if ((block.getX() == chestBlock.getX() && Math.abs(block.getZ() - chestBlock.getZ()) == 1) || (Math.abs(block.getX() - chestBlock.getX()) == 1 && block.getZ() == chestBlock.getZ())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
