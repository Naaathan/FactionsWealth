package net.kyuzi.factionswealth.task.calculate;

import com.massivecraft.factions.Faction;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.location.BlockPos;
import net.kyuzi.factionswealth.location.ChunkPos;
import net.kyuzi.factionswealth.task.Task;

import org.bukkit.*;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class CalculateChunkTask extends Task {

    private Map<Material, Integer> blocks;
    private Map<BlockPos, Material> chestBlockPositions;
    private double chestValue;
    private ChunkSnapshot claim;
    private Faction faction;
    private Map<EntityType, Integer> spawners;
    private Map<BlockPos, Material> specialBlocks;
    private boolean wasLoaded;

    public CalculateChunkTask(ChunkSnapshot claim, Faction faction, boolean wasLoaded) {
        super(true);
        this.blocks = new HashMap<>();
        this.chestBlockPositions = new HashMap<>();
        this.chestValue = 0D;
        this.claim = claim;
        this.faction = faction;
        this.spawners = new HashMap<>();
        this.specialBlocks = new HashMap<>();
        this.wasLoaded = wasLoaded;
    }

    public void addSpecialBlocks() {
        if (!specialBlocks.isEmpty()) {
            for (Map.Entry<BlockPos, Material> specialBlockEntry : specialBlocks.entrySet()) {
                switch (specialBlockEntry.getValue()) {
                    case CHEST:
                    case TRAPPED_CHEST:
                        chestValue += specialBlockEntry.getKey().calculateChestValue();
                        break;
                    case MOB_SPAWNER:
                        EntityType spawnerType = specialBlockEntry.getKey().findSpawnerType();
                        spawners.put(spawnerType, spawners.getOrDefault(spawnerType, 0) + 1);
                }
            }
        }
    }

    @Override
    public void done() {
    }

    public Map<Material, Integer> getBlocks() {
        return blocks;
    }

    public double getChestValue() {
        return chestValue;
    }

    public ChunkSnapshot getClaim() {
        return claim;
    }

    public Faction getFaction() {
        return faction;
    }

    public Map<EntityType, Integer> getSpawners() {
        return spawners;
    }

    public Map<BlockPos, Material> getSpecialBlocks() {
        return specialBlocks;
    }

    public boolean wasLoaded() {
        return wasLoaded;
    }

    @Override
    public void run() {
        for (int y = 0; y < 256; y++) {
            if (claim.isSectionEmpty(y >> 4)) {
                y += 15;
                continue;
            }

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Material blockType = Material.getMaterial(claim.getBlockTypeId(x, y, z));

                    if (blockType == null) {
                        continue;
                    }

                    BlockPos blockPos = new BlockPos(new ChunkPos(claim.getWorldName(), claim.getX(), claim.getZ()), x, y, z);

                    switch (blockType) {
                        case CHEST:
                        case TRAPPED_CHEST:
                            if (FactionsWealth.getInstance().shouldIncludeChestContent() && !hasAddedChest(blockPos, blockType)) {
                                chestBlockPositions.put(blockPos, blockType);
                                specialBlocks.put(blockPos, blockType);
                            }

                            break;
                        case MOB_SPAWNER:
                            specialBlocks.put(blockPos, blockType);
                            break;
                        default:
                            blocks.put(blockType, blocks.getOrDefault(blockType, 0) + 1);
                    }
                }
            }
        }

        complete = true;
        done();
    }

    private boolean hasAddedChest(BlockPos blockPos, Material blockType) {
        if (!chestBlockPositions.isEmpty()) {
            for (Map.Entry<BlockPos, Material> chestBlockPosEntry : chestBlockPositions.entrySet()) {
                BlockPos chestBlockPos = chestBlockPosEntry.getKey();
                Material chestBlockType = chestBlockPosEntry.getValue();

                if (blockType == chestBlockType && blockPos.getChunkPos().equals(chestBlockPos.getChunkPos())) {
                    if (blockPos.getY() == chestBlockPos.getY()) {
                        if ((blockPos.getX() == chestBlockPos.getX() && Math.abs(blockPos.getZ() - chestBlockPos.getZ()) == 1) || (Math.abs(blockPos.getX() - chestBlockPos.getX()) == 1 && blockPos.getZ() == chestBlockPos.getZ())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

}
