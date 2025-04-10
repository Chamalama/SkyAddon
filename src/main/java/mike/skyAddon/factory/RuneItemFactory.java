package mike.skyAddon.factory;

import gg.supervisor.core.annotation.Component;
import mike.mLibrary.config.MConfig;
import mike.mLibrary.item.ItemWrapper;
import mike.mLibrary.text.Chat;
import mike.skyAddon.SkyAddon;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Component
public class RuneItemFactory extends MConfig<ItemWrapper> {

    private final NamespacedKey runeKey;

    public RuneItemFactory() {
        super(SkyAddon.getPlugin(), "items", "rune-item", new ItemWrapper());
        this.runeKey = new NamespacedKey(SkyAddon.getPlugin(), "RUNE_ITEM");
    }

    public ItemStack getRune() {
        ItemWrapper wrapper = this.getOrLoad(false);
        ItemStack stack = new ItemStack(wrapper.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Chat.translate(wrapper.getName()));
        meta.lore(Chat.translate(wrapper.getLore()));
        meta.setCustomModelData(wrapper.getModelData());

        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(runeKey, PersistentDataType.STRING, "RUNE");

        stack.setItemMeta(meta);
        return stack;
    }

    public boolean isRune(ItemStack stack) {
        final ItemMeta meta = stack.getItemMeta();
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(runeKey);
    }

}
