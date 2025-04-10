package mike.skyAddon.entity.entities;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mike.mLibrary.entity.AttributeUtil;
import mike.mLibrary.entity.Target;
import mike.mLibrary.item.ItemBuilder;
import mike.mLibrary.util.LocWrapper;
import mike.mLibrary.util.LocationUtil;
import mike.mLibrary.util.ParticleUtil;
import mike.skyAddon.SkyAddon;
import mike.skyAddon.entity.LMSEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.*;

public class DarkMage extends Stray implements LMSEntity {

    private int i, activationTimer, attackCount = 0;
    private long lastTeleportTime, lastAttackTime = 0;
    private long attackDelay = 1250;
    private boolean active = false;

    private final Random random = new Random();

    private BukkitTask activationTask;
    private Phase magePhase = Phase.ONE;

    private static final Set<String> exclusions = Sets.newHashSet("BOSS_TYPE", "LMS_ENTITY");
    private final Set<Meteor> meteors = new HashSet<>();
    private final Set<MeteorFloorBlock> blocksChanged = new HashSet<>();

    public DarkMage(Location location) {
        super(EntityType.STRAY, ((CraftWorld)location.getWorld()).getHandle());
    }

    @Override
    public String entityID() {
        return "DARK_MAGE";
    }

    @Override
    public LivingEntity livingEntity() {
        return this.getBukkitLivingEntity().getHandle();
    }

    @Override
    public void onDealDamage(EntityDamageByEntityEvent event) {
        event.setDamage(event.getDamage() * 1.25);
    }

    @Override
    public void onTakeDamage(EntityDamageEvent event) {
        if(event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) event.setCancelled(true);
    }

    @Override
    public void onDeath(EntityDeathEvent event) {
        LMSEntity.super.onDeath(event);
        active = false;
        i = 0;

        for(MeteorFloorBlock meteorFloorBlock : blocksChanged) {
            final Location loc = meteorFloorBlock.getLocation().toBukkit();
            loc.getBlock().setType(meteorFloorBlock.getOldMaterial(), false);
        }

        meteors.forEach(meteor -> {
            final Entity entity = Bukkit.getEntity(meteor.getDisplayID());
            if(entity != null) {
                entity.remove();
            }
        });

        meteors.clear();
        blocksChanged.clear();
    }

    @Override
    public void activate() {

        this.setNoGravity(true);

        final Set<UUID> toClear = new HashSet<>();

        this.activationTask = Bukkit.getScheduler().runTaskTimer(SkyAddon.getPlugin(), () -> {

            this.activationTimer++;

            for(Entity entity : this.getBukkitLivingEntity().getNearbyEntities(32, 32, 32)) {

                if(!(entity instanceof Player player) || toClear.contains(player.getUniqueId())) continue;
                final Location playerLocation = player.getLocation();
                final Location mageLocation = this.getBukkitLivingEntity().getLocation();

                final Vector dir = mageLocation.toVector().subtract(playerLocation.toVector()).normalize().multiply(0.4);

                player.setVelocity(dir);

                if(activationTimer % 5 == 0) {
                    player.playSound(player, Sound.ITEM_FIRECHARGE_USE, 1.0F, 1.2F);
                }

                if(playerLocation.distanceSquared(mageLocation) <= 5.0) {

                    toClear.add(player.getUniqueId());
                    this.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()));
                    this.getBukkitLivingEntity().swingMainHand();

                    Bukkit.getScheduler().runTaskLater(SkyAddon.getPlugin(), () -> {
                        player.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                        playerLocation.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, playerLocation, 10);
                        player.setVelocity(mageLocation.toVector().subtract(playerLocation.toVector()).normalize().multiply(-1.5));
                        player.damage(5.0);
                    }, 5L);

                }

            }

