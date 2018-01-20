package net.kyuzi.factionswealth.utility;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import java.util.*;

public class FactionUtils {

    public static List<Faction> getAllFactions() {
        List<Faction> factions = Factions.getInstance().getAllFactions();

        factions.remove(Factions.getInstance().getSafeZone());
        factions.remove(Factions.getInstance().getWarZone());
        factions.remove(Factions.getInstance().getWilderness());

        return factions;
    }

    public static Map<String, Double> getOrderedFactions(Map<String, Double> factionsWealth) {
        List<Map.Entry<String, Double>> entries = new LinkedList<>(factionsWealth.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {

            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }

        });

        Map<String, Double> orderedFactionsWealth = new HashMap<>();

        for (Map.Entry<String, Double> entry : entries) {
            orderedFactionsWealth.put(entry.getKey(), entry.getValue());
        }

        return orderedFactionsWealth;
    }

    public static boolean isValidFaction(Faction faction) {
        return faction != null && !faction.isSafeZone() && !faction.isWarZone() && !faction.isWilderness();
    }

}
