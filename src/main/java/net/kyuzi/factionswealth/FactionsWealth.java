package net.kyuzi.factionswealth;

import com.massivecraft.factions.P;

import net.kyuzi.factionswealth.command.factions.CmdRecalculate;
import net.kyuzi.factionswealth.command.factions.CmdRecalculateWealth;
import net.kyuzi.factionswealth.command.CmdReload;
import net.kyuzi.factionswealth.command.factions.CmdWealth;
import net.kyuzi.factionswealth.exception.HookFailureException;
import net.kyuzi.factionswealth.exception.StorageFailureException;
import net.kyuzi.factionswealth.hook.SilkSpawnersHook;
import net.kyuzi.factionswealth.listener.BasicListener;
import net.kyuzi.factionswealth.listener.SilkSpawnersListener;
import net.kyuzi.factionswealth.storage.MySQLStorage;
import net.kyuzi.factionswealth.storage.Storage;
import net.kyuzi.factionswealth.storage.StorageType;
import net.kyuzi.factionswealth.storage.YAMLStorage;
import net.kyuzi.factionswealth.task.calculate.CalculateTask;
import net.kyuzi.factionswealth.task.update.WealthUpdate;
import net.kyuzi.factionswealth.task.update.WealthUpdateTask;
import net.kyuzi.factionswealth.utility.BukkitUtils;
import net.kyuzi.factionswealth.utility.HoverUtils;
import net.kyuzi.factionswealth.utility.MessageUtils;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class FactionsWealth extends JavaPlugin {

    private static boolean hookedSilkSpawners = false;
    private static FactionsWealth instance = null;

    private Map<Material, Double> blocks;
    private CmdRecalculate cmdRecalculate;
    private CmdRecalculateWealth cmdRecalculateWealth;
    private CmdReload cmdReload;
    private CmdWealth cmdWealth;
    private int factionsPerPage;
    private boolean includeChestContent;
    private Map<Material, Double> items;
    private SilkSpawnersHook silkSpawnersHook;
    private Map<EntityType, Double> spawners;
    private CalculateTask recalculateTask;
    private Storage storage;
    private WealthUpdateTask wealthUpdateTask;

    public static boolean isHookedSilkSpawners() {
        return hookedSilkSpawners;
    }

    public static FactionsWealth getInstance() {
        return instance;
    }

    public Map<Material, Double> getBlocks() {
        return blocks;
    }

    public int getFactionsPerPage() {
        return factionsPerPage;
    }

    public boolean shouldIncludeChestContent() {
        return includeChestContent;
    }

    public Map<Material, Double> getItems() {
        return items;
    }

    public SilkSpawnersHook getSilkSpawnersHook() {
        return silkSpawnersHook;
    }

    public Map<EntityType, Double> getSpawners() {
        return spawners;
    }

    public CalculateTask getRecalculateTask() {
        return recalculateTask;
    }

    public void setRecalculateTask(CalculateTask recalculateTask) {
        if (recalculateTask != null) {
            if (recalculateTask.isRunning()) {
                if (!recalculateTask.isComplete()) {
                    recalculateTask.stop();
                }
            }
        }

        this.recalculateTask = recalculateTask;
    }

    public Storage getStorage() {
        return storage;
    }

    public WealthUpdateTask getWealthUpdateTask() {
        return wealthUpdateTask;
    }

    public void completeEnable() {
        getLogger().info("Initialised " + storage.getType().name() + " storage system!");
        getLogger().info("Adding wealth sub commands to Factions...");

        try {
            Class.forName("com.massivecraft.factions.P");
            P.p.cmdBase.addSubCommand((cmdRecalculate = new CmdRecalculate()));
            P.p.cmdBase.addSubCommand((cmdRecalculateWealth = new CmdRecalculateWealth()));
            P.p.cmdBase.addSubCommand((cmdWealth = new CmdWealth()));
            getLogger().info("Added wealth sub command to Factions!");
        } catch (ClassNotFoundException e) {
            getLogger().severe("Failed to add wealth sub command to Factions! Maybe your Factions version isn't supported?");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Registering commands...");
        getCommand("fwealthreload").setExecutor((cmdReload = new CmdReload()));
        getLogger().info("Registered commands!");
        getLogger().info("Registering listeners...");
        getServer().getPluginManager().registerEvents(new BasicListener(), this);
        getLogger().info("Registered listeners!");
        getLogger().info("Loading hovers...");

        if (HoverUtils.getInstance() == null) {
            getLogger().severe("Failed to load hovers!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Hovers loaded!");
        getLogger().info("Loading messages...");

        if (MessageUtils.getInstance() == null) {
            getLogger().severe("Failed to load messages!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Messages loaded!");
        getLogger().info("Plugin enabled!");
    }

    public void loadConfigData() {
        blocks = new HashMap<>();
        items = new HashMap<>();
        spawners = new HashMap<>();

        factionsPerPage = getConfig().getInt("factions_per_page", 10);
        includeChestContent = getConfig().getBoolean("include_chest_content", false);

        if (getConfig().getConfigurationSection("values.blocks") != null) {
            for (String materialString : getConfig().getConfigurationSection("values.blocks").getKeys(false)) {
                Material material = BukkitUtils.getMaterialFromString(materialString);

                if (material == null) {
                    continue;
                }

                blocks.put(material, getConfig().getDouble("values.blocks." + materialString));
            }
        }

        if (getConfig().getConfigurationSection("values.items") != null) {
            for (String materialString : getConfig().getConfigurationSection("values.items").getKeys(false)) {
                Material material = BukkitUtils.getMaterialFromString(materialString);

                if (material == null) {
                    continue;
                }

                items.put(material, getConfig().getDouble("values.items." + materialString));
            }
        }

        if (getConfig().getConfigurationSection("values.spawners") != null) {
            for (String entityType : getConfig().getConfigurationSection("values.spawners").getKeys(false)) {
                EntityType type = BukkitUtils.getEntityTypeFromString(entityType);

                if (type == null) {
                    continue;
                }

                spawners.put(type, getConfig().getDouble("values.spawners." + entityType));
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling plugin...");

        try {
            Class.forName("com.massivecraft.factions.P");
            P.p.cmdBase.subCommands.remove(cmdRecalculate);
            P.p.cmdBase.subCommands.remove(cmdRecalculateWealth);
            P.p.cmdBase.subCommands.remove(cmdWealth);
            getLogger().info("Disabled wealth sub command in Factions!");
        } catch (ClassNotFoundException ignored) {
        }

        if (wealthUpdateTask != null) {
            List<WealthUpdate> updates = wealthUpdateTask.getUpdates();

            if (!updates.isEmpty()) {
                for (WealthUpdate update : updates) {
                    FactionsWealth.getInstance().getStorage().addValuedFaction(update.update());
                }
            }
        }

        if (storage != null) {
            storage.save();
        }

        getLogger().info("Plugin disabled!");
    }

    @Override
    public void onEnable() {
        getLogger().info("Enabling plugin...");
        getLogger().info("Checking for soft/hard dependencies...");

        if (!getServer().getPluginManager().isPluginEnabled("Factions")) {
            getLogger().severe("Factions dependency not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if ((hookedSilkSpawners = getServer().getPluginManager().isPluginEnabled("SilkSpawners"))) {
            try {
                silkSpawnersHook = new SilkSpawnersHook();

                getServer().getPluginManager().registerEvents(new SilkSpawnersListener(), this);
                getLogger().info("SilkSpawners soft dependency found!");
            } catch (HookFailureException e) {
                hookedSilkSpawners = false;

                getLogger().warning("SilkSpawners soft dependency found but its instance was not!");
            }
        } else {
            getLogger().warning("SilkSpawners soft dependency not found, chest spawners may not work completely!");
        }

        getLogger().info("All dependencies found!");
        getLogger().info("Checking for configuration file...");

        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        if (configFile.exists()) {
            getLogger().info("Configuration file found!");
        } else {
            getLogger().severe("Error finding configuration file!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Loading configuration data...");
        loadConfigData();
        getLogger().info("Configuration data loaded!");

        StorageType storageType = StorageType.getStorageType(getConfig().getString("storage.type"));
        instance = this;
        recalculateTask = null;
        wealthUpdateTask = new WealthUpdateTask(0L, 20L);

        wealthUpdateTask.start();
        getLogger().info("Wealth update task enabled!");
        getLogger().info("Attempting to initialise " + storageType.name() + " storage system...");

        try {
            switch (storageType) {
                case MYSQL:
                    try {
                        storage =
                                new MySQLStorage(
                                        getConfig().getString("storage.mysql.host", "localhost"),
                                        getConfig().getInt("storage.mysql.port", 3306),
                                        getConfig().getString("storage.mysql.database", "factions_wealth"),
                                        getConfig().getString("storage.mysql.username", "root"),
                                        getConfig().getString("storage.mysql.password", "password")
                                );
                        break;
                    } catch (StorageFailureException e) {
                        if (storageType != StorageType.DEFAULT) {
                            getLogger().severe("Failed to initialise " + storageType.name() + " storage system, attempting " + StorageType.DEFAULT.name() + " storage system...");
                        }
                    }
                case YAML:
                default:
                    storage = new YAMLStorage(new File(getDataFolder(), getConfig().getString("storage.yaml.name", "wealth.yml")));
            }
        } catch (StorageFailureException e) {
            getLogger().severe("Failed to initialise " + storageType.name() + " storage system!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        storage.load();
        getLogger().info("Waiting for storage system to initialise...");
    }

}
