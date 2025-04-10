package mike.skyAddon.service.task;

import gg.supervisor.core.annotation.Component;
import gg.supervisor.util.runnable.AbstractRunnable;
import mike.skyAddon.SkyAddon;
import mike.skyAddon.component.SpiritComponent;
import mike.skyAddon.entity.Bosses;
import mike.skyAddon.entity.LMSEntity;
import mike.skyAddon.service.CrystalService;
import mike.skyAddon.service.RuneAnimationService;
import org.bukkit.*;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Iterator;

@Component
public class TickingTask extends AbstractRunnable {

    private final RuneAnimationService runeAnimationService;
    private final CrystalService crystalService;

    public TickingTask(RuneAnimationService runeAnimationService, CrystalService crystalService) {
        super(SkyAddon.getPlugin(), 1, false);
        this.runeAnimationService = runeAnimationService;
        this.crystalService = crystalService;
    }

    @Override
    public void run() {
        for(LMSEntity lmsEntity : Bosses.getIdToLMSEntity().values()) {
            lmsEntity.tickLMS();
        }
        runAnimation();
    }

    private void runAnimation() {

        final Iterator<SpiritComponent> spiritComponentIterator = runeAnimationService.getSpiritAnimationSet().iterator();

        while(spiritComponentIterator.hasNext()) {

            final SpiritComponent spiritComponent = spiritComponentIterator.next();
            final Entity spiritEntity = Bukkit.getEntity(spiritComponent.getSpiritID());

            if(spiritEntity == null) {
                spiritComponentIterator.remove();
                return;
            }

            final double distanceToEnd = spiritEntity.getLocation().distanceSquared(spiritComponent.getEndLocation().toBukkit().toCenterLocation());

            if(distanceToEnd <= 0.1) {
                spiritEntity.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, spiritEntity.getLocation().toCenterLocation(), 20);
                spiritEntity.getWorld().playSound(spiritEntity.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0F, 0.8F);
                spiritEntity.removeMetadata("SPIRIT", SkyAddon.getPlugin());
                crystalService.addRune();
                spiritEntity.remove();
                spiritComponentIterator.remove();
                return;
            }

            final Location endLocation = spiritComponent.getEndLocation().toBukkit().toCenterLocation();
            final Location startLocation = spiritComponent.getStartLocation().toBukkit().toCenterLocation();

            final Vector toTeleport = endLocation.toVector().subtract(startLocation.toVector()).normalize();
            final Vector vel = toTeleport.multiply(0.45);

            final Allay spirit = (Allay) spiritEntity;

            spirit.lookAt(crystalService.getCrystalLocationStorage().getOrLoad(false).toBukkit().toCenterLocation().clone().add(0, 0.5, 0));

            spiritEntity.teleport(spiritEntity.getLocation().add(vel));


        }
    }

}
