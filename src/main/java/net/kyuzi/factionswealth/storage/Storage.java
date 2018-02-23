package net.kyuzi.factionswealth.storage;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.task.calculate.CalculateTask;
import net.kyuzi.factionswealth.utility.FactionUtils;

import java.util.*;

public abstract class Storage {

    protected List<ValuedFaction> valuedFactions;
    protected StorageType type;

    protected Storage(StorageType type) {
        this.valuedFactions = new ArrayList<>();
        this.type = type;
    }

    public StorageType getType() {
        return type;
    }

    public ValuedFaction getValuedFaction(String id) {
        Faction faction = Factions.getInstance().getFactionById(id);

        if (FactionUtils.isValidFaction(faction)) {
            if (!valuedFactions.isEmpty()) {
                for (ValuedFaction valuedFaction : valuedFactions) {
                    if (valuedFaction.getFactionId().equals(id)) {
                        return valuedFaction;
                    }
                }
            }

            ValuedFaction valuedFaction = new ValuedFaction(id, new HashMap<>(), 0D, new HashMap<>());

            valuedFactions.add(valuedFaction);
            return valuedFaction;
        }

        return null;
    }

    public ValuedFaction getValuedFactionAt(int position) {
        if (position < 0 || valuedFactions.size() > position) {
            return null;
        }

        return valuedFactions.get(position);
    }

    public List<ValuedFaction> getValuedFactions() {
        return new ArrayList<>(valuedFactions);
    }

    public void addValuedFaction(ValuedFaction valuedFaction) {
        if (valuedFaction == null) {
            return;
        }

        int i;

        removeValuedFaction(valuedFaction.getFactionId());

        for (i = 0; i < valuedFactions.size(); i++) {
            if (valuedFactions.get(i).getTotalValue() < valuedFaction.getTotalValue()) {
                break;
            }
        }

        valuedFactions.add(i, valuedFaction);
    }

    public void removeValuedFaction(String id) {
        ValuedFaction valuedFaction = getValuedFaction(id);

        if (valuedFaction != null) {
            valuedFactions.remove(valuedFaction);
        }
    }

    public void setValuedFactions(List<ValuedFaction> valuedFactions) {
        this.valuedFactions = valuedFactions;
    }

    public abstract void load();

    public void recalculate(CalculateTask task) {
        if (FactionsWealth.getInstance().getRecalculateTask() != null && !FactionsWealth.getInstance().getRecalculateTask().isComplete()) {
            FactionsWealth.getInstance().getLogger().warning("Trying to start recalculate task whilst one is not complete!");
            return;
        }

        FactionsWealth.getInstance().setRecalculateTask(task);
        FactionsWealth.getInstance().getRecalculateTask().start();
    }

    public abstract void save();

}
