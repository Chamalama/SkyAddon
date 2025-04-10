package mike.skyAddon.storage;

import gg.supervisor.core.annotation.Component;
import mike.mLibrary.config.MConfig;
import mike.skyAddon.SkyAddon;
import mike.skyAddon.component.BossComponent;

@Component
public class BossStorage extends MConfig<BossComponent> {

    public BossStorage() {
        super(SkyAddon.getPlugin(), "storage", "boss-storage", new BossComponent());
    }
}
