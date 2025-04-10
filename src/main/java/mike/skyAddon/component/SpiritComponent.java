package mike.skyAddon.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mike.mLibrary.util.LocWrapper;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class SpiritComponent {

    private LocWrapper startLocation;
    private LocWrapper endLocation;
    private UUID spiritID;

}
