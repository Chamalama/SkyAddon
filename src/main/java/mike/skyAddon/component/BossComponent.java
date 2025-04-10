package mike.skyAddon.component;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BossComponent {

    @Expose
    private String lastBossSpawned;
    @Expose
    private long lastBossSpawnTime;
    @Expose
    private long timePerBossSpawn;

}
