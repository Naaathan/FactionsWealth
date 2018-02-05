package net.kyuzi.factionswealth.task.chunk;

import net.kyuzi.factionswealth.task.Task;

public abstract class ChunkTask extends Task {

    private String worldName;
    private int x;
    private int z;

    public ChunkTask(String worldName, int x, int z) {
        super(false);
        this.worldName = worldName;
        this.x = x;
        this.z = z;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

}