            if(this.activationTimer >= 80) {
                this.activationTask.cancel();
                this.active = true;
                this.setNoGravity(false);
                toClear.clear();
            }

        }, 1L, 1L);

    }

    @Override
    public void spawn(Location location) {
        setup(ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Dark Mage");
        this.goalSelector.getAvailableGoals().clear();
        this.targetSelector.getAvailableGoals().clear();
        this.getBukkitLivingEntity().setCollidable(false);
        this.getBukkitLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
        this.getBukkitLivingEntity().setMetadata("BOSS_TYPE", new FixedMetadataValue(SkyAddon.getPlugin(), ""));

        if(this.getBukkitLivingEntity().getEquipment() == null) return;

        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.HEAD, ItemBuilder.buildSkull("https://textures.minecraft.net/texture/e332808affd671a69f82beb7f08320d44f4cb3554619cc673eb683266991f40f"));
        this.getBukkitLivingEntity().getEquipment().setItem(EquipmentSlot.HAND, ItemBuilder.build(Material.STICK));

        AttributeUtil.setAttributes(this.getBukkitLivingEntity(), Map.of(
                Attribute.GENERIC_MOVEMENT_SPEED, 0.85,
                Attribute.GENERIC_ATTACK_DAMAGE, 8.0,
                Attribute.GENERIC_MAX_HEALTH, 1500.0
        ));
    }

    @Override
    public void tickLMS() {
        runMeteors();
        if(!this.active) return;
        i++;
        if(i % 3 != 0) return;
        final org.bukkit.entity.LivingEntity mage = this.getBukkitLivingEntity();
        org.bukkit.entity.LivingEntity mainTarget = Target.findClosestEntity(this.getBukkitLivingEntity(), 16, exclusions);
        if(mainTarget == null) return;
        final double distance = mage.getLocation().distanceSquared(mainTarget.getLocation());

        this.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(mainTarget.getX(), mainTarget.getY() + 0.5, mainTarget.getZ()));

        basicAttack(mage, mainTarget);
        fireBallAbility(mage, mainTarget);
        lightningStormAbility(mage);
        trySwapPhase(mage);

        lightningLineAbility(mage, mainTarget);

        if(distance <= 8 && canTeleport()) {
            teleportAbility();
        }

    }

    private void basicAttack(org.bukkit.entity.LivingEntity mage, org.bukkit.entity.LivingEntity mainTarget) {
        final long currentTime = System.currentTimeMillis();
        if(currentTime - lastAttackTime >= attackDelay) {
            attackCount++;
            mage.swingMainHand();
            ParticleUtil.drawParticle(mage.getLocation().clone().add(0, 1.25, 0), mainTarget.getEyeLocation(), Particle.ENCHANTMENT_TABLE, 2);
            mainTarget.damage(6);
            if(mainTarget instanceof Player player) {
                player.playSound(player, Sound.ENTITY_WITHER_HURT, 1.0F, 0.8F);
            }
            lastAttackTime = currentTime;
        }
    }

    private void lightningLineAbility(org.bukkit.entity.LivingEntity mage, org.bukkit.entity.LivingEntity target) {
        double random = Math.random();
        double chance = 0.01 * magePhase.getPhase();

        if(random > chance) return;

        Location startLocation = mage.getLocation();
        final Location targetLocation = target.getLocation();

        final Vector dir = targetLocation.toVector().subtract(startLocation.toVector()).normalize();

        for(int i = 0; i <= 10; i++) {

            Location strikeLocation = startLocation.clone().add(dir.clone().multiply(i+2));
            strikeLocation.getWorld().strikeLightningEffect(strikeLocation);
            strikeLocation.getWorld().playSound(strikeLocation, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 0.8F);

            strikeLocation.getNearbyEntities(3, 3, 3).forEach(entity -> {
                if(!(entity instanceof Player player)) return;
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
                player.damage(3);
            });

        }

    }

    private void lightningStormAbility(org.bukkit.entity.LivingEntity mage) {
        final long currentTime = System.currentTimeMillis();

        if (mage.hasMetadata("CD") && currentTime - mage.getMetadata("CD").get(0).asLong() < 30000) return;

        mage.removeMetadata("CD", SkyAddon.getPlugin());

        double random = Math.random();
        double chance = 0.009 * magePhase.getPhase();

        if (random > chance) return;

        final Set<LocWrapper> locations = new HashSet<>();

        for (int k = 0; k <= 16 + magePhase.getPhase(); k++) {
            final Location toAdd = LocationUtil.selectRandomNearbyLocation(mage.getLocation().toCenterLocation(), 10 - magePhase.getPhase());
            final LocWrapper wrapper = new LocWrapper(toAdd.getWorld().getName(), toAdd.getX(), toAdd.getY(), toAdd.getZ(), 0, 0);
            locations.add(wrapper);
        }

        for (LocWrapper locWrapper : locations) {
            final Location bukkitLocation = locWrapper.toBukkit();
            mage.getWorld().playSound(bukkitLocation, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 0.8F);
            mage.getWorld().strikeLightningEffect(bukkitLocation);
            mage.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, bukkitLocation.toCenterLocation(), 3);
            bukkitLocation.getNearbyEntities(3, 3, 3).forEach(entity -> {
                if (!(entity instanceof Player player)) return;
                player.damage(10);
            });
        }

        mage.setMetadata("CD", new FixedMetadataValue(SkyAddon.getPlugin(), System.currentTimeMillis()));

    }

    private void fireBallAbility(org.bukkit.entity.LivingEntity mage, org.bukkit.entity.LivingEntity target) {

        if(attackCount % (8 - magePhase.getPhase()) != 0) return;

        final World world = mage.getWorld();

        final double dirX = mage.getLocation().getDirection().getX();
        final double dirZ = mage.getLocation().getDirection().getZ();

        double randomX = (0.25 * Math.random()) - (0.25 * Math.random());
        double randomZ = (0.25 * Math.random()) - (0.25 * Math.random());

        final Fireball fireBall = (Fireball) world.spawnEntity(mage.getEyeLocation().clone().add(dirX + randomX, -0.5, dirZ + randomZ), org.bukkit.entity.EntityType.FIREBALL);
        fireBall.setInvulnerable(true);
        fireBall.setNoPhysics(true);
        fireBall.setVelocity(new Vector());
        fireBall.setPower(new Vector());

        mage.setMetadata("FIREBALL", new FixedMetadataValue(SkyAddon.getPlugin(), ""));

        final Location endLocation = target.getLocation().clone().add(0, 0.0, 0);
        final Location startLocation = mage.getLocation().clone().add(0, 0.0, 0);

        final Vector dir = endLocation.toVector().subtract(startLocation.toVector()).normalize().multiply(0.5);

        fireBall.setDirection(dir);

        for(Entity entity : fireBall.getNearbyEntities(3, 3, 3)) {
            if(!(entity instanceof Player player)) continue;
            player.playSound(player, Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
            player.damage(10);
        }

    }

    private void teleportAbility() {
        final org.bukkit.entity.LivingEntity mage = this.getBukkitLivingEntity();
        lastTeleportTime = System.currentTimeMillis();
        mage.getWorld().strikeLightningEffect(mage.getLocation());
        mage.getWorld().playSound(mage.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 2.0F);

        final int increase = magePhase.getPhase();

        for(Entity ent : mage.getNearbyEntities(3 + increase, 3 + increase, 3 + increase)) {
            if(!(ent instanceof Player player)) continue;
            player.damage(6 + increase);
        }

        mage.teleport(selectPotentialSafeArea());

        Bukkit.getScheduler().runTaskLater(SkyAddon.getPlugin(), () -> {
            mage.getWorld().strikeLightningEffect(mage.getLocation());
            mage.getWorld().playSound(mage.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 2.0F);

            for(Entity ent : mage.getNearbyEntities(3, 3, 3)) {
                if(!(ent instanceof Player player)) continue;
                player.damage(6);
            }
        }, 2L);
    }

    private void meteorShower(org.bukkit.entity.LivingEntity mage) {
        mage.getWorld().playSound(mage.getEyeLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 0.9F);
        for(int i = 0; i < 20; i++) {
            final Location meteorLocation = LocationUtil.selectRandomNearbyLocation(mage.getLocation(), 10);
            double randomX = (55 * Math.random()) - (55 * Math.random());
            double randomZ = (55 * Math.random()) - (55 * Math.random());
            final Location airLocation = meteorLocation.clone().add(randomX, 50, randomZ);
            final BlockDisplay blockDisplay = (BlockDisplay) mage.getWorld().spawnEntity(airLocation, org.bukkit.entity.EntityType.BLOCK_DISPLAY);
            blockDisplay.setBlock(Material.MAGMA_BLOCK.createBlockData());

            Transformation transformation = blockDisplay.getTransformation();
            transformation.getScale().add(0.5F, 0.5F, 0.5F);
            blockDisplay.setTransformation(transformation);

            Meteor meteor = new Meteor(airLocation, meteorLocation, blockDisplay.getUniqueId());
            meteors.add(meteor);
        }
    }

    private void runMeteors() {
        if(meteors.isEmpty()) return;

        for(Meteor meteor : meteors) {

            final UUID id = meteor.getDisplayID();
            final Vector dir = meteor.getEndLocation().toVector().subtract(meteor.getStartLocation().toVector()).normalize().multiply(0.9);
            final Entity entity = Bukkit.getEntity(id);
            if(entity == null) continue;
            final BlockDisplay meteorBlock = (BlockDisplay) entity;

            meteorBlock.teleport(meteorBlock.getLocation().add(dir));

            if(meteorBlock.getLocation().distanceSquared(meteor.getEndLocation()) <= 2.5) {

                ParticleUtil.spawnParticle(meteorBlock.getWorld(), Particle.FLAME, meteorBlock.getLocation().toCenterLocation(), 20, 0.0, 0.0, 0.0, 0.2);
                meteorBlock.getWorld().playSound(meteorBlock.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0F, 1.25F);
                meteorBlock.remove();

                for(Entity ent : meteor.getEndLocation().getNearbyEntities(5, 5, 5)) {
                    if(!(ent instanceof Player player)) continue;
                    player.setFireTicks(300);
                    player.damage(4);
                }

                for(int i = 0; i < 5; i++) {
                    final Location loc = LocationUtil.selectRandomNearbyLocation(meteorBlock.getLocation(), 5);
                    final LocWrapper locWrapper = new LocWrapper(loc.getWorld().getName(), loc.getBlockX(), loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()), loc.getBlockZ(), 0, 0);
                    if(locWrapper.toBukkit().getBlock().getType() == Material.MAGMA_BLOCK) continue;
                    final MeteorFloorBlock meteorFloorBlock = new MeteorFloorBlock(locWrapper, locWrapper.toBukkit().getBlock().getType());
                    locWrapper.toBukkit().getBlock().setType(Material.MAGMA_BLOCK, false);
                    blocksChanged.add(meteorFloorBlock);
                }
            }
        }
    }

    private Location selectPotentialSafeArea() {
        final Location currentLocation = this.getBukkitLivingEntity().getLocation();
        final Set<int[]> nearbyChunks = new HashSet<>();
        int[] finalChunk = new int[]{};

        final int currentChunkX = currentLocation.getChunk().getX();
        final int currentChunkZ = currentLocation.getChunk().getZ();

        for(int xOff = currentChunkX - 1; xOff <= currentChunkX + 1; xOff++) {
            for(int zOff = currentChunkZ - 1; zOff <= currentChunkZ + 1; zOff++) {
                nearbyChunks.add(new int[]{xOff, zOff});
            }
        }

        int smallestCount = 1000;

        for(int[] potentialChunks : nearbyChunks) {

            final Chunk chunk = currentLocation.getWorld().getChunkAt(potentialChunks[0], potentialChunks[1]);

            int chunkCount = 0;
            for(Entity entity : chunk.getEntities()) {
                if(!(entity instanceof Player)) continue;
                chunkCount++;
            }

            if(chunkCount <= smallestCount) {
                smallestCount = chunkCount;
                finalChunk = new int[]{chunk.getX(), chunk.getZ()};
            }
        }

        final Chunk location = currentLocation.getWorld().getChunkAt(finalChunk[0], finalChunk[1]);

        final int minChunkX = location.getX() * 16;
        final int minChunkZ = location.getZ() * 16;

        final int maxChunkX = (location.getX() + 1) * 16 - 1;
        final int maxChunkZ = (location.getZ() + 1) * 16 - 1;


        for(int i = 0; i < 10; i++) {

            final int randomX = minChunkX + random.nextInt(maxChunkX - minChunkX + 1);
            final int randomZ = minChunkZ + random.nextInt(maxChunkZ - minChunkZ + 1);

            int y = location.getWorld().getHighestBlockYAt(randomX, randomZ);

            if (y > currentLocation.getBlockY() + 5) {
                y = currentLocation.getBlockY();
            }

            final Block initial = location.getWorld().getBlockAt(randomX, y+1, randomZ);
            final Block ground = initial.getLocation().clone().subtract(0, 1, 0).getBlock();
            final Block air = initial.getLocation().clone().add(0, 1, 0).getBlock();

            if(ground.isSolid() && air.isEmpty() && initial.isEmpty()) {
                return ground.getLocation().toCenterLocation().add(0, 1, 0);
            }

        }

        return currentLocation;

    }

    private boolean canTeleport() {
        final long currentTime = System.currentTimeMillis();
        return currentTime - lastTeleportTime >= (15000 - (magePhase.getPhase() * 1000L));
    }

    private void trySwapPhase(org.bukkit.entity.LivingEntity mage) {
        if(getHealthPercentage(mage) <= 0.75 && (magePhase != Phase.TWO && magePhase != Phase.THREE)) {
            magePhase = Phase.TWO;
            attackDelay = 1100;
            meteorShower(mage);
        }else if(getHealthPercentage(mage) <= 0.35 && magePhase != Phase.THREE) {
            magePhase = Phase.THREE;
            attackDelay = 1000;
            meteorShower(mage);
        }
    }

    private double getHealthPercentage(org.bukkit.entity.LivingEntity mage) {
        double maxHealth = mage.getMaxHealth();
        double currHealth = mage.getHealth();

        return currHealth / maxHealth;
    }

    @Getter
    public enum Phase {
        ONE(1),
        TWO(2),
        THREE(3);

        @Setter
        private int phase;

        Phase(int phase) {
            this.phase = phase;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class Meteor {

        private Location startLocation;
        private Location endLocation;
        private UUID displayID;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class MeteorFloorBlock {

        private LocWrapper location;
        private Material oldMaterial;

    }

}
