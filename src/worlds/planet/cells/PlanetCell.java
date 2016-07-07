

package worlds.planet.cells;

import worlds.planet.cells.atmosphere.AtmoCell;
import java.util.List;

/**
 *
 * @author Richard DeSilvey
 */
public class PlanetCell extends AtmoCell {

    public PlanetCell(int x, int y) {
        super(x, y);
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        return super.render(settings);
    }

}
