

package worlds.planet.geosphere;

import engine.util.Point;
import engine.util.task.BasicTask;
import java.util.List;
import worlds.planet.PlanetCell;

/**
 *
 * @author Richard DeSilvey
 */
public abstract class PlateTectonicsTask extends BasicTask {

    private List<List<PlanetCell>> plates;
    
    /**
     * Updates all the plates.
     */
    public void updatePlates(){
        
    }
    
    /**
     * Used to build a new plate using the given cell as the center of
     * the plate.
     * @param centralCell The location of the center of the plate
     * @return The list of positions of each cell for the new plate.
     */
    public List<Point> selectCellsFrom(Point centralCell){
        return null;
    }
}
