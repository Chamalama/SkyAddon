package mike.skyAddon.component;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import mike.mLibrary.util.LocWrapper;

import java.util.LinkedList;

@Getter
@Setter
public class KeeperSpawningComponent {

    @Expose
    private LinkedList<SpawnLocationComponent> spawnLocations = new LinkedList<>() {{
        add(new SpawnLocationComponent(new LocWrapper("LMS", 0, 65, 0, 0, 0), 0L));
    }};
    @Expose
    private long spawnTime = 120;

}
