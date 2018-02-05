package net.kyuzi.factionswealth.task.calculate;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.task.Task;
import net.kyuzi.factionswealth.task.chunk.LoadChunkTask;
import net.kyuzi.factionswealth.task.chunk.UnloadChunkTask;

import org.bukkit.*;
import org.bukkit.entity.EntityType;

import java.util.*;

public class CalculateFactionTask extends Task {

    private Map<Material, Integer> blocks;
    private double chestValue;
    private Faction faction;
    private Map<EntityType, Integer> spawners;

    public CalculateFactionTask(Faction faction) {
        super(true);
        this.blocks = new HashMap<>();
        this.chestValue = 0D;
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
    public void done() {
        ValuedFaction valuedFaction = new ValuedFaction(faction.getId(), blocks, chestValue, spawners);

        FactionsWealth.getInstance().getStorage().addValuedFaction(valuedFaction);
    }

    @Override
    public void run() {
        Set<FLocation> claims = faction.getAllClaims();

        if (!claims.isEmpty()) {
            final List<CalculateChunkTask> calculateChunkTasks = new ArrayList<>();
            final List<LoadChunkTask> loadChunkTasks = new ArrayList<>();

            for (final FLocation claim : claims) {
                final World world = claim.getWorld();
                LoadChunkTask loadChunkTask = new LoadChunkTask(claim.getWorldName(), (int) claim.getX(), (int) claim.getZ()) {

                    @Override
                    public void done() {
                        final Chunk chunk = world.getChunkAt(getX(), getZ());
                        CalculateChunkTask calculateChunkTask = new CalculateChunkTask(chunk.getChunkSnapshot(), faction) {

                            @Override
                            public void done() {
                                if (wasLoaded()) {
                                    new UnloadChunkTask(claim.getWorldName(), getX(), getZ()).start();
                                }

                                CalculateFactionTask.this.blocks.putAll(this.getBlocks());
                                CalculateFactionTask.this.chestValue += this.getChestValue();
                                CalculateFactionTask.this.spawners.putAll(this.getSpawners());
                            }

                        };

                        calculateChunkTask.start();
                        calculateChunkTasks.add(calculateChunkTask);
                    }

                };

                loadChunkTask.start();
                loadChunkTasks.add(loadChunkTask);
            }

            while (true) {
                for (int i = 0; i < loadChunkTasks.size(); i++) {
                    LoadChunkTask task = loadChunkTasks.get(i);

                    if (task.isComplete()) {
                        loadChunkTasks.remove(i);
                        i--;
                    }
                }

                for (int i = 0; i < calculateChunkTasks.size(); i++) {
                    CalculateChunkTask task = calculateChunkTasks.get(i);

                    if (task.isComplete()) {
                        calculateChunkTasks.remove(i);
                        i--;
                    }
                }

                if (loadChunkTasks.isEmpty() && calculateChunkTasks.isEmpty()) {
                    break;
                }
            }
        }

        complete = true;
        done();
    }

}
