package net.kyuzi.factionswealth.location;

public class ChunkPos {

    private String worldName;
    private int x;
    private int z;

    public ChunkPos(String worldName, int x, int z) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        ChunkPos chunkPos;

        if (obj instanceof ChunkPos) {
            chunkPos = (ChunkPos) obj;
        } else {
            return false;
        }

        return worldName.equals(chunkPos.worldName) && x == chunkPos.x && z == chunkPos.z;
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
