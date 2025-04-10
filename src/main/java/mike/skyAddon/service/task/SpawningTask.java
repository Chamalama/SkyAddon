package mike.skyAddon.service.task;

import gg.supervisor.core.annotation.Component;
import gg.supervisor.util.runnable.AbstractRunnable;
import mike.skyAddon.SkyAddon;
import mike.skyAddon.service.CrystalService;
import mike.skyAddon.service.LMSService;

@Component
public class SpawningTask extends AbstractRunnable {

    private final LMSService lmsService;
    private final CrystalService crystalService;

    public SpawningTask(LMSService lmsService, CrystalService crystalService) {
        super(SkyAddon.getPlugin(), 20, false);
        this.lmsService = lmsService;
        this.crystalService = crystalService;
    }

    @Override
    public void run() {
        lmsService.spawnKeepers();
        crystalService.spawnBoss();
    }
}
