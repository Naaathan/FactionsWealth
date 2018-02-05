package net.kyuzi.factionswealth.command;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.task.calculate.CalculateTask;
import net.kyuzi.factionswealth.utility.HoverUtils;
import net.kyuzi.factionswealth.utility.MessageUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CmdReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("factionswealth.reload")) {
            sender.sendMessage(MessageUtils.getInstance().NO_PERMISSION);
            return true;
        }

        CalculateTask recalculateTask = FactionsWealth.getInstance().getRecalculateTask();

        if (recalculateTask != null && !recalculateTask.isComplete() && recalculateTask.isRunning()) {
            sender.sendMessage(MessageUtils.getInstance().TASK_CURRENTLY_RUNNING);
            return true;
        }

        FactionsWealth.getInstance().reloadConfig();
        FactionsWealth.getInstance().loadConfigData();

        FactionsWealth.getInstance().getLogger().info("Loading hovers...");

        if (HoverUtils.reload() == null) {
            FactionsWealth.getInstance().getLogger().severe("Failed to load hovers!");
            FactionsWealth.getInstance().getServer().getPluginManager().disablePlugin(FactionsWealth.getInstance());
            return true;
        }

        FactionsWealth.getInstance().getLogger().info("Hovers loaded!");
        FactionsWealth.getInstance().getLogger().info("Loading messages...");

        if (MessageUtils.reload() == null) {
            FactionsWealth.getInstance().getLogger().severe("Failed to load messages!");
            FactionsWealth.getInstance().getServer().getPluginManager().disablePlugin(FactionsWealth.getInstance());
            return true;
        }

        FactionsWealth.getInstance().getLogger().info("Messages loaded!");
        sender.sendMessage(MessageUtils.getInstance().RELOAD_SUCCESS);

        return true;
    }

}
