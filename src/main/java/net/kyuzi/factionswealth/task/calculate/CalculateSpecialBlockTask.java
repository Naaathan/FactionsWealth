package net.kyuzi.factionswealth.task.calculate;

import net.kyuzi.factionswealth.location.BlockPos;
import net.kyuzi.factionswealth.task.Task;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class CalculateSpecialBlockTask extends Task {

    private BlockPos blockPos;
    private Material blockType;
    private double chestValue;
    private EntityType spawnerType;

    public CalculateSpecialBlockTask(BlockPos blockPos, Material blockType) {
        super(false);
        this.blockPos = blockPos;
        this.blockType = blockType;
        this.chestValue = 0D;
        this.spawnerType = null;
    }

    @Override
    public void done() {
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public double getChestValue() {
        return chestValue;
    }

    public EntityType getSpawnerType() {
        return spawnerType;
    }

    @Override
    public void run() {
        switch (blockType) {
            case CHEST:
            case TRAPPED_CHEST:
                this.chestValue = blockPos.calculateChestValue();
                break;
            case MOB_SPAWNER:
                this.spawnerType = blockPos.findSpawnerType();
        }

        complete = true;
        done();
    }

}
