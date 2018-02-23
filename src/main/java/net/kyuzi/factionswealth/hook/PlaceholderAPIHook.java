package net.kyuzi.factionswealth.hook;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.exception.HookFailureException;

import net.kyuzi.factionswealth.utility.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class PlaceholderAPIHook {

    private final DecimalFormat FORMAT = new DecimalFormat("0.00");

    public PlaceholderAPIHook() throws HookFailureException {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
        } catch (ClassNotFoundException ignored) {
            throw new HookFailureException("PlaceholderAPI class not found!");
        }
    }

    public void registerPlaceholders() {
        PlaceholderAPI.registerPlaceholderHook(FactionsWealth.getInstance(), new PlaceholderHook() {

            @Override
            public String onPlaceholderRequest(Player player, String identifier) {
                if (identifier.toLowerCase().contains("factions_wealth_")) {
                    String positionString = identifier.replace("factions_wealth_", "");
                    int position;

                    try {
                        position = Integer.parseInt(positionString);
                    } catch (NumberFormatException e) {
                        return null;
                    }

                    ValuedFaction valuedFaction = FactionsWealth.getInstance().getStorage().getValuedFactionAt(position - 1);
                    Faction faction = valuedFaction != null ? Factions.getInstance().getFactionById(valuedFaction.getFactionId()) : null;

                    if (valuedFaction == null || faction == null) {
                        return MessageUtils.getInstance().UNKNOWN_PLACEHOLDER_MESSAGE
                                .replace("{position}", positionString)
                                .replace("{faction}", "Unknown")
                                .replace("{relation}", "")
                                .replace("{wealth}", "0.00");
                    }

                    ChatColor relationColour = null;

                    if (player != null) {
                        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

                        if (fPlayer != null) {
                            relationColour = fPlayer.getRelationTo(faction).getColor();
                        }
                    }

                    return MessageUtils.getInstance().PLACEHOLDER_MESSAGE
                            .replace("{position}", positionString)
                            .replace("{faction}", faction.getTag())
                            .replace("{relation}", relationColour != null ? relationColour + "" : "")
                            .replace("{wealth}", FORMAT.format(valuedFaction.getTotalValue()));
                }

                return null;
            }

        });
    }

    public void unregisterPlaceholders() {
        PlaceholderAPI.unregisterPlaceholderHook(FactionsWealth.getInstance());
    }

}
