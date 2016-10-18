

package worlds.planet.geosphere.tasks;

import engine.util.Point;
import engine.util.Tools;
import engine.util.concurrent.SurfaceThread;
import engine.util.task.BasicTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import worlds.planet.PlanetCell;
import worlds.planet.geosphere.Geosphere;

/**
 * Performs basic plate tectonics on a section of the Geosphere. 
 * Each thread will have their own plates to update not exclusive
 * to their own section of the surface; that is a thread can update
 * a plate located anywhere on the surface. 
 * @author Richard DeSilvey
 */
public abstract class PlateTectonicsTask extends BasicTask {

    private List<List<Point>> plates = new ArrayList<>();
    private final Geosphere geosphere;
    
    public PlateTectonicsTask(Geosphere geosphere) {
        this.geosphere = geosphere;
    }
    
    @Override
    public final void construct() {
    	plates = new ArrayList<>();
    }
    
    /**
     * Updates all the plates.
     */
    public void updatePlates() {
    	int cellLength = PlanetCell.cellLength;
    	plates.forEach(plate -> {
            Iterator<Point> plateIter = plate.iterator();
            while(plateIter.hasNext()){
                updateNext(plateIter, cellLength);
            }
        });
    }
    
    /**
     * Builds a list of points that define a new plate. The list
     * is always a non-null list.
     * @param centralCell The center of the plate
     * @param radius The radius of the plate
     * @param velocity
     * @return The list of positions defining the plate.
     */
    public List<Point> buildPlate(Point centralCell, int radius, Point velocity){
        
        List<Point> plate = Tools.fillPoints(centralCell, radius);
        
        plate.forEach(point -> {
            int x = (int)point.getX();
            int y = (int)point.getY();
            PlanetCell cell = geosphere.waitForCellAt(x, y);
            cell.getVelocity().set(velocity);
            cell.setPlateControlThread(getTaskThread());
            resetActualPosition(cell);
            geosphere.release(cell);
        });
        
        return plate;
    }
    
    /**
     * At the simplest a cell will collide with another cell. This method
     * won't test for a collision or change velocities. Instead the plates
     * will fold or get thrust on top or pushed down depending on the densities
     * of both. There is a depth assigned to the function, below that depth
     * other layers are ignored and melted.
     * 
     * @param from The cell moving that will thrust it's layers on top or below
     * @param to The cell that will receive the crust
     * @param maxDepth The maximum depth that will not subduct
     */
    public void thrustCrust(PlanetCell from, PlanetCell to, float maxDepth){
    	
    }
    
    /**
     * Calculates the transfer of energy when two cells collide. cellA is colliding into
     * cellB and the returned object is the new velocity for cellA. 
     * 
     * @param cellA The cell colliding into cellB
     * @param cellB The cell being collided into
     * @param c The coefficient of restiution, 0 = complete inelastic, 1 = complete elastic
     * @return The new velocity of cellA
     */
    public Point calculateEnergyTransfer(PlanetCell cellA, PlanetCell cellB, float c){
    	
    	float massA = cellA.getTotalMass();
    	float massB = cellB.getTotalMass();
    	
    	float velA_X = cellA.getVelocity().getX();
    	float velA_Y = cellA.getVelocity().getY();
    	
    	float velB_X = cellB.getVelocity().getX();
    	float velB_Y = cellB.getVelocity().getY();
    	
    	float finalVelA_X, finalVelA_Y;
    	float sumOfMasses = (massA + massB);
    			
    	finalVelA_X = (c * (massB * (velB_X-velA_X)) + (massA*velA_X) + (massB*velB_X)) / sumOfMasses;
    	finalVelA_Y = (c * (massB * (velB_Y-velA_Y)) + (massA*velA_Y) + (massB*velB_Y)) / sumOfMasses;
    	
    	return new Point(finalVelA_X, finalVelA_Y);
    }
    
    public void addPlate(List<Point> plate){
    	plates.add(plate);
    }
    
    /**
     * Will set the parent for each cell location.
     * @param cellToSet
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
    
    private boolean isOwnedByThisPlate(PlanetCell cell){
        SurfaceThread pct = cell.getPlateControlThread();
        SurfaceThread taskThread = getTaskThread();
        return pct.equals(taskThread);
    }
    
    private void updateNext(Iterator<Point> plateIter, int cellLength) {
    	
        Point cellPoint = plateIter.next();
        
    	int x = (int)cellPoint.getX();
        int y = (int)cellPoint.getY();
        
        PlanetCell cell = geosphere.waitForCellAt(x, y);
        
        if (isOwnedByThisPlate(cell)){
        
            Point cellVelocity = cell.getVelocity();
            Point cellPos = cell.getGridPosition();
            Point cellActPos = cell.getActualPosition();

            cellActPos.add(cellVelocity);

            Point adj = new Point(cellLength, cellLength);
            cellPos.mul(adj);


            // Move the strata from one cell to the other
            // and if a collision occurs then energy is transfered
            // into the cell being collided with and crust is trust
            // on top or below depending on the densities of both
            // cells. The collision is 100% inelastic, cells will stick
            // together when collision occures.
            if (cellVelocity.getX() > 0) { // Move right
                if (cellPos.getX() <= cellActPos.getX()) {
                    // Reset the cell's active position
                    moveCell(cell, new Point(1, 0));
                }
            }else if (cellVelocity.getX() < 0) {
                if (cellPos.getX() >= cellActPos.getX()) {
                    moveCell(cell, new Point(-1, 0));
                }
            }

            if (cellVelocity.getY() > 0) {
                if (cellPos.getY() <= cellActPos.getY()) {
                    moveCell(cell, new Point(0, 1));
                }
            }else if (cellVelocity.getY() < 0) {
                if (cellPos.getY() >= cellActPos.getY()) {
                    moveCell(cell, new Point(0, -1));
                }
            }
            
        } else {
            plateIter.remove();
        }
        geosphere.release(cell);
    }
    
    /**	
     * Movement that has occured will move a given cell in the given direction. 
     * When movement happens the cell's actual position is reset.
     * @param cell The cell being moved
     * @param direction The direction the cell is moving in
     */
    private void moveCell(PlanetCell cell, Point direction) {
    	resetActualPosition(cell);
    	
    }
    
    private void resetActualPosition(PlanetCell cell){
    	int cellLength = PlanetCell.cellLength;
    	cell.getActualPosition().set(cell.getGridPosition());
    	cell.getActualPosition().mul(new Point(cellLength, cellLength));
    }
}
