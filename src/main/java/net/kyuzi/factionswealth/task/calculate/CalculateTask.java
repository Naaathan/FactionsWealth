package net.kyuzi.factionswealth.task.calculate;

import com.massivecraft.factions.Faction;

import net.kyuzi.factionswealth.task.Task;
import net.kyuzi.factionswealth.utility.FactionUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class CalculateTask extends Task {

    private List<CalculateFactionTask> calculateFactionTasks;
    private List<Faction> factions;

    public CalculateTask(List<Faction> factions) {
        super(true);
        this.calculateFactionTasks = new ArrayList<>();
        this.factions = factions;
    }

    @Override
    public void run() {
        if (!factions.isEmpty()) {
            for (Faction faction : factions) {
                if (FactionUtils.isValidFaction(faction)) {
                    CalculateFactionTask calculateFactionTask = new CalculateFactionTask(faction);

                    calculateFactionTask.start();
                    calculateFactionTasks.add(calculateFactionTask);
                }
            }

            while (true) {
                for (int i = 0; i < calculateFactionTasks.size(); i++) {
                    CalculateFactionTask task = calculateFactionTasks.get(i);

                    if (task.isComplete()) {
                        calculateFactionTasks.remove(i);
                        i--;
                    }
                }

                if (calculateFactionTasks.isEmpty()) {
                    break;
                }
            }
        }

        complete = true;
        done();
    }

}
