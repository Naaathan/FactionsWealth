package net.kyuzi.factionswealth.task.update;

import com.massivecraft.factions.*;
import com.massivecraft.factions.struct.Relation;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.event.ValuedFactionWealthUpdateEvent;
import net.kyuzi.factionswealth.task.TimerTask;
import net.kyuzi.factionswealth.utility.FactionUtils;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class WealthUpdateTask extends TimerTask {

    private List<WealthUpdate> updates;

    public WealthUpdateTask(long delay, long period) {
        super(true, delay, period);
        this.updates = new ArrayList<>();
    }

    public List<WealthUpdate> getUpdates() {
        return Arrays.asList(updates.toArray(new WealthUpdate[updates.size()]));
    }

    public void clearUpdates() {
        updates.clear();
    }

    @Override
    public void run() {
        List<WealthUpdate> updates = getUpdates();

        if (!updates.isEmpty()) {
            for (WealthUpdate update : updates) {
                this.updates.remove(update);

                ValuedFactionWealthUpdateEvent valuedFactionWealthUpdateEvent = new ValuedFactionWealthUpdateEvent(update.getValuedFaction(), update);
                FactionsWealth.getInstance().getServer().getPluginManager().callEvent(valuedFactionWealthUpdateEvent);

                if (valuedFactionWealthUpdateEvent.isCancelled()) {
                    continue;
                }

                FactionsWealth.getInstance().getStorage().addValuedFaction(update.update());
            }
        }
    }

    public void updateWealth(Block block, Player player, WealthUpdate update) {
        FPlayer fPlayer = player != null ? FPlayers.getInstance().getByPlayer(player) : null;
        Faction otherFaction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        Faction playerFaction = fPlayer != null ? fPlayer.getFaction() : null;

        if (FactionUtils.isValidFaction(otherFaction) && (fPlayer == null || FactionUtils.isValidFaction(playerFaction)) && (fPlayer == null || fPlayer.isAdminBypassing() || otherFaction.getRelationTo(playerFaction) == Relation.MEMBER)) {
            ValuedFaction valuedFaction = FactionsWealth.getInstance().getStorage().getValuedFaction(otherFaction.getId());

            if (valuedFaction != null) {
                update.setFactionId(otherFaction.getId());
                updates.add(update);
            }
        }
    }

}
