package net.kyuzi.factionswealth.utility;

import net.kyuzi.factionswealth.FactionsWealth;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HoverUtils {

    private static final boolean DEFAULT_DISPLAY_ZERO_VALUES = false;
    private static final String[] DEFAULT_HEADER = new String[]{"&b{faction}&e's Overview:", "", ""};
    private static final String[] DEFAULT_BLOCKS_HEADER = new String[]{"&b&lBLOCKS", ""};
    private static final String[] DEFAULT_BLOCKS_INNER = new String[]{"&eEmerald Block: &d{EMERALD_BLOCK}", ""};
    private static final String[] DEFAULT_SPAWNERS_HEADER = new String[]{"&b&lSPAWNERS", ""};
    private static final String[] DEFAULT_SPAWNERS_INNER = new String[]{"&eIron Golem: &d{IRON_GOLEM}", ""};
    private static final String[] DEFAULT_VALUES_HEADER = new String[]{"&b&lVALUES", ""};
    private static final String[] DEFAULT_VALUES_INNER = new String[]{"&eBlock Value: &d${BLOCK_VALUE}", "&eChest Value: &d${CHEST_VALUE}", "&eSpawner Value: &d${SPAWNER_VALUE}", "&eTotal Value: &d${TOTAL_VALUE}"};
    private static final String[] DEFAULT_FOOTER = new String[]{};

    private static HoverUtils instance;

    public final boolean DISPLAY_ZERO_VALUES;
    public final String[] HEADER;
    public final String[] BLOCKS_HEADER;
    public final String[] BLOCKS_INNER;
    public final String[] SPAWNERS_HEADER;
    public final String[] SPAWNERS_INNER;
    public final String[] VALUES_HEADER;
    public final String[] VALUES_INNER;
    public final String[] FOOTER;

    private HoverUtils() throws IOException {
        File file = new File(FactionsWealth.getInstance().getDataFolder(), "hover.yml");

        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException();
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> temp = new ArrayList<>();

        if (config.contains("display_zero_values")) {
            DISPLAY_ZERO_VALUES = config.getBoolean("display_zero_values");
        } else {
            config.set("display_zero_values", DEFAULT_DISPLAY_ZERO_VALUES);
            DISPLAY_ZERO_VALUES = DEFAULT_DISPLAY_ZERO_VALUES;
        }

        if (config.contains("header")) {
            for (String string : config.getStringList("header")) {
                temp.add(prepareMessage(string));
            }
        } else {
            config.set("header", DEFAULT_HEADER);

            for (String string : DEFAULT_HEADER) {
                temp.add(prepareMessage(string));
            }
        }

        HEADER = temp.toArray(new String[temp.size()]);
        temp = new ArrayList<>();

        if (config.contains("blocks_header")) {
            for (String string : config.getStringList("blocks_header")) {
                temp.add(prepareMessage(string));
            }
        } else {
            config.set("blocks_header", DEFAULT_BLOCKS_HEADER);

            for (String string : DEFAULT_BLOCKS_HEADER) {
                temp.add(prepareMessage(string));
            }
        }

        BLOCKS_HEADER = temp.toArray(new String[temp.size()]);
        temp = new ArrayList<>();

        if (config.contains("blocks_inner")) {
            for (String string : config.getStringList("blocks_inner")) {
                temp.add(prepareMessage(string));
            }
        } else {
            config.set("blocks_inner", DEFAULT_BLOCKS_INNER);

            for (String string : DEFAULT_BLOCKS_INNER) {
                temp.add(prepareMessage(string));
            }
        }

        BLOCKS_INNER = temp.toArray(new String[temp.size()]);
        temp = new ArrayList<>();

        if (config.contains("spawners_header")) {
            for (String string : config.getStringList("spawners_header")) {
                temp.add(prepareMessage(string));
            }
        } else {
            config.set("spawners_header", DEFAULT_SPAWNERS_HEADER);

            for (String string : DEFAULT_SPAWNERS_HEADER) {
                temp.add(prepareMessage(string));
            }
        }

        SPAWNERS_HEADER = temp.toArray(new String[temp.size()]);
        temp = new ArrayList<>();

        if (config.contains("spawners_inner")) {
            for (String string : config.getStringList("spawners_inner")) {
                temp.add(prepareMessage(string));
            }
        } else {
            config.set("spawners_inner", DEFAULT_SPAWNERS_INNER);

            for (String string : DEFAULT_SPAWNERS_INNER) {
                temp.add(prepareMessage(string));
            }
        }

        SPAWNERS_INNER = temp.toArray(new String[temp.size()]);
        temp = new ArrayList<>();

        if (config.contains("values_header")) {
            for (String string : config.getStringList("values_header")) {
                temp.add(prepareMessage(string));
            }
        } else {
            config.set("values_header", DEFAULT_VALUES_HEADER);

            for (String string : DEFAULT_VALUES_HEADER) {
                temp.add(prepareMessage(string));
            }
        }

        VALUES_HEADER = temp.toArray(new String[temp.size()]);
        temp = new ArrayList<>();

        if (config.contains("values_inner")) {
            for (String string : config.getStringList("values_inner")) {
                temp.add(prepareMessage(string));
            }
        } else {
            config.set("values_inner", DEFAULT_VALUES_INNER);

            for (String string : DEFAULT_VALUES_INNER) {
                temp.add(prepareMessage(string));
            }
        }

        VALUES_INNER = temp.toArray(new String[temp.size()]);
        temp = new ArrayList<>();

        if (config.contains("footer")) {
            for (String string : config.getStringList("footer")) {
                temp.add(prepareMessage(string));
            }
        } else {
            config.set("footer", DEFAULT_FOOTER);

            for (String string : DEFAULT_FOOTER) {
                temp.add(prepareMessage(string));
            }
        }

        FOOTER = temp.toArray(new String[temp.size()]);

        config.save(file);
    }

    public static HoverUtils getInstance() {
        if (instance == null) {
            try {
                instance = new HoverUtils();
            } catch (IOException e) {
                return null;
            }
        }

        return instance;
    }

    public static HoverUtils reload() {
        instance = null;
        return getInstance();
    }

    private String prepareMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
