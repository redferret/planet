

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

    private List<List<Point>> plates = new ArrayList<>();
    
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
        return new ArrayList<>();
    }
    
    /**
     * At the simplest a cell will collide with another cell. This method
     * won't test for a collision or change velocities. Instead the plates
     * will fold or get thrust ontop or pushed down depending on the densities
     * of both. There is a depth assigned to the function, below that depth
     * other layers are ignored and melted.
     * 
     * @param from The cell moving that will thrust it's layers on top or below
     * @param to The cell that will recieve the crust
     */
    public void collideCells(PlanetCell from, PlanetCell to, float maxDepth){
    	
    }
    
    public void addPlate(List<Point> plate){
    	plates.add(plate);
    }
    
    /**
     * Will set the parent for each cell location.
     */
    public void setParent(PlanetCell cellToSet){
    	cellToSet.setPlateControlThread(getTaskThread());
    }
    
    public void removePlate(int index){
    	plates.remove(index);
    }
    
    /**
     * A non negative number of how many plates there are.
     * @return The number of plates
     */
    public int getNumberOfPlates(){
    	return plates.size();
    }
}
