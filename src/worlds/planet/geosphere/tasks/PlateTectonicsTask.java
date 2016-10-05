

package worlds.planet.geosphere.tasks;

import engine.util.Point;
import engine.util.task.BasicTask;
import java.util.List;

/**
 *
 * @author Richard DeSilvey
 */
public abstract class PlateTectonicsTask extends BasicTask {

    private List<List<Point>> plates;
    
    /**
     * Updates all the plates.
     */
    public void updatePlates(){

    }
    
    /**
     * Builds a list of points that define a new plate. The list
     * is always a non-null list.
     * @param centralCell The center of the plate
     * @param radius The radius of the plate
     * @return The list of positions defining the plate.
     */
    public List<Point> buildPlate(Point centralCell, int radius){
        return null;
    }
}
