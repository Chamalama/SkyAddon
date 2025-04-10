package mike.skyAddon.service;

import gg.supervisor.core.annotation.Component;
import lombok.Getter;
import mike.mLibrary.util.LocWrapper;
import mike.skyAddon.SkyAddon;
import mike.skyAddon.component.SpiritComponent;
import org.bukkit.Location;
import org.bukkit.entity.Allay;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Set;

@Getter
@Component
public class RuneAnimationService {

    private final Set<SpiritComponent> spiritAnimationSet = new HashSet<>();

    private final CrystalService crystalService;

    public RuneAnimationService(CrystalService crystalService) {
        this.crystalService = crystalService;
    }

    //This could be changed to packets to save on performance
    public void spawnSpirit(Location spawnLocation) {
        if(crystalService.isBossStarted()) return;
        final LocWrapper spawnLoc = new LocWrapper(spawnLocation.getWorld().getName(), spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), 0, 0);
        final LocWrapper endLocation = crystalService.getCrystalLocationStorage().getOrLoad(false);

        final Allay allay = (Allay) spawnLocation.getWorld().spawnEntity(spawnLocation.toCenterLocation(), EntityType.ALLAY);
        allay.setInvulnerable(true);
        allay.setCanDuplicate(false);
        allay.setCollidable(false);
        allay.setMetadata("SPIRIT", new FixedMetadataValue(SkyAddon.getPlugin(), true));

        allay.lookAt(endLocation.toBukkit().toCenterLocation().clone().add(0, 0.5, 0));

        final SpiritComponent spiritComponent = new SpiritComponent(spawnLoc, endLocation, allay.getUniqueId());

        spiritAnimationSet.add(spiritComponent);
    }

}
