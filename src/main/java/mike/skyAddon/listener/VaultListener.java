package mike.skyAddon.listener;

import gg.supervisor.core.annotation.Component;
import mike.mLibrary.util.LocationUtil;
import mike.skyAddon.service.VaultService;
import mike.skyAddon.storage.region.VaultRegionStorage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@Component
public class VaultListener implements Listener {

    private final VaultService vaultService;
    private final VaultRegionStorage vaultRegionStorage;

    public VaultListener(VaultService vaultService, VaultRegionStorage vaultRegionStorage) {
        this.vaultService = vaultService;
        this.vaultRegionStorage = vaultRegionStorage;
    }

    @EventHandler
    public void onTryEnterVault(PlayerMoveEvent event) {
        if(LocationUtil.isSameLocation(event.getFrom(), event.getTo())) return;
        if(!LocationUtil.isInRegion(event.getTo(), vaultRegionStorage.getOrLoad(false)))  return;
        final Player player = event.getPlayer();
        if(!vaultService.canLoot(player)) {
            event.setCancelled(true);
        }
    }

}
