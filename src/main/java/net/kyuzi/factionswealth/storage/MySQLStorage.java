package net.kyuzi.factionswealth.storage;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.exception.StorageFailureException;
import net.kyuzi.factionswealth.utility.BukkitUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLStorage extends Storage {

    private enum Table {

        BLOCK("Block", "`faction_id` TEXT NOT NULL, `material` TEXT NOT NULL, `amount` BIGINT NOT NULL DEFAULT 0"),
        CHEST("Chest", "`faction_id` TEXT NOT NULL, `value` FLOAT(10,2) NOT NULL DEFAULT 0"),
        SPAWNER("Spawner", "`faction_id` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` BIGINT NOT NULL DEFAULT 0");

        private String name;
        private String columns;

        Table(String name, String columns) {
            this.name = name;
            this.columns = columns;
        }

        private String getCreateQuery() {
            return "CREATE TABLE IF NOT EXISTS `" + name + "` (" + columns + ");";
        }

    }

    private Connection connection;
    private List<String> queries;

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public MySQLStorage(String host, int port, String database, String username, String password) throws StorageFailureException {
        super(StorageType.MYSQL);
        this.queries = new ArrayList<>();

        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;

        if (getConnection() == null) {
            throw new StorageFailureException("Invalid connection!");
        }

        createTables();
    }

    @Override
    public void load() {
        new Thread(() -> {
            List<ValuedFaction> valuedFactions = new ArrayList<>();

            queries.add(" ");

            try {
                PreparedStatement stmt = getConnection().prepareStatement("SELECT * FROM `" + Table.BLOCK.name + "`;");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String factionId = rs.getString("faction_id");
                    ValuedFaction valuedFaction = getValuedFaction(valuedFactions, factionId);

                    if (valuedFaction == null) {
                        valuedFaction = new ValuedFaction(factionId, new HashMap<>(), 0D, new HashMap<>());
                    } else {
                        valuedFactions.remove(valuedFaction);
                    }

                    Material material = Material.getMaterial(rs.getString("material"));

                    if (material == null) {
                        continue;
                    }

                    valuedFaction.getBlocks().put(material, rs.getInt("amount"));
                    valuedFactions.add(valuedFaction);
                }

                stmt = getConnection().prepareStatement("SELECT * FROM `" + Table.CHEST.name + "`;");
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String factionId = rs.getString("faction_id");
                    ValuedFaction valuedFaction = getValuedFaction(valuedFactions, factionId);

                    if (valuedFaction == null) {
                        valuedFaction = new ValuedFaction(factionId, new HashMap<>(), 0D, new HashMap<>());
                    } else {
                        valuedFactions.remove(valuedFaction);
                    }

                    valuedFaction.setChestValue(rs.getDouble("value"));
                    valuedFactions.add(valuedFaction);
                }

                stmt = getConnection().prepareStatement("SELECT * FROM `" + Table.SPAWNER.name + "`;");
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String factionId = rs.getString("faction_id");
                    ValuedFaction valuedFaction = getValuedFaction(valuedFactions, factionId);

                    if (valuedFaction == null) {
                        valuedFaction = new ValuedFaction(factionId, new HashMap<>(), 0D, new HashMap<>());
                    } else {
                        valuedFactions.remove(valuedFaction);
                    }

                    EntityType entityType = BukkitUtils.getEntityTypeFromString(rs.getString("type"));

                    if (entityType == null) {
                        continue;
                    }

                    valuedFaction.getSpawners().put(entityType, rs.getInt("amount"));
                    valuedFactions.add(valuedFaction);
                }

                setValuedFactions(valuedFactions);
            } catch (SQLException e) {
                FactionsWealth.getInstance().getLogger().severe("Error loading wealth data: " + e.getMessage());
            }

            queries.remove(" ");
            close(false);

            Bukkit.getScheduler().runTask(FactionsWealth.getInstance(), () -> {
                FactionsWealth.getInstance().completeEnable();
            });
        }).start();
    }

    @Override
    public void save() {
        new Thread(() -> {
            List<ValuedFaction> valuedFactions = getValuedFactions();

            queries.add(" ");

            try {
                getConnection().prepareStatement("DELETE FROM `" + Table.BLOCK.name + "`;").execute();
                getConnection().prepareStatement("DELETE FROM `" + Table.CHEST.name + "`;").execute();
                getConnection().prepareStatement("DELETE FROM `" + Table.SPAWNER.name + "`;").execute();

                if (!valuedFactions.isEmpty()) {
                    for (ValuedFaction valuedFaction : valuedFactions) {
                        Map<Material, Integer> blocks = new HashMap<>(valuedFaction.getBlocks());
                        Map<EntityType, Integer> spawners = new HashMap<>(valuedFaction.getSpawners());
                        StringBuilder queryBuilder = new StringBuilder("INSERT IGNORE INTO `").append(Table.BLOCK.name).append("` (`faction_id`, `material`, `amount`) VALUES");

                        if (!blocks.isEmpty()) {
                            for (int i = 0; i < blocks.size(); i++) {
                                queryBuilder.append(" (?, ?, ?),");
                            }

                            String query = queryBuilder.substring(0, queryBuilder.length() - 1) + ";";
                            PreparedStatement stmt = getConnection().prepareStatement(query);
                            int i = 1;

                            for (Map.Entry<Material, Integer> block : blocks.entrySet()) {
                                stmt.setString(i, valuedFaction.getFactionId());
                                stmt.setString(i + 1, block.getKey().name());
                                stmt.setInt(i + 2, block.getValue());

                                i += 3;
                            }

                            stmt.execute();
                        }

                        if (valuedFaction.getChestValue() > 0D) {
                            PreparedStatement stmt = getConnection().prepareStatement("INSERT INTO `"+ Table.CHEST.name + "` (`faction_id`, `value`) VALUES (?, ?);");

                            stmt.setString(1, valuedFaction.getFactionId());
                            stmt.setDouble(2, valuedFaction.getChestValue());
                            stmt.execute();
                        }

                        queryBuilder = new StringBuilder("INSERT IGNORE INTO `").append(Table.SPAWNER.name).append("` (`faction_id`, `type`, `amount`) VALUES");

                        if (!spawners.isEmpty()) {
                            for (int i = 0; i < spawners.size(); i++) {
                                queryBuilder.append(" (?, ?, ?),");
                            }

                            String query = queryBuilder.substring(0, queryBuilder.length() - 1) + ";";
                            PreparedStatement stmt = getConnection().prepareStatement(query);
                            int i = 1;

                            for (Map.Entry<EntityType, Integer> spawner : spawners.entrySet()) {
                                stmt.setString(i, valuedFaction.getFactionId());
                                stmt.setString(i + 1, spawner.getKey().name());
                                stmt.setInt(i + 2, spawner.getValue());

                                i += 3;
                            }

                            stmt.execute();
                        }
                    }
                }
            } catch (SQLException e) {
                FactionsWealth.getInstance().getLogger().severe("Error saving wealth data: " + e.getMessage());
            }

            queries.remove(" ");
            close(false);
        }).start();
    }

    private void close(boolean force) {
        if (connection != null && (force || queries.isEmpty())) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                FactionsWealth.getInstance().getLogger().severe("Error closing MySQL connection: " + e.getMessage());
            }
        }
    }

    private void createTables() {
        new Thread(() -> {
            queries.add(" ");

            for (Table table : Table.values()) {
                try {
                    getConnection().prepareStatement(table.getCreateQuery()).execute();
                } catch (SQLException e) {
                    FactionsWealth.getInstance().getLogger().severe("Error creating tables: " + e.getMessage());
                }
            }

            queries.remove(" ");
            close(true);
        }).start();
    }

    private Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            }
        } catch (SQLException e) {
            FactionsWealth.getInstance().getLogger().severe("Error opening MySQL connection: " + e.getMessage());
        }

        return connection;
    }

    private ValuedFaction getValuedFaction(List<ValuedFaction> factions, String factionId) {
        if (!factions.isEmpty()) {
            for (ValuedFaction faction : factions) {
                if (faction.getFactionId().equalsIgnoreCase(factionId)) {
                    return faction;
                }
            }
        }

        return null;
    }

}
