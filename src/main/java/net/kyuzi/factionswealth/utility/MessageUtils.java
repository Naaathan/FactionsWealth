package net.kyuzi.factionswealth.utility;

import net.kyuzi.factionswealth.FactionsWealth;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MessageUtils {

    private static final String DEFAULT_PREFIX = "";
    private static final String DEFAULT_NO_FACTION = "&eThe faction you have entered doesn't exist!";
    private static final String DEFAULT_NO_FACTIONS = "&eNo factions exist!";
    private static final String DEFAULT_NO_PERMISSION = "&cYou do not have permission!";
    private static final String DEFAULT_NOT_YET_CALCULATED = "&eFaction wealth leaderboard has not yet been calculated!";
    private static final String DEFAULT_RELOAD_SUCCESS = "&aYou have reloaded the configuration file from disk!";
    private static final String DEFAULT_TASK_COMPLETE = "&aThe recalculation task is now complete!";
    private static final String DEFAULT_TASK_CURRENTLY_RUNNING = "&cThere is a recalculation task currently running!";
    private static final String DEFAULT_TASK_NOT_RUNNING = "&cThere isn't a recalculation task currently running!";
    private static final String DEFAULT_TASK_STARTED = "&eYou have started a recalculation task!";
    private static final String DEFAULT_TASK_STOPPED = "&eYou have stopped the recalculation task!";
    private static final String DEFAULT_WEALTH_FOOTER = "&ePage: &7{current_page}&8/&6{final_page}";
    private static final String DEFAULT_WEALTH_HEADER = "&eFactions Wealth Leaderboard";
    private static final String DEFAULT_WEALTH_MESSAGE = "&e{position}. &b{faction}&e: &d${wealth}";

    private static MessageUtils instance;

    public final String PREFIX;
    public final String NO_FACTION;
    public final String NO_FACTIONS;
    public final String NO_PERMISSION;
    public final String NOT_YET_CALCULATED;
    public final String RELOAD_SUCCESS;
    public final String TASK_COMPLETE;
    public final String TASK_CURRENTLY_RUNNING;
    public final String TASK_NOT_RUNNING;
    public final String TASK_STARTED;
    public final String TASK_STOPPED;
    public final String WEALTH_FOOTER;
    public final String WEALTH_HEADER;
    public final String WEALTH_MESSAGE;

    private MessageUtils() throws IOException {
        File file = new File(FactionsWealth.getInstance().getDataFolder(), "message.yml");

        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException();
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.contains("prefix")) {
            PREFIX = ChatColor.translateAlternateColorCodes('&', config.getString("prefix"));
        } else {
            config.set("prefix", DEFAULT_PREFIX);
            PREFIX = ChatColor.translateAlternateColorCodes('&', DEFAULT_PREFIX);
        }

        if (config.contains("no_faction")) {
            NO_FACTION = prepareMessage(config.getString("no_faction"));
        } else {
            config.set("no_faction", DEFAULT_NO_FACTION);
            NO_FACTION = prepareMessage(DEFAULT_NO_FACTION);
        }

        if (config.contains("no_factions")) {
            NO_FACTIONS = prepareMessage(config.getString("no_factions"));
        } else {
            config.set("no_factions", DEFAULT_NO_FACTIONS);
            NO_FACTIONS = prepareMessage(DEFAULT_NO_FACTIONS);
        }

        if (config.contains("no_permission")) {
            NO_PERMISSION = prepareMessage(config.getString("no_permission"));
        } else {
            config.set("no_permission", DEFAULT_NO_PERMISSION);
            NO_PERMISSION = prepareMessage(DEFAULT_NO_PERMISSION);
        }

        if (config.contains("not_yet_calculated")) {
            NOT_YET_CALCULATED = prepareMessage(config.getString("not_yet_calculated"));
        } else {
            config.set("not_yet_calculated", DEFAULT_NOT_YET_CALCULATED);
            NOT_YET_CALCULATED = prepareMessage(DEFAULT_NOT_YET_CALCULATED);
        }

        if (config.contains("reload_success")) {
            RELOAD_SUCCESS = prepareMessage(config.getString("reload_success"));
        } else {
            config.set("reload_success", DEFAULT_RELOAD_SUCCESS);
            RELOAD_SUCCESS = prepareMessage(DEFAULT_RELOAD_SUCCESS);
        }

        if (config.contains("task_complete")) {
            TASK_COMPLETE = prepareMessage(config.getString("task_complete"));
        } else {
            config.set("task_complete", DEFAULT_TASK_COMPLETE);
            TASK_COMPLETE = prepareMessage(DEFAULT_TASK_COMPLETE);
        }

        if (config.contains("task_currently_running")) {
            TASK_CURRENTLY_RUNNING = prepareMessage(config.getString("task_currently_running"));
        } else {
            config.set("task_currently_running", DEFAULT_TASK_CURRENTLY_RUNNING);
            TASK_CURRENTLY_RUNNING = prepareMessage(DEFAULT_TASK_CURRENTLY_RUNNING);
        }

        if (config.contains("task_not_running")) {
            TASK_NOT_RUNNING = prepareMessage(config.getString("task_not_running"));
        } else {
            config.set("task_not_running", DEFAULT_TASK_NOT_RUNNING);
            TASK_NOT_RUNNING = prepareMessage(DEFAULT_TASK_NOT_RUNNING);
        }

        if (config.contains("task_started")) {
            TASK_STARTED = prepareMessage(config.getString("task_started"));
        } else {
            config.set("task_started", DEFAULT_TASK_STARTED);
            TASK_STARTED = prepareMessage(DEFAULT_TASK_STARTED);
        }

        if (config.contains("task_stopped")) {
            TASK_STOPPED = prepareMessage(config.getString("task_stopped"));
        } else {
            config.set("task_stopped", DEFAULT_TASK_STOPPED);
            TASK_STOPPED = prepareMessage(DEFAULT_TASK_STOPPED);
        }

        if (config.contains("wealth_footer")) {
            WEALTH_FOOTER = prepareMessage(config.getString("wealth_footer"));
        } else {
            config.set("wealth_footer", DEFAULT_WEALTH_FOOTER);
            WEALTH_FOOTER = prepareMessage(DEFAULT_WEALTH_FOOTER);
        }

        if (config.contains("wealth_header")) {
            WEALTH_HEADER = prepareMessage(config.getString("wealth_header"));
        } else {
            config.set("wealth_header", DEFAULT_WEALTH_HEADER);
            WEALTH_HEADER = prepareMessage(DEFAULT_WEALTH_HEADER);
        }

        if (config.contains("wealth_message")) {
            WEALTH_MESSAGE = prepareMessage(config.getString("wealth_message"));
        } else {
            config.set("wealth_message", DEFAULT_WEALTH_MESSAGE);
            WEALTH_MESSAGE = prepareMessage(DEFAULT_WEALTH_MESSAGE);
        }

        config.save(file);
    }

    public static MessageUtils getInstance() {
        if (instance == null) {
            try {
                instance = new MessageUtils();
            } catch (IOException e) {
                return null;
            }
        }

        return instance;
    }

    private String prepareMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message.replace("{prefix}", PREFIX).replace("\\n", "\n"));
    }

}
