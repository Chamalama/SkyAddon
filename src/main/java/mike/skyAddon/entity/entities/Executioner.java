package mike.skyAddon.entity.entities;

import com.google.common.collect.Sets;
import mike.mLibrary.entity.Target;
import mike.mLibrary.item.ItemBuilder;
import mike.skyAddon.SkyAddon;
import mike.skyAddon.entity.LMSEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Set;

public class Executioner extends Skeleton implements LMSEntity {

    private int i = 0;
    private long lastAttackTime = 0;
    private boolean active = false;
    private static final Set<String> exclusions = Sets.newHashSet("BOSS_TYPE", "LMS_ENTITY");

    public Executioner(Location location) {
        super(EntityType.SKELETON, ((CraftWorld)location.getWorld()).getHandle());
    }

    @Override
    public String entityID() {
        return "EXECUTIONER";
    }

    @Override
    public LivingEntity livingEntity() {
        return this.getBukkitLivingEntity().getHandle();
    }

    @Override
    public void onDealDamage(EntityDamageByEntityEvent event) {

    }

    @Override
    public void onTakeDamage(EntityDamageEvent event) {
        if(event.getCause() == EntityDamageEvent.DamageCause.FALL) event.setCancelled(true);
    }

    @Override
    public void activate() {

    }

    @Override
    public void spawn(Location location) {
        setup(ChatColor.RED + ChatColor.BOLD.toString() + "Executioner");
        this.goalSelector.getAvailableGoals().clear();
        this.targetSelector.getAvailableGoals().clear();

        if(this.getBukkitLivingEntity().getEquipment() == null) return;

        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.HEAD, ItemBuilder.buildSkull("https://textures.minecraft.net/texture/29ac32d3bf2f5c0afec1aeff4e6967d370cb5f7052e5cef3c3dec2f80e6cf69d"));
        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.CHEST, ItemBuilder.build(Material.IRON_CHESTPLATE, true));
        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.LEGS, ItemBuilder.build(Material.IRON_LEGGINGS, true));
        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.FEET, ItemBuilder.build(Material.IRON_BOOTS, true));
        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.HAND, ItemBuilder.build(Material.IRON_AXE, true));
        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.OFF_HAND, ItemBuilder.build(Material.SHIELD, true));

        this.getBukkitLivingEntity().setMetadata("BOSS_TYPE", new FixedMetadataValue(SkyAddon.getPlugin(), ""));
    }

    @Override
    public void tickLMS() {
        if(!active) return;
        i++;
        if(i % 3 != 0) return;
        final long currentTime = System.currentTimeMillis();
        final org.bukkit.entity.LivingEntity executioner = this.getBukkitLivingEntity();
        final org.bukkit.entity.LivingEntity target = Target.findClosestEntity(executioner, 16, exclusions);
        final double distanceToTarget = executioner.getLocation().distanceSquared(target.getLocation());

        if(distanceToTarget > 4) {
            this.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), 0.7);
        }

        if(distanceToTarget <= 3) {
            if(currentTime - lastAttackTime >= 850) {
                executioner.attack(target);
                executioner.swingMainHand();
                lastAttackTime = currentTime;
            }
        }
    }
}
