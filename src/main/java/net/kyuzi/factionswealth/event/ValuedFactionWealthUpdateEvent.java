package net.kyuzi.factionswealth.event;

import net.kyuzi.factionswealth.entity.ValuedFaction;
import net.kyuzi.factionswealth.task.update.WealthUpdate;

import org.bukkit.event.Cancellable;

public class ValuedFactionWealthUpdateEvent extends ValuedFactionEvent implements Cancellable {

    public enum UpdateType {

        BLOCK,
        CHEST,
        SPAWNER;

    }

    private UpdateType updateType;
    private WealthUpdate wealthUpdate;
    private boolean cancelled;

    public ValuedFactionWealthUpdateEvent(ValuedFaction valuedFaction, WealthUpdate wealthUpdate) {
        super(valuedFaction);

        if (wealthUpdate instanceof WealthUpdate.BlockUpdate) {
            this.updateType = UpdateType.BLOCK;
        } else if (wealthUpdate instanceof WealthUpdate.ChestUpdate) {
            this.updateType = UpdateType.CHEST;
        } else {
            this.updateType = UpdateType.SPAWNER;
        }

        this.wealthUpdate = wealthUpdate;
        this.cancelled = false;
    }

    public ValuedFaction getUpdatedValuedFaction() {
        return wealthUpdate.update();
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
