package net.kyuzi.factionswealth.task.calculate;

import com.massivecraft.factions.Faction;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.location.BlockPos;
import net.kyuzi.factionswealth.location.ChunkPos;
import net.kyuzi.factionswealth.task.Task;

import org.bukkit.*;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CalculateChunkTask extends Task {

    private Map<Material, Integer> blocks;
    private Map<BlockPos, Material> chestBlockPositions;
    private double chestValue;
    private ChunkSnapshot claim;
    private Faction faction;
    private Map<EntityType, Integer> spawners;

    public CalculateChunkTask(ChunkSnapshot claim, Faction faction) {
        super(true);
        this.blocks = new HashMap<>();
        this.chestBlockPositions = new HashMap<>();
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
        List<CalculateSpecialBlockTask> calculateSpecialBlockTasks = new ArrayList<>();

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
                                CalculateSpecialBlockTask calculateSpecialBlockTask = new CalculateSpecialBlockTask(blockPos, blockType) {

                                    @Override
                                    public void done() {
                                        CalculateChunkTask.this.chestValue += this.getChestValue();
                                    }

                                };

                                calculateSpecialBlockTask.start();
                                calculateSpecialBlockTasks.add(calculateSpecialBlockTask);
                                chestBlockPositions.put(blockPos, blockType);
                            }
                            break;
                        case MOB_SPAWNER:
                            CalculateSpecialBlockTask calculateSpecialBlockTask = new CalculateSpecialBlockTask(blockPos, blockType) {

                                @Override
                                public void done() {
                                    CalculateChunkTask.this.spawners.put(this.getSpawnerType(), CalculateChunkTask.this.spawners.getOrDefault(this.getSpawnerType(), 0) + 1);
                                }

                            };

                            calculateSpecialBlockTask.start();
                            calculateSpecialBlockTasks.add(calculateSpecialBlockTask);
                            break;
                        default:
                            blocks.put(blockType, blocks.getOrDefault(blockType, 0) + 1);
                    }
                }
            }
        }

        while (true) {
            for (int i = 0; i < calculateSpecialBlockTasks.size(); i++) {
                CalculateSpecialBlockTask task = calculateSpecialBlockTasks.get(i);

                if (task.isComplete()) {
                    calculateSpecialBlockTasks.remove(i);
                    i--;
                }
            }

            if (calculateSpecialBlockTasks.isEmpty()) {
                break;
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
