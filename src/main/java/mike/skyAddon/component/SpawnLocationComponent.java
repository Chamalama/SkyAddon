package mike.skyAddon.component;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import mike.mLibrary.util.LocWrapper;

@Getter
@Setter
public class SpawnLocationComponent {

    @Expose
    private LocWrapper locWrapper;

    private transient long lastSpawnTime;

    private transient boolean spawned = false;

    public SpawnLocationComponent(LocWrapper locWrapper, long lastSpawnTime) {
        this.locWrapper = locWrapper;
        this.lastSpawnTime = lastSpawnTime;
    }

}
