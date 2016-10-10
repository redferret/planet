

package worlds.planet.geosphere.tasks;

import engine.util.Point;
import engine.util.concurrent.SurfaceThread;
import engine.util.task.BasicTask;

import java.util.ArrayList;
import java.util.List;

import worlds.planet.PlanetCell;

/**
 * Performs basic plate tectonics on a section of the Geosphere. 
 * Each thread will have their own plates to update not exclusive
 * to their own section of the surface; that is a thread can update
 * a plate located anywhere on the surface. 
 * @author Richard DeSilvey
 */
public abstract class PlateTectonicsTask extends BasicTask {

    private List<List<Point>> plates;
    
    @Override
    public final void construct() {
    	plates = new ArrayList<>();
    }
    
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
    
    public void addPlate(List<Point> plate){
    	
    }
    
    /**
     * Will set the parent for each cell location.
     */
    public void setParent(PlanetCell cellToSet){
    	cellToSet.setPlateControlThread(getTaskThread());
    }
    
    /**
     * A non negative number of how many plates there are.
     * @return The number of plates
     */
    public int getNumberOfPlates(){
    	return -1;
    }
}
