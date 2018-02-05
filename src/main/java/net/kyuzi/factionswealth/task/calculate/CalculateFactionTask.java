package net.kyuzi.factionswealth.task.calculate;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.task.TimerTask;

import org.bukkit.*;
import org.bukkit.entity.EntityType;

import java.util.*;

public class CalculateFactionTask extends TimerTask {

    private Map<Material, Integer> blocks;
    private List<CalculateChunkTask> calculateChunkTasks;
    private double chestValue;
    private Faction faction;
    private boolean firstTick;
    private Map<EntityType, Integer> spawners;

    public CalculateFactionTask(Faction faction) {
        super(false, 0L, 20L);
        this.blocks = new HashMap<>();
        this.calculateChunkTasks = new ArrayList<>();
        this.chestValue = 0D;
        this.faction = faction;
        this.firstTick = true;
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
    public void done() {
        ValuedFaction valuedFaction = new ValuedFaction(faction.getId(), blocks, chestValue, spawners);

        FactionsWealth.getInstance().getStorage().addValuedFaction(valuedFaction);
    }

    @Override
    public void run() {
        if (firstTick) {
            Set<FLocation> claims = faction.getAllClaims();

            if (!claims.isEmpty()) {
                for (final FLocation claim : claims) {
                    boolean loaded;
                    World world = claim.getWorld();

                    if (!(loaded = world.isChunkLoaded((int) claim.getX(), (int) claim.getZ()))) {
                        world.loadChunk((int) claim.getX(), (int) claim.getZ());
                    }

                    Chunk chunk = world.getChunkAt((int) claim.getX(), (int) claim.getZ());
                    ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
                    CalculateChunkTask calculateChunkTask = new CalculateChunkTask(chunkSnapshot, faction, loaded) {

                        @Override
                        public void done() {
                            CalculateFactionTask.this.blocks.putAll(this.getBlocks());
                        }

                    };

                    calculateChunkTask.start();
                    calculateChunkTasks.add(calculateChunkTask);
                }
            }

            firstTick = false;
        }

        for (int i = 0; i < calculateChunkTasks.size(); i++) {
            CalculateChunkTask calculateChunkTask = calculateChunkTasks.get(i);

            if (calculateChunkTask.isComplete()) {
                calculateChunkTask.addSpecialBlocks();

                if (!calculateChunkTask.wasLoaded()) {
                    ChunkSnapshot claim = calculateChunkTask.getClaim();
                    World world = Bukkit.getWorld(claim.getWorldName());

                    if (world != null) {
                        world.getChunkAt(claim.getX(), claim.getZ()).unload();
                    }
                }

                calculateChunkTasks.remove(i);
                i--;
            }
        }

        if (calculateChunkTasks.isEmpty()) {
            complete = true;
            done();
            cancel();
        }
    }

}
