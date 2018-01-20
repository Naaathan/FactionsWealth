package net.kyuzi.factionswealth.command.factions;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.task.calculate.CalculateFactionTask;
import net.kyuzi.factionswealth.task.calculate.CalculateTask;
import net.kyuzi.factionswealth.utility.MessageUtils;

import java.util.List;

public class CmdRecalculate extends FCommand {

    public CmdRecalculate() {
        super();
        this.aliases.add("recalculate");

        this.requiredArgs.add("faction tag");

        this.permission = "factions.recalculate";
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
        CalculateTask currentTask = FactionsWealth.getInstance().getRecalculateTask();

        if (currentTask != null && !currentTask.isComplete() && currentTask.isRunning()) {
            msg(MessageUtils.getInstance().TASK_CURRENTLY_RUNNING);
            return;
        }

        Faction faction = Factions.getInstance().getByTag(argAsString(0));

        if (faction == null) {
            msg(MessageUtils.getInstance().NO_FACTION);
        }

        CalculateFactionTask task = new CalculateFactionTask(faction) {

            @Override
            public void done() {
                super.done();
                msg(MessageUtils.getInstance().TASK_COMPLETE);
            }

        };

        task.start();
        msg(MessageUtils.getInstance().TASK_STARTED);
    }

}
