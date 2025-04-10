package mike.skyAddon.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mike.mLibrary.util.LocWrapper;
import org.bukkit.Material;

@Getter
@Setter
@AllArgsConstructor
public class BlockComponent {

    private LocWrapper locWrapper;
    private Material material;

}
