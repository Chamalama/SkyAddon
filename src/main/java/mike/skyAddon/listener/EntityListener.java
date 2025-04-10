package mike.skyAddon.listener;

import gg.supervisor.core.annotation.Component;
import mike.mLibrary.player.PlayerUtil;
import mike.mLibrary.text.Chat;
import mike.skyAddon.SkyAddon;
import mike.skyAddon.component.BossComponent;
import mike.skyAddon.component.SpawnLocationComponent;
import mike.skyAddon.entity.Bosses;
import mike.skyAddon.entity.LMSEntity;
import mike.skyAddon.factory.RuneItemFactory;
import mike.skyAddon.service.CrystalService;
import mike.skyAddon.service.LMSService;
import mike.skyAddon.service.RuneAnimationService;
import mike.skyAddon.service.VaultService;
import mike.skyAddon.storage.BossStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

@Component
public class EntityListener implements Listener {

    private final LMSService lmsService;
    private final RuneItemFactory runeItemFactory;
    private final CrystalService crystalService;
    private final RuneAnimationService runeAnimationService;
    private final BossStorage bossStorage;
    private final VaultService vaultService;

    public EntityListener(LMSService lmsService, RuneItemFactory runeItemFactory, CrystalService crystalService, RuneAnimationService runeAnimationService, BossStorage bossStorage, VaultService vaultService) {
        this.lmsService = lmsService;
        this.runeItemFactory = runeItemFactory;
        this.crystalService = crystalService;
        this.runeAnimationService = runeAnimationService;
        this.bossStorage = bossStorage;
        this.vaultService = vaultService;
    }

    @EventHandler
    public void onTakeDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof LivingEntity le)) return;
        final LMSEntity lmsEntity = Bosses.fromCache(le);
        if(lmsEntity == null) return;
        lmsEntity.onTakeDamage(event);
    }

    @EventHandler
    public void onDealDamage(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof LivingEntity le)) return;
        final LMSEntity lmsEntity = Bosses.fromCache(le);
        if(lmsEntity == null) return;
        lmsEntity.onDealDamage(event);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        final LivingEntity livingEntity = event.getEntity();
        final LMSEntity lmsEntity = Bosses.fromCache(livingEntity);

        if(lmsEntity == null) return;

        lmsEntity.onDeath(event);
        event.getDrops().clear();
        event.setDroppedExp(0);

        tryResetBoss(livingEntity);

        tryClearKeeper(livingEntity);
    }

    private void tryResetBoss(LivingEntity livingEntity) {

        if(livingEntity.hasMetadata("BOSS_TYPE")) {

            final BossComponent bossComponent = bossStorage.getOrLoad(false);

            livingEntity.removeMetadata("BOSS_TYPE", SkyAddon.getPlugin());

            crystalService.setActiveBoss(null);
            crystalService.setActiveBossId(null);
            crystalService.setBossStarted(false);

            bossComponent.setLastBossSpawnTime(System.currentTimeMillis());

            bossStorage.update();

            crystalService.resetCrystal();

            vaultService.sortLooters();

            final Bosses bossType = Bosses.enumInst(livingEntity);
            if(bossType == null) return;

            Bukkit.getScheduler().runTaskAsynchronously(SkyAddon.getPlugin(), () -> {

               for(Player player : Bukkit.getOnlinePlayers()) {

                   player.sendMessage("");
                   player.sendMessage(Chat.translate( "     " + bossName(bossType) + " Defeated  "));
                   player.sendMessage(Chat.translate(ChatColor.GRAY + "             Top Damagers   "));
                   player.sendMessage("");

                   int i = 0;

                   for(UUID uuid : vaultService.getAuthorizedPlayers()) {
                       i++;
                       final Player bossPlayer = Bukkit.getPlayer(uuid);
                       if(bossPlayer == null) continue;
                       player.sendMessage(Chat.translate("            " + ChatColor.WHITE + i + ". " + bossPlayer.getName() + " " + ChatColor.RED + vaultService.getPlayerDamage(player) + " DMG"));
                   }

                   player.sendMessage("");
               }
                vaultService.getDamageMap().clear();

            });



        }
    }

    private void tryClearKeeper(LivingEntity livingEntity) {

        final SpawnLocationComponent storedLocation = lmsService.fromMob(livingEntity);
        if(storedLocation == null) return;
        storedLocation.setSpawned(false);
        storedLocation.setLastSpawnTime(System.currentTimeMillis());
        lmsService.clearMob(livingEntity);

        final Player player = livingEntity.getKiller();

        if(player == null) return;

        if(Bosses.enumInst(livingEntity) == Bosses.BANDIT_RUNE_KEEPER) {
            PlayerUtil.giveItem(player, runeItemFactory.getRune(), 1);
            player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
            runeAnimationService.spawnSpirit(livingEntity.getEyeLocation().toCenterLocation());
            livingEntity.remove();
        }

    }

    private String bossName(Bosses bosses) {
        String name = null;
        switch (bosses) {
            case DARK_MAGE -> name = ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Dark Mage";
            case EXECUTIONER -> name = ChatColor.RED + ChatColor.BOLD.toString() + "Executioner";
        }
        return name;
    }

}
