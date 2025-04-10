package mike.skyAddon.storage.gui;

import gg.supervisor.core.annotation.Component;
import mike.mLibrary.config.MConfig;
import mike.mLibrary.gui.GuiItem;
import mike.mLibrary.gui.GuiWrapper;
import mike.skyAddon.SkyAddon;

import java.util.HashMap;
import java.util.Map;

@Component
public class VaultGUIStorage extends MConfig<GuiWrapper> {

    public VaultGUIStorage() {
        super(SkyAddon.getPlugin(), "config", "vault-gui", new GuiWrapper());
        System.out.println("THISSSSSSS + " + this.getOrLoad(false).getGuiItems());
    }

    public Map<Integer, GuiItem> getItems() {
        return new HashMap<>(this.getItems());
    }

}
