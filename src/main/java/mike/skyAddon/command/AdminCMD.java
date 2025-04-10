package mike.skyAddon.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import gg.supervisor.core.annotation.Component;
import mike.mLibrary.gui.GuiItem;
import mike.mLibrary.item.LootItem;
import mike.mLibrary.region.RegionWrapper;
import mike.mLibrary.text.Message;
import mike.skyAddon.entity.Bosses;
import mike.skyAddon.factory.RuneItemFactory;
import mike.skyAddon.service.gui.VaultGUI;
import mike.skyAddon.storage.BossStorage;
import mike.skyAddon.storage.KeeperSpawnStorage;
import mike.skyAddon.storage.gui.VaultGUIStorage;
import mike.skyAddon.storage.region.VaultRegionStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Component
@CommandAlias("skyaddon")
@CommandPermission("admin.skyaddon.cmd")
public class AdminCMD extends BaseCommand {

    private final KeeperSpawnStorage keeperSpawnStorage;
    private final BossStorage bossStorage;
    private final VaultGUIStorage vaultGUIStorage;
    private final VaultRegionStorage vaultRegionStorage;

    private final RuneItemFactory runeItemFactory;

    private final VaultGUI vaultGUI;

    private final List<Location> regionLocations = new LinkedList<>();

    public AdminCMD(PaperCommandManager paperCommandManager, KeeperSpawnStorage keeperSpawnStorage, BossStorage bossStorage, VaultGUIStorage vaultGUIStorage, VaultRegionStorage vaultRegionStorage, RuneItemFactory runeItemFactory, VaultGUI vaultGUI) {
        this.keeperSpawnStorage = keeperSpawnStorage;
        this.bossStorage = bossStorage;
        this.vaultGUIStorage = vaultGUIStorage;
        this.vaultRegionStorage = vaultRegionStorage;
        this.runeItemFactory = runeItemFactory;
        this.vaultGUI = vaultGUI;
        paperCommandManager.registerCommand(this);
    }

    @Subcommand("reload")
    public void onReload(Player player) {
        Bosses.clearAllMobs();
        keeperSpawnStorage.updateConfig();
        runeItemFactory.updateConfig();
        vaultGUIStorage.updateConfig();
        reloadGUI();
        Message.message(player, "Reloaded SkyAddon configs!");
    }

    @Subcommand("resetboss")
    public void onResetBoss(Player player) {
        bossStorage.getOrLoad(false).setLastBossSpawned(null);
        bossStorage.update();
        Message.message(player, "Reset the current boss spawned!");
    }

    @Subcommand("vault")
    public void openVault(Player player) {
        vaultGUI.onOpen(player);
    }

    @Subcommand("vaultadd")
    @CommandCompletion("slot command")
    public void addItemToVault(Player player, @Single int slot, @Single String command) {
        final ItemStack stack = player.getInventory().getItemInMainHand().clone();

        if(stack.getType() == Material.AIR) {
            Message.urgent(player, "You cannot add air to the vault shop!");
            return;
        }

        GuiItem guiItem = new GuiItem();
        guiItem.setDisplayName(stack.getItemMeta().hasDisplayName() ? stack.getItemMeta().getDisplayName() : stack.getType().name());
        guiItem.setLore(stack.getItemMeta().hasLore() ? stack.getItemMeta().getLore() : new ArrayList<>());
        guiItem.setMaterial(stack.getType());

        guiItem.setSlot(slot);
        guiItem.setLootItem(new LootItem(stack, 1, 1, command));

        vaultGUIStorage.getOrLoad(false).getGuiItems().put(slot, guiItem);
        vaultGUIStorage.update();

        Message.message(player, "Added item to the vault shop!");
    }

    @Subcommand("region")
    @CommandCompletion("set")
    public void editRegion(Player player, String set) {
        if(set == null) return;
        final Location location = player.getLocation();

        regionLocations.add(location);

        Message.message(player, "Added region point at " + location.getBlockX() + "x, " + location.getBlockY() + "y, " + location.getBlockZ() + "z!");

        final int size = regionLocations.size();

        if(size == 2) {

            Message.message(player, "Vault region created!");

            final RegionWrapper createdRegion = new RegionWrapper(regionLocations.get(0), regionLocations.get(1));

            final RegionWrapper regionWrapper = vaultRegionStorage.getOrLoad(false);

            regionWrapper.setWorldName(player.getWorld().getName());
            regionWrapper.setMinX(createdRegion.getMinX());
            regionWrapper.setMaxX(createdRegion.getMaxX());
            regionWrapper.setMinY(createdRegion.getMinY());
            regionWrapper.setMaxY(createdRegion.getMaxY());
            regionWrapper.setMinZ(createdRegion.getMinZ());
            regionWrapper.setMaxZ(createdRegion.getMaxZ());

            vaultRegionStorage.update();

            regionLocations.clear();

        }else{

            Message.urgent(player, "Add another point to complete the region!");

        }

    }

    private void reloadGUI() {
        vaultGUI.clearItems();
        vaultGUI.getInventory().clear();
        vaultGUI.getGuiItems().putAll(vaultGUIStorage.getOrLoad(false).getGuiItems());
        vaultGUI.populateGUI();
    }

}
