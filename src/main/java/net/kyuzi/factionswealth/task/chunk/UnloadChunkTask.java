package net.kyuzi.factionswealth.task.chunk;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class UnloadChunkTask extends ChunkTask {

    public UnloadChunkTask(String worldName, int x, int z) {
        super(worldName, x, z);
    }

    @Override
    public void done() {
    }

    @Override
    public void run() {
        World world = Bukkit.getWorld(getWorldName());

        if (world != null) {
            if (world.isChunkLoaded(getX(), getZ())) {
                world.unloadChunk(getX(), getZ());
            }
        }

        complete = true;
        done();
    }

}
