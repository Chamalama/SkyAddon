package mike.skyAddon.entity;

import mike.mLibrary.text.Chat;
import mike.skyAddon.SkyAddon;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public interface LMSEntity {

    String entityID();

    LivingEntity livingEntity();

    void onDealDamage(EntityDamageByEntityEvent event);

    void onTakeDamage(EntityDamageEvent event);

    void spawn(Location location);

    void tickLMS();

    void activate();

    default void onDeath(EntityDeathEvent event) {
        final UUID uuid = event.getEntity().getUniqueId();
        Bosses.clear(uuid);
        event.getEntity().removeMetadata("LMS_ENTITY", SkyAddon.getPlugin());
    }

    default void setup(String name) {
        org.bukkit.entity.LivingEntity le = livingEntity().getBukkitLivingEntity();
        le.getPersistentDataContainer().set(Bosses.getENTITY_KEY(), PersistentDataType.STRING, entityID());
        le.setCustomNameVisible(true);
        le.customName(Chat.translate(name));
    }

}
