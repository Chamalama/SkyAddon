package mike.skyAddon.storage.region;

import gg.supervisor.core.annotation.Component;
import mike.mLibrary.config.MConfig;
import mike.mLibrary.region.RegionWrapper;
import mike.skyAddon.SkyAddon;

@Component
public class VaultRegionStorage extends MConfig<RegionWrapper> {

    public VaultRegionStorage() {
        super(SkyAddon.getPlugin(), "storage", "vault-region", new RegionWrapper("", 0, 0, 0, 0, 0, 0));
    }
}
