

package worlds.planet;

import worlds.planet.atmosphere.AtmoCell;
import java.util.List;

/**
 *
 * @author Richard DeSilvey
 */
public class PlanetCell extends AtmoCell {

	public static int cellArea, cellLength;
	
	static {
		cellArea = 1;
		cellLength = 1;
	}
	
    public PlanetCell(int x, int y) {
        super(x, y);
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        return super.render(settings);
    }

}
