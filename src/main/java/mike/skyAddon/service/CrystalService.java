package mike.skyAddon.service;

import gg.supervisor.core.annotation.Component;
import lombok.Getter;
import lombok.Setter;
import mike.mLibrary.util.LocWrapper;
import mike.skyAddon.component.BlockComponent;
import mike.skyAddon.component.BossComponent;
import mike.skyAddon.entity.Bosses;
import mike.skyAddon.entity.LMSEntity;
import mike.skyAddon.storage.BossStorage;
import mike.skyAddon.storage.CrystalLocationStorage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;

@Setter
@Getter
@Component
public class CrystalService {

    private final CrystalLocationStorage crystalLocationStorage;
    private final BossStorage bossStorage;

    private Bosses activeBoss = null;
    private UUID activeBossId = null;
    private int crystalRuneCount = 0;
    private boolean bossStarted = false;

    private final int RUNES_NEEDED = 4;

    private final Queue<BlockComponent> crystalBlocks = new LinkedList<>();

    private final Set<LocWrapper> cachedCrystalBlocks = new HashSet<>();

    public CrystalService(CrystalLocationStorage crystalLocationStorage, BossStorage bossStorage) {
        this.crystalLocationStorage = crystalLocationStorage;
        this.bossStorage = bossStorage;
    }

    public void spawnBoss() {

        final BossComponent bossComponent = bossStorage.getOrLoad(false);

        final long currentTime = System.currentTimeMillis();

        final long lastSpawnTime = bossComponent.getLastBossSpawnTime();

        if(currentTime - lastSpawnTime < bossComponent.getTimePerBossSpawn()) return;

        if(activeBoss != null) return;

        final List<Bosses> potentialBosses = new ArrayList<>();

        for(Bosses bosses : Bosses.values()) {
            if(bossComponent.getLastBossSpawned() != null && bossComponent.getLastBossSpawned().equalsIgnoreCase(bosses.name())) continue;
            if(bosses.isMobType()) continue;
            potentialBosses.add(bosses);
        }

        if(potentialBosses.isEmpty()) {
            return;
        }

        final int random = new Random().nextInt(potentialBosses.size());

        final Bosses toSpawn = potentialBosses.get(random);

        final LivingEntity boss = toSpawn.spawn(crystalLocationStorage.getOrLoad(false).toBukkit().toCenterLocation());
        if(boss == null) return;
        activeBossId = boss.getUniqueId();

        activeBoss = toSpawn;

        bossComponent.setLastBossSpawned(activeBoss.name());

        bossStorage.update();

    }

    public void addRune() {
        if(this.bossStarted) return;
        this.crystalRuneCount++;
        if(this.crystalRuneCount >= RUNES_NEEDED) {
            breakCrystal();
            this.crystalRuneCount = 0;
        }
    }

    public void breakCrystal() {
        this.bossStarted = true;

        if(activeBossId == null) return;
        final Entity entity = Bukkit.getEntity(activeBossId);
        if(entity != null) {
            final LMSEntity lmsEntity = Bosses.fromCache((LivingEntity) entity);
            if (lmsEntity == null) return;
            lmsEntity.activate();
        }
        /*
        Faster performance than looping each time
        Not sure if there's a faster or easier way to get blocks in a radius like that
        Could probably do it through vectors
        */
        if(!cachedCrystalBlocks.isEmpty()) {
            for(LocWrapper locWrapper : cachedCrystalBlocks) {
                final Location bukkitLocation = locWrapper.toBukkit();
                final Block block = bukkitLocation.getBlock();
                crystalBlocks.add(new BlockComponent(locWrapper, block.getType()));
                block.setType(Material.AIR);
                block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().toCenterLocation(), 10, Material.LIGHT_BLUE_STAINED_GLASS.createBlockData());
                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
            }
            return;
        }

        final Location crystalLocation = crystalLocationStorage.getOrLoad(false).toBukkit().toCenterLocation();


        //This should be async or should just store the build on startup and access it from there
        for(int xOff = crystalLocation.getBlockX() - 10; xOff <= crystalLocation.getBlockX() + 10; xOff++) {
            for(int yOff = crystalLocation.getBlockY() - 10; yOff <= crystalLocation.getBlockY() + 10; yOff++) {
                for(int zOff = crystalLocation.getBlockZ() - 10; zOff <= crystalLocation.getBlockZ() + 10; zOff++) {
                    final Block block = crystalLocation.getWorld().getBlockAt(xOff, yOff, zOff);
                    if(block.getType() != Material.LIGHT_BLUE_STAINED_GLASS) continue;
                    final LocWrapper locWrapper = new LocWrapper(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), 0, 0);
                    crystalBlocks.add(new BlockComponent(locWrapper, block.getType()));

                    //Cache the blocks we'll be modifying so we don't have to keep using this
                    cachedCrystalBlocks.add(locWrapper);

                    block.setType(Material.AIR);
                    block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().toCenterLocation(), 10, Material.LIGHT_BLUE_STAINED_GLASS.createBlockData());
                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
                }
            }
        }
    }

    public void resetCrystal() {
        while(!crystalBlocks.isEmpty()) {
            final BlockComponent component = crystalBlocks.poll();
            final Location location = component.getLocWrapper().toBukkit();
            location.getBlock().setType(component.getMaterial());
            location.getWorld().spawnParticle(Particle.BLOCK_CRACK, location.toCenterLocation(), 10, Material.LIGHT_BLUE_STAINED_GLASS.createBlockData());
        }
    }

}
