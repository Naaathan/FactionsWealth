package net.kyuzi.factionswealth.event;

import net.kyuzi.factionswealth.entity.ValuedFaction;

public class ValuedFactionEvent extends FactionsWealthEvent {

    private ValuedFaction valuedFaction;

    public ValuedFactionEvent(ValuedFaction valuedFaction) {
        this.valuedFaction = valuedFaction;
    }

    public ValuedFaction getValuedFaction() {
        return valuedFaction;
    }

}
