package mike.skyAddon.storage;

import gg.supervisor.core.annotation.Component;
import mike.mLibrary.config.MConfig;
import mike.mLibrary.util.LocWrapper;
import mike.skyAddon.SkyAddon;

@Component
public class CrystalLocationStorage extends MConfig<LocWrapper> {

    public CrystalLocationStorage() {
        super(SkyAddon.getPlugin(), "storage", "crystal-location", new LocWrapper("LMS", 0, 77, 0, 0, 0));
    }
}
