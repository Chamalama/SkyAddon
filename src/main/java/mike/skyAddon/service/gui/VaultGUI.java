package mike.skyAddon.service.gui;

import gg.supervisor.core.annotation.Component;
import mike.mLibrary.gui.BaseGUI;
import mike.mLibrary.gui.GuiItem;
import mike.mLibrary.item.LootItem;
import mike.mLibrary.player.PlayerUtil;
import mike.mLibrary.text.Message;
import mike.skyAddon.SkyAddon;
import mike.skyAddon.factory.RuneItemFactory;
import mike.skyAddon.service.VaultService;
import mike.skyAddon.storage.gui.VaultGUIStorage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;

@Component
public class VaultGUI extends BaseGUI {

    private final VaultGUIStorage vaultGUIStorage;
    private final VaultService vaultService;
    private final RuneItemFactory runeItemFactory;

    public VaultGUI(VaultGUIStorage vaultGUIStorage, VaultService vaultService, RuneItemFactory runeItemFactory) {
        super(SkyAddon.getPlugin(), vaultGUIStorage.getOrLoad(false).getTitle(), vaultGUIStorage.getOrLoad(false).getSize(), new HashMap<>(vaultGUIStorage.getOrLoad(false).getGuiItems()),  false);
        this.vaultGUIStorage = vaultGUIStorage;
        this.vaultService = vaultService;
        this.runeItemFactory = runeItemFactory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if(!event.getView().getTitle().equalsIgnoreCase(vaultGUIStorage.getOrLoad(false).getTitle())) return;
        event.setCancelled(true);

        final Player player = (Player) event.getWhoClicked();

        if(event.getClickedInventory() == player.getInventory()) return;

        final int clickedSlot = event.getSlot();

        final int runeCount = vaultService.getRunes(player);

        if(runeCount <= 0) {
            Message.urgent(player, "Not enough runes!");
            return;
        }

        final GuiItem guiItem = this.getGuiItems().get(clickedSlot);

        if(guiItem == null) return;

        final LootItem lootItem = guiItem.getLootItem();

        if(lootItem == null) return;

        if(runeCount < guiItem.getPrice()) {
            Message.urgent(player, "Cannot afford this!");
            return;
        }

        Message.urgent(player, "-" + guiItem.getPrice() + " runes!");

        player.playSound(player, Sound.ENTITY_BAT_TAKEOFF, 1.0F, 1.0F);

        PlayerUtil.removeItem(player, runeItemFactory.getRune(), guiItem.getPrice());

        LootItem.giveLootItem(player, lootItem);

    }

    @Override
    public void onOpen(Player player) {

        player.openInventory(this.getInventory());

    }
}
