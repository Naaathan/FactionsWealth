package net.kyuzi.factionswealth.storage;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.exception.StorageFailureException;
import net.kyuzi.factionswealth.utility.BukkitUtils;
import net.kyuzi.factionswealth.utility.FactionUtils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YAMLStorage extends Storage {

    private File file;

    public YAMLStorage(File file) throws StorageFailureException {
        super(StorageType.YAML);
        this.file = file;

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new StorageFailureException("Failed to create " + file.getName() + "!");
                }
            } catch (IOException e) {
                throw new StorageFailureException("Failed to create " + file.getName() + "!");
            }
        }
    }

    @Override
    public void load() {
        if (!file.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String id : config.getKeys(false)) {
            Faction faction = Factions.getInstance().getFactionById(id);

            if (FactionUtils.isValidFaction(faction)) {
                Map<Material, Integer> blocks = new HashMap<>();
                Map<EntityType, Integer> spawners = new HashMap<>();

                ConfigurationSection section = config.getConfigurationSection(id + ".blocks");

                if (section != null) {
                    for (String blockType : section.getKeys(false)) {
                        Material material = Material.getMaterial(blockType);

                        if (material != null) {
                            blocks.put(material, config.getInt(id + ".blocks." + blockType, 0));
                        }
                    }
                }

                section = config.getConfigurationSection(id + ".spawners");

                if (section != null) {
                    for (String entityType : section.getKeys(false)) {
                        EntityType type = BukkitUtils.getEntityTypeFromString(entityType);

                        if (type != null) {
                            spawners.put(type, config.getInt(id + ".spawners." + entityType, 0));
                        }
                    }
                }

                double chestValue = config.getDouble(id + ".chest", 0D);
                addValuedFaction(new ValuedFaction(id, blocks, chestValue, spawners));
            }
        }

        FactionsWealth.getInstance().completeEnable();
    }

    @Override
    public void save() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }

        if (!valuedFactions.isEmpty()) {
            for (ValuedFaction valuedFaction : valuedFactions) {
                Map<Material, Integer> blocks = valuedFaction.getBlocks();
                Map<EntityType, Integer> spawners = valuedFaction.getSpawners();

                if (!blocks.isEmpty()) {
                    for (Map.Entry<Material, Integer> block : blocks.entrySet()) {
                        config.set(valuedFaction.getFactionId() + ".blocks." + block.getKey().name(), block.getValue());
                    }
                }

                config.set(valuedFaction.getFactionId() + ".chest", valuedFaction.getChestValue());

                if (!spawners.isEmpty()) {
                    for (Map.Entry<EntityType, Integer> spawner : spawners.entrySet()) {
                        config.set(valuedFaction.getFactionId() + ".spawners." + spawner.getKey().name(), spawner.getValue());
                    }
                }
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            FactionsWealth.getInstance().getLogger().severe("Failed to save storage file: " + file.getName() + "!");
        }
    }

}
