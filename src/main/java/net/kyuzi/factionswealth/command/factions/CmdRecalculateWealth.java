package net.kyuzi.factionswealth.command.factions;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.task.calculate.CalculateTask;
import net.kyuzi.factionswealth.utility.FactionUtils;
import net.kyuzi.factionswealth.utility.MessageUtils;

import java.util.List;

public class CmdRecalculateWealth extends FCommand {

    private enum Action {

        START("start"),
        STOP("stop");

        public static final Action DEFAULT = START;
        private String value;

        Action(String value) {
            this.value = value;
        }

        public static Action getAction(String value) {
            for (Action action : values()) {
                if (action.value.equalsIgnoreCase(value)) {
                    return action;
                }
            }

            return DEFAULT;
        }

    }

    public CmdRecalculateWealth() {
        super();
        this.aliases.add("recalculatewealth");

        this.optionalArgs.put("start/stop", "start");

        this.permission = "factions.recalculatewealth";
        this.disableOnLock = false;

        senderMustBePlayer = false;
        senderMustBeMember = false;
        senderMustBeModerator = false;
        senderMustBeAdmin = false;
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }

    @Override
    public void perform() {
        Action action = Action.getAction(argAsString(0, "start"));
        CalculateTask currentTask = FactionsWealth.getInstance().getRecalculateTask();

        switch (action) {
            case START:
                if (currentTask != null && !currentTask.isComplete() && currentTask.isRunning()) {
                    msg(MessageUtils.getInstance().TASK_CURRENTLY_RUNNING);
                    return;
                }

                List<Faction> factions = FactionUtils.getAllFactions();

                CalculateTask task = new CalculateTask(factions) {

                    @Override
                    public void done() {
                        FactionsWealth.getInstance().getStorage().save();
                        msg(MessageUtils.getInstance().TASK_COMPLETE);
                    }

                };

                msg(MessageUtils.getInstance().TASK_STARTED);
                FactionsWealth.getInstance().getStorage().recalculate(task);

                break;
            case STOP:
                if (currentTask == null || currentTask.isComplete() || !currentTask.isRunning()) {
                    msg(MessageUtils.getInstance().TASK_NOT_RUNNING);
                    return;
                }

                FactionsWealth.getInstance().setRecalculateTask(null);

                currentTask.stop();
                msg(MessageUtils.getInstance().TASK_STOPPED);

                break;
        }
    }

}
