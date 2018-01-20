package net.kyuzi.factionswealth.task.calculate;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.task.Task;

import org.bukkit.*;
import org.bukkit.entity.EntityType;

import java.util.*;

public class CalculateFactionTask extends Task {

    private Map<Material, Integer> blocks;
    private List<CalculateChunkTask> calculateChunkTasks;
    private double chestValue;
    private Faction faction;
    private Map<EntityType, Integer> spawners;

    public CalculateFactionTask(Faction faction) {
        super(true);
        this.blocks = new HashMap<>();
        this.calculateChunkTasks = new ArrayList<>();
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
            for (FLocation claim : claims) {
                CalculateChunkTask calculateChunkTask = new CalculateChunkTask(claim, faction) {

                    @Override
                    public void done() {
                        CalculateFactionTask.this.blocks.putAll(this.getBlocks());
                        CalculateFactionTask.this.chestValue += this.getChestValue();
                        CalculateFactionTask.this.spawners.putAll(this.getSpawners());
                    }

                };

                calculateChunkTask.start();
                calculateChunkTasks.add(calculateChunkTask);
            }

            while (true) {
                for (int i = 0; i < calculateChunkTasks.size(); i++) {
                    CalculateChunkTask task = calculateChunkTasks.get(i);

                    if (task.isComplete()) {
                        calculateChunkTasks.remove(i);
                        i--;
                    }
                }

                if (calculateChunkTasks.isEmpty()) {
                    break;
                }
            }
        }

        complete = true;
        done();
    }

}
