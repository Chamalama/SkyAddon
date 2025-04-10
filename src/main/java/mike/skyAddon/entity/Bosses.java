package mike.skyAddon.entity;

import lombok.Getter;
import mike.skyAddon.SkyAddon;
import mike.skyAddon.entity.entities.DarkMage;
import mike.skyAddon.entity.entities.Executioner;
import mike.skyAddon.entity.entities.Runekeeper;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Getter
public enum Bosses {

    BANDIT_RUNE_KEEPER(Runekeeper.class, true),
    DARK_MAGE(DarkMage.class, false),
    EXECUTIONER(Executioner.class, false);

    private final Class<? extends LMSEntity> entityClass;
    private final boolean mobType;

    @Getter
    public static final Map<UUID, LMSEntity> idToLMSEntity = new HashMap<>();

    @Getter
    public static final NamespacedKey ENTITY_KEY = new NamespacedKey(SkyAddon.getPlugin(), "LMS_ENTITY");

    Bosses(Class<? extends LMSEntity> entityClass, boolean mobType) {
        this.entityClass = entityClass;
        this.mobType = mobType;
    }

    public static Bosses enumInst(LivingEntity livingEntity) {
        final PersistentDataContainer pdc = livingEntity.getPersistentDataContainer();
        if(!pdc.has(ENTITY_KEY)) return null;
        final String entityKey = pdc.get(ENTITY_KEY, PersistentDataType.STRING);
        if(entityKey == null) return null;
        Bosses bossType = null;
        switch (entityKey) {
            case "RUNE_KEEPER" -> bossType = BANDIT_RUNE_KEEPER;
            case "DARK_MAGE" -> bossType = DARK_MAGE;
            case "EXECUTIONER" -> bossType = EXECUTIONER;
        }
        return bossType;
    }

    public static LMSEntity fromCache(LivingEntity livingEntity) {
        final UUID id = livingEntity.getUniqueId();
        return idToLMSEntity.getOrDefault(id, null);
    }

    public static void clear(UUID uuid) {
        idToLMSEntity.remove(uuid);
    }

    public static void clearAll() {
        for(LMSEntity lmsEntity : idToLMSEntity.values()) {
            lmsEntity.livingEntity().remove(Entity.RemovalReason.DISCARDED);
        }
        idToLMSEntity.clear();
    }

    public static void clearAllMobs() {
        List<UUID> idToClear = new ArrayList<>();
        for(LMSEntity lmsEntity : idToLMSEntity.values()) {
            if(lmsEntity.livingEntity().getBukkitLivingEntity().hasMetadata("BOSS_TYPE")) continue;
            lmsEntity.livingEntity().remove(Entity.RemovalReason.DISCARDED);
            idToClear.add(lmsEntity.livingEntity().getBukkitLivingEntity().getUniqueId());
        }
        for(UUID id : idToClear) {
            idToLMSEntity.remove(id);
        }
    }

    public LivingEntity spawn(Location location) {
        try{

            final Constructor<? extends LMSEntity> lmsEntity = entityClass.getConstructor(Location.class);
            final LMSEntity entityToSpawn = lmsEntity.newInstance(location);
            if(location.getWorld() == null) return null;
            final CraftWorld level = ((CraftWorld) location.getWorld());
            final ServerLevel serverLevel = level.getHandle();

            if(entityToSpawn.livingEntity() == null) return null;

            serverLevel.addFreshEntityWithPassengers(entityToSpawn.livingEntity(), CreatureSpawnEvent.SpawnReason.CUSTOM);
            entityToSpawn.livingEntity().setPos(location.getX(), location.getY(), location.getZ());
            idToLMSEntity.put(entityToSpawn.livingEntity().getUUID(), entityToSpawn);
            entityToSpawn.spawn(location);

            entityToSpawn.livingEntity().getBukkitLivingEntity().setRemoveWhenFarAway(false);

            entityToSpawn.livingEntity().getBukkitLivingEntity().setMetadata("LMS_ENTITY", new FixedMetadataValue(SkyAddon.getPlugin(), ""));

            return entityToSpawn.livingEntity().getBukkitLivingEntity();

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
