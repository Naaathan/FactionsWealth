package net.kyuzi.factionswealth.entity;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.utility.Operator;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Map;

public class ValuedFaction {

    private Map<Material, Integer> blocks;
    private double chestValue;
    private String factionId;
    private Map<EntityType, Integer> spawners;

    public ValuedFaction(String factionId, Map<Material, Integer> blocks, double chestValue, Map<EntityType, Integer> spawners) {
        this.blocks = blocks;
        this.chestValue = chestValue;
        this.factionId = factionId;
        this.spawners = spawners;
    }

    public String getFactionId() {
        return factionId;
    }

    public Map<Material, Integer> getBlocks() {
        return blocks;
    }

    public double getBlocksValue() {
        double value = 0D;

        if (!blocks.isEmpty()) {
            for (Map.Entry<Material, Integer> block : blocks.entrySet()) {
                value += FactionsWealth.getInstance().getBlocks().getOrDefault(block.getKey(), 0D) * block.getValue();
            }
        }

        return value;
    }

    public double getChestValue() {
        return chestValue;
    }

    public void setChestValue(double chestValue) {
        this.chestValue = chestValue;
    }

    public Map<EntityType, Integer> getSpawners() {
        return spawners;
    }

    public double getSpawnersValue() {
        double value = 0D;

        if (!spawners.isEmpty()) {
            for (Map.Entry<EntityType, Integer> spawner : spawners.entrySet()) {
                value += FactionsWealth.getInstance().getSpawners().getOrDefault(spawner.getKey(), 0D) * spawner.getValue();
            }
        }

        return value;
    }

    public double getTotalValue() {
        return chestValue + getBlocksValue() + getSpawnersValue();
    }

    public void merge(Operator operator, Map<Material, Integer> blocks, double chestValue, Map<EntityType, Integer> spawners) {
        for (Map.Entry<Material, Integer> block : blocks.entrySet()) {
            int current = this.blocks.getOrDefault(block.getKey(), 0);
            int total = block.getValue() + (operator == Operator.ADD ? current : -current);

            if (total > 0) {
                this.blocks.put(block.getKey(), total);
            } else if (this.blocks.containsKey(block.getKey())) {
                this.blocks.remove(block.getKey());
            }
        }

        this.chestValue += (operator == Operator.ADD ? chestValue : -chestValue);

        for (Map.Entry<EntityType, Integer> spawner : spawners.entrySet()) {
            int current = this.spawners.getOrDefault(spawner.getKey(), 0);
            int total = spawner.getValue() + (operator == Operator.ADD ? current : -current);

            if (total > 0) {
                this.spawners.put(spawner.getKey(), total);
            } else if (this.spawners.containsKey(spawner.getKey())) {
                this.spawners.remove(spawner.getKey());
            }
        }
    }

    public void reset() {
        blocks.clear();
        chestValue = 0D;
        spawners.clear();
    }

}
