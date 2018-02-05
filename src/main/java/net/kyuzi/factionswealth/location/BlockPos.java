package net.kyuzi.factionswealth.location;

import net.kyuzi.factionswealth.utility.InventoryUtils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.EntityType;

public class BlockPos {

    private ChunkPos chunkPos;
    private int x;
    private int y;
    private int z;

    public BlockPos(ChunkPos chunkPos, int x, int y, int z) {
        this.chunkPos = chunkPos;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double calculateChestValue() {
        double value = 0D;
        World world = Bukkit.getWorld(chunkPos.getWorldName());

        if (world != null) {
            boolean loaded;

            if (!(loaded = world.isChunkLoaded(chunkPos.getX(), chunkPos.getZ()))) {
                world.loadChunk(chunkPos.getX(), chunkPos.getZ());
            }

            Chunk chunk = world.getChunkAt(chunkPos.getX(), chunkPos.getZ());
            Block block = chunk.getBlock(x, y, z);

            switch (block.getType()) {
                case CHEST:
                case TRAPPED_CHEST:
                    if (block.getState() instanceof Chest) {
                        value += InventoryUtils.calculateChestValue(((Chest) block.getState()).getBlockInventory().getContents());
                    } else if (block.getState() instanceof DoubleChest) {
                        value += InventoryUtils.calculateChestValue(((DoubleChest) block.getState()).getInventory().getContents());
                    }
            }

            if (!loaded) {
                world.unloadChunk(chunk);
            }
        }

        return value;
    }

    public EntityType findSpawnerType() {
        EntityType spawnerType = null;
        World world = Bukkit.getWorld(chunkPos.getWorldName());

        if (world != null) {
            boolean loaded;

            if (!(loaded = world.isChunkLoaded(chunkPos.getX(), chunkPos.getZ()))) {
                world.loadChunk(chunkPos.getX(), chunkPos.getZ());
            }

            Chunk chunk = world.getChunkAt(chunkPos.getX(), chunkPos.getZ());
            Block block = chunk.getBlock(x, y, z);

            if (block.getType() == Material.MOB_SPAWNER) {
                spawnerType = ((CreatureSpawner) block.getState()).getSpawnedType();
            }

            if (!loaded) {
                world.unloadChunk(chunk);
            }
        }

        return spawnerType;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

}
