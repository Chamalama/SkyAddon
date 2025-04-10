package mike.skyAddon.service;

import gg.supervisor.core.annotation.Component;
import lombok.Getter;
import mike.skyAddon.component.KeeperSpawningComponent;
import mike.skyAddon.component.SpawnLocationComponent;
import mike.skyAddon.entity.Bosses;
import mike.skyAddon.storage.KeeperSpawnStorage;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Component
public class LMSService {

    private final KeeperSpawnStorage spawnConfig;

    private final Map<UUID, SpawnLocationComponent> spawnedMobs = new HashMap<>();

    public LMSService(KeeperSpawnStorage spawnConfig) {
        this.spawnConfig = spawnConfig;
    }

    public void spawnKeepers() {

        final long currentTime = (System.currentTimeMillis() / 1000);

        final KeeperSpawningComponent keeperSpawningComponent = spawnConfig.getOrLoad(false);

        for(SpawnLocationComponent locationComponent : keeperSpawningComponent.getSpawnLocations()) {

            if(locationComponent.isSpawned()) continue;

            final long lastSpawnTime = (locationComponent.getLastSpawnTime() / 1000);

            if(currentTime - lastSpawnTime < keeperSpawningComponent.getSpawnTime()) continue;

            final LivingEntity runeKeeper = Bosses.BANDIT_RUNE_KEEPER.spawn(locationComponent.getLocWrapper().toBukkit().toCenterLocation());

            if(runeKeeper == null) continue;

            locationComponent.setSpawned(true);

            spawnedMobs.put(runeKeeper.getUniqueId(), locationComponent);

        }
    }

    public SpawnLocationComponent fromMob(LivingEntity livingEntity) {
        return spawnedMobs.getOrDefault(livingEntity.getUniqueId(), null);
    }

    public void clearMob(LivingEntity livingEntity) {
        spawnedMobs.remove(livingEntity.getUniqueId());
    }

}
