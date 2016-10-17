

package worlds.planet;

import worlds.planet.atmosphere.AtmoCell;
import java.util.List;

/**
 *
 * @author Richard DeSilvey
 */
public class PlanetCell extends AtmoCell {

	public static int cellArea;
	
	static {
		cellArea = 0;
	}
	
    public PlanetCell(int x, int y) {
        super(x, y);
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        return super.render(settings);
    }

}
