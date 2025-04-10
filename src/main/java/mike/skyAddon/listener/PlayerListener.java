package mike.skyAddon.listener;

import gg.supervisor.core.annotation.Component;
import mike.skyAddon.service.VaultService;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Component
public class PlayerListener implements Listener {

    private final VaultService vaultService;

    public PlayerListener(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @EventHandler
    public void onDamageBoss(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity livingEntity) {
            if(!livingEntity.hasMetadata("BOSS_TYPE")) return;
            vaultService.incrementDamageDealt(player, event.getDamage());
        }
    }

}
