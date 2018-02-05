package net.kyuzi.factionswealth.task.chunk;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class LoadChunkTask extends ChunkTask {

    private boolean loaded;

    public LoadChunkTask(String worldName, int x, int z) {
        super(worldName, x, z);
        this.loaded = false;
    }

    @Override
    public void done() {
    }

    @Override
    public void run() {
        World world = Bukkit.getWorld(getWorldName());

        if (world == null) {
            world = Bukkit.createWorld(new WorldCreator(getWorldName()));

            if (world == null) {
                done();
                return;
            }
        }

        if (loaded = !world.isChunkLoaded(getX(), getZ())) {
            world.loadChunk(getX(), getZ());
        }

        complete = true;
        done();
    }

    public boolean wasLoaded() {
        return loaded;
    }

}
