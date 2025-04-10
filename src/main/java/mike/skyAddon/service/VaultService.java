package mike.skyAddon.service;

import gg.supervisor.core.annotation.Component;
import lombok.Getter;
import mike.skyAddon.factory.RuneItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
public class VaultService {

    private final RuneItemFactory runeItemFactory;

    private final ConcurrentHashMap<UUID, Double> damageMap = new ConcurrentHashMap<>();
    private final Set<UUID> authorizedPlayers = new HashSet<>();

    public VaultService(RuneItemFactory runeItemFactory) {
        this.runeItemFactory = runeItemFactory;
    }

    public void incrementDamageDealt(Player player, double damage) {
        if(damageMap.containsKey(player.getUniqueId())) {
            double currentDamage = damageMap.get(player.getUniqueId());
            currentDamage += damage;
            damageMap.put(player.getUniqueId(), currentDamage);
            return;
        }
        damageMap.putIfAbsent(player.getUniqueId(), damage);
    }

    public void sortLooters() {
        List<Map.Entry<UUID, Double>> sortedDamages = damageMap.entrySet().stream().sorted(Map.Entry.<UUID, Double>comparingByValue().reversed()).limit(5).toList();
        List<UUID> topDamagers = sortedDamages.stream().map(Map.Entry::getKey).toList();
        authorizedPlayers.addAll(topDamagers);
    }

    public double getPlayerDamage(Player player) {
        return damageMap.getOrDefault(player.getUniqueId(), 0.0);
    }

    public int getRunes(Player player) {
        int count = 0;
        for(ItemStack stack : player.getInventory()) {
            if(stack == null) continue;
            if(!runeItemFactory.isRune(stack)) continue;
            count += stack.getAmount();
        }
        return count;
    }

    public boolean hasRune(Player player) {
        for(ItemStack stack : player.getInventory().getContents()) {
            if(stack == null) continue;
            if(runeItemFactory.isRune(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean canLoot(Player player) {
        return authorizedPlayers.contains(player.getUniqueId());
    }

}
