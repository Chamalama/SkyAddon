package mike.skyAddon.entity.entities;

import com.google.common.collect.Sets;
import mike.mLibrary.entity.AttributeUtil;
import mike.mLibrary.entity.Target;
import mike.mLibrary.item.ItemBuilder;
import mike.skyAddon.entity.LMSEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Map;

public class Runekeeper extends Skeleton implements LMSEntity {

    private final HashSet<String> exclusions = Sets.newHashSet("LMS_ENTITY", "SPIRIT");

    private int ticks = 0;

    private long lastAttackTime = 0L;

    public Runekeeper(Location location) {
        super(EntityType.SKELETON, ((CraftWorld)location.getWorld()).getHandle());
    }

    @Override
    public String entityID() {
        return "RUNE_KEEPER";
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

    }

    @Override
    public void activate() {

    }

    @Override
    public void spawn(Location location) {
        setup(ChatColor.AQUA + ChatColor.BOLD.toString() + "Rune Keeper");
        this.goalSelector.getAvailableGoals().clear();
        this.targetSelector.getAvailableGoals().clear();

        if(this.getBukkitLivingEntity().getEquipment() == null) return;

        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.HEAD, ItemBuilder.buildSkull("https://textures.minecraft.net/texture/72df6a7989876778a7f5be1b886b6ac4baa90548eb7ce24b46acbc9afcdc1f9f"), false);
        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.CHEST, ItemBuilder.build(Material.GOLDEN_CHESTPLATE, true), false);
        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.LEGS, ItemBuilder.build(Material.GOLDEN_LEGGINGS, true), false);
        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.FEET, ItemBuilder.build(Material.GOLDEN_BOOTS, true), false);
        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.HAND, ItemBuilder.build(Material.GOLDEN_SWORD, true));


        AttributeUtil.setAttributes(this.getBukkitLivingEntity(), Map.of(
                Attribute.GENERIC_MOVEMENT_SPEED, 0.8,
                Attribute.GENERIC_MAX_HEALTH, 50.0,
                Attribute.GENERIC_ATTACK_DAMAGE, 3.0
        ));

    }

    @Override
    public void tickLMS() {

        ticks++;

        if(ticks % 3 != 0) return;

        final long currentTime = System.currentTimeMillis();

        final org.bukkit.entity.LivingEntity target = Target.findClosestEntity(this.getBukkitLivingEntity(), 9, exclusions);

        if(target == null) return;

        final double distance = this.getBukkitLivingEntity().getLocation().distanceSquared(target.getLocation());

        this.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + 0.5, target.getZ()));

        if(distance <= 4.5) {
            if(currentTime - lastAttackTime >= 1000L) {
                this.getBukkitLivingEntity().attack(target);
                this.getBukkitLivingEntity().swingMainHand();
                lastAttackTime = currentTime;
            }
            return;
        }

        this.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), this.getBukkitLivingEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());

    }
}
