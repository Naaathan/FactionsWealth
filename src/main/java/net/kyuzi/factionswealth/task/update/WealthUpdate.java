package net.kyuzi.factionswealth.task.update;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.utility.Operator;
import net.kyuzi.factionswealth.entity.ValuedFaction;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public abstract class WealthUpdate {

    public static class BlockUpdate extends WealthUpdate {

        private Material material;

        public BlockUpdate(Operator operator, Material material) {
            super(operator);
            this.material = material;
        }

        @Override
        public ValuedFaction update() {
            ValuedFaction valuedFaction = getValuedFaction();

            if (valuedFaction == null) {
                return null;
            }

            int amount = valuedFaction.getBlocks().getOrDefault(material, 0) + (operator == Operator.ADD ? 1 : -1);

            if (amount > 0) {
                valuedFaction.getBlocks().put(material, amount);
            } else {
                valuedFaction.getBlocks().remove(material);
            }

            return valuedFaction;
        }

    }

    public static class ChestUpdate extends WealthUpdate {

        private double value;

        public ChestUpdate(Operator operator, double value) {
            super(operator);
            this.value = value;
        }

        @Override
        public ValuedFaction update() {
            ValuedFaction valuedFaction = getValuedFaction();

            if (valuedFaction == null) {
                return null;
            }

            double value = valuedFaction.getChestValue() + (operator == Operator.ADD ? this.value : -this.value);

            if (value < 0) {
                value = 0D;
            }

            valuedFaction.setChestValue(value);
            return valuedFaction;
        }

    }

    public static class SpawnerUpdate extends WealthUpdate {

        private EntityType spawnerType;

        public SpawnerUpdate(Operator operator, EntityType spawnerType) {
            super(operator);
            this.spawnerType = spawnerType;
        }

        @Override
        public ValuedFaction update() {
            ValuedFaction valuedFaction = getValuedFaction();

            if (valuedFaction == null) {
                return null;
            }

            int amount = valuedFaction.getSpawners().getOrDefault(spawnerType, 0) + (operator == Operator.ADD ? 1 : -1);

            if (amount > 0) {
                valuedFaction.getSpawners().put(spawnerType, amount);
            } else {
                valuedFaction.getSpawners().remove(spawnerType);
            }

            return valuedFaction;
        }

    }

    protected String factionId;
    protected Operator operator;

    private WealthUpdate(Operator operator) {
        this.operator = operator;
    }

    public String getFactionId() {
        return factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

    public abstract ValuedFaction update();

    protected ValuedFaction getValuedFaction() {
        return FactionsWealth.getInstance().getStorage().getValuedFaction(factionId);
    }

}
