package mike.skyAddon.storage;

import gg.supervisor.core.annotation.Component;
import mike.mLibrary.config.MConfig;
import mike.skyAddon.SkyAddon;
import mike.skyAddon.component.KeeperSpawningComponent;

@Component
public class KeeperSpawnStorage extends MConfig<KeeperSpawningComponent> {

    public KeeperSpawnStorage() {
        super(SkyAddon.getPlugin(), "storage", "spawn-storage", new KeeperSpawningComponent());
    }
}
