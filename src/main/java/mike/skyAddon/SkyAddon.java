package mike.skyAddon;

import co.aikar.commands.PaperCommandManager;
import gg.supervisor.core.loader.SupervisorLoader;
import gg.supervisor.core.util.Services;
import gg.supervisor.util.runnable.AbstractRunnable;
import lombok.Getter;
import mike.skyAddon.entity.Bosses;
import mike.skyAddon.service.CrystalService;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkyAddon extends JavaPlugin {

    @Getter
    public static SkyAddon plugin;

    @Override
    public void onEnable() {
        plugin = this;
        Services.register(Plugin.class, this);
        SupervisorLoader.register(this, new PaperCommandManager(this));
    }

    @Override
    public void onDisable() {
        for(Object abstractRunnable : Services.getRegisteredServices().values()) {
            if(abstractRunnable instanceof AbstractRunnable runnable) {
                runnable.cancel();
            }
        }
        Bosses.clearAll();
        Services.getService(CrystalService.class).resetCrystal();
    }
}
