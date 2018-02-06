package net.kyuzi.factionswealth.command.factions;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.shade.mkremins.fanciful.FancyMessage;
import com.massivecraft.factions.zcore.util.TL;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.task.calculate.CalculateTask;
import net.kyuzi.factionswealth.utility.BukkitUtils;
import net.kyuzi.factionswealth.utility.FactionUtils;
import net.kyuzi.factionswealth.utility.HoverUtils;
import net.kyuzi.factionswealth.utility.MessageUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CmdWealth extends FCommand {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

    public CmdWealth() {
        super();
        this.aliases.add("top");
        this.aliases.add("wealth");

        this.optionalArgs.put("page", "1");

        this.permission = "factions.wealth";
        this.disableOnLock = false;

        senderMustBePlayer = true;
        senderMustBeMember = false;
        senderMustBeModerator = false;
        senderMustBeAdmin = false;

        FORMAT.setGroupingSize(3);
        FORMAT.setGroupingUsed(true);
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }

    @Override
    public void perform() {
        List<ValuedFaction> valuedFactions = FactionsWealth.getInstance().getStorage().getValuedFactions();

        if (valuedFactions.isEmpty()) {
            List<Faction> factions = FactionUtils.getAllFactions();

            if (factions.isEmpty()) {
                msg(MessageUtils.getInstance().NO_FACTIONS);
                return;
            }

            CalculateTask currentTask = FactionsWealth.getInstance().getRecalculateTask();

            if (currentTask != null && !currentTask.isComplete() && currentTask.isRunning()) {
                msg(MessageUtils.getInstance().NOT_YET_CALCULATED);
                return;
            }

            CalculateTask task = new CalculateTask(factions) {

                @Override
                public void done() {
                    Bukkit.getScheduler().runTask(FactionsWealth.getInstance(), () -> {
                        Bukkit.dispatchCommand(sender, "f wealth");
                    });

                    FactionsWealth.getInstance().getStorage().save();
                }

            };

            FactionsWealth.getInstance().getStorage().recalculate(task);
            return;
        }

        int factionsPerPage = FactionsWealth.getInstance().getFactionsPerPage();
        int page = argAsInt(0, 1);
        int pages = Math.floorDiv(valuedFactions.size(), factionsPerPage) + 1;

        if (page > pages) {
            page = pages;
        } else if (page < 1) {
            page = 1;
        }

        String headerMessage = MessageUtils.getInstance().WEALTH_HEADER.replace("{current_page}", "" + page).replace("{final_page}", "" + pages);

        msg(MessageUtils.getInstance().WEALTH_HEADER_TITLEISE ? p.txt.titleize(headerMessage) : headerMessage);
        msg("");

        int max = factionsPerPage * page;

        if (max > valuedFactions.size()) {
            max = valuedFactions.size();
        }

        int start = max - factionsPerPage;

        if (start < 0) {
            start = 0;
        }

        for (int i = start; i < max; i++) {
            ValuedFaction valuedFaction = valuedFactions.get(i);
            Faction faction = Factions.getInstance().getFactionById(valuedFaction.getFactionId());

            if (FactionUtils.isValidFaction(faction)) {
                StringBuilder tooltip = new StringBuilder("");
                StringBuilder tooltipTemp = new StringBuilder("");

                for (String header : HoverUtils.getInstance().HEADER) {
                    if (!header.equals("")) {
                        tooltip.append(header.replace("{faction}", faction.getTag()));
                    } else {
                        tooltip.append("\n");
                    }
                }

                boolean includeNextBlock = false;

                for (String inner : HoverUtils.getInstance().BLOCKS_INNER) {
                    List<String> extractedPlaceholders = extractPlaceholders(inner);

                    if (!extractedPlaceholders.isEmpty()) {
                        for (String extractedPlaceholder : extractedPlaceholders) {
                            Material material = Material.getMaterial(extractedPlaceholder);

                            if (material == null) {
                                continue;
                            }

                            int amount = valuedFaction.getBlocks().getOrDefault(material, 0);

                            if (amount > 0 || HoverUtils.getInstance().DISPLAY_ZERO_VALUES) {
                                includeNextBlock = true;
                                tooltipTemp.append(inner.replace("{" + extractedPlaceholder + "}", "" + amount)).append("\n");
                            }
                        }
                    } else if (inner.equals("")) {
                        tooltipTemp.append("\n");
                    }
                }

                if (includeNextBlock) {
                    for (String header : HoverUtils.getInstance().BLOCKS_HEADER) {
                        if (!header.equals("")) {
                            tooltip.append(header);
                        } else {
                            tooltip.append("\n");
                        }
                    }

                    tooltip.append(tooltipTemp.toString());
                }

                includeNextBlock = false;
                tooltipTemp = new StringBuilder("");

                for (String inner : HoverUtils.getInstance().SPAWNERS_INNER) {
                    List<String> extractedPlaceholders = extractPlaceholders(inner);

                    if (!extractedPlaceholders.isEmpty()) {
                        for (String extractedPlaceholder : extractedPlaceholders) {
                            EntityType entityType = BukkitUtils.getEntityTypeFromString(extractedPlaceholder);

                            if (entityType == null) {
                                continue;
                            }

                            int amount = valuedFaction.getSpawners().getOrDefault(entityType, 0);

                            if (amount > 0 || HoverUtils.getInstance().DISPLAY_ZERO_VALUES) {
                                includeNextBlock = true;
                                tooltipTemp.append(inner.replace("{" + extractedPlaceholder + "}", "" + amount)).append("\n");
                            }
                        }
                    } else if (inner.equals("")) {
                        tooltipTemp.append("\n");
                    }
                }

                if (includeNextBlock) {
                    for (String header : HoverUtils.getInstance().SPAWNERS_HEADER) {
                        if (!header.equals("")) {
                            tooltip.append(header);
                        } else {
                            tooltip.append("\n");
                        }
                    }

                    tooltip.append(tooltipTemp.toString());
                }

                includeNextBlock = false;
                tooltipTemp = new StringBuilder("");

                for (String inner : HoverUtils.getInstance().VALUES_INNER) {
                    if (!inner.equals("")) {
                        if (inner.contains("{BLOCK_VALUE}") || inner.contains("{CHEST_VALUE}") || inner.contains("{SPAWNER_VALUE}") || inner.contains("{TOTAL_VALUE}")) {
                            includeNextBlock = true;
                            tooltipTemp.append(
                                    inner.replace("{BLOCK_VALUE}", FORMAT.format(valuedFaction.getBlocksValue()))
                                            .replace("{CHEST_VALUE}", FORMAT.format(valuedFaction.getChestValue()))
                                            .replace("{SPAWNER_VALUE}", FORMAT.format(valuedFaction.getSpawnersValue()))
                                            .replace("{TOTAL_VALUE}", FORMAT.format(valuedFaction.getTotalValue()))
                            ).append("\n");
                        }
                    } else {
                        tooltipTemp.append("\n");
                    }
                }

                if (includeNextBlock) {
                    for (String header : HoverUtils.getInstance().VALUES_HEADER) {
                        if (!header.equals("")) {
                            tooltip.append(header);
                        } else {
                            tooltip.append("\n");
                        }
                    }

                    tooltip.append(tooltipTemp.toString());
                }

                for (String footer : HoverUtils.getInstance().FOOTER) {
                    if (!footer.equals("")) {
                        tooltip.append(footer);
                    } else {
                        tooltip.append("\n");
                    }
                }

                FancyMessage message = new FancyMessage(
                        MessageUtils.getInstance().WEALTH_MESSAGE
                                .replace("{position}", "" + (i + 1))
                                .replace("{faction}", faction.getTag())
                                .replace("{wealth}", FORMAT.format(valuedFaction.getTotalValue()))
                );

                if (!tooltip.toString().equals("")) {
                    message.tooltip(tooltip.toString());
                }

                sendFancyMessage(message);
            }
        }

        String footerMessage = MessageUtils.getInstance().WEALTH_FOOTER.replace("{current_page}", "" + page).replace("{final_page}", "" + pages);

        msg("");
        msg(MessageUtils.getInstance().WEALTH_FOOTER_TITLEISE ? p.txt.titleize(footerMessage) : footerMessage);
    }

    private List<String> extractPlaceholders(String string) {
        List<String> extractedPlaceholders = new ArrayList<>();
        StringBuilder extracted = new StringBuilder("");
        boolean started = false;

        for (char c : string.toCharArray()) {
            if (!started && c == '{') {
                started = true;
                continue;
            }

            if (started) {
                if (c == '}') {
                    extractedPlaceholders.add(extracted.toString());

                    extracted = new StringBuilder("");
                    started = false;
                    continue;
                }

                extracted.append(c);
            }
        }

        return extractedPlaceholders;
    }

}
