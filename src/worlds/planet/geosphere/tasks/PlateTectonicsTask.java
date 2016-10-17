

package worlds.planet.geosphere.tasks;

import engine.util.Point;
import engine.util.Tools;
import engine.util.task.BasicTask;

import java.util.ArrayList;
import java.util.List;
import worlds.planet.Planet;

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
    private Geosphere geosphere;
    
    public PlateTectonicsTask(Geosphere geosphere){
        this.geosphere = geosphere;
    }
    
    @Override
    public final void construct() {
    	plates = new ArrayList<>();
    }
    
    /**
     * Updates all the plates.
     */
    public void updatePlates(){
        int cellLength = 20;
    	plates.forEach(plate ->{
            plate.forEach(cellPoint -> {
            
                int x = (int)cellPoint.getX();
                int y = (int)cellPoint.getY();
                    
                PlanetCell cell = geosphere.waitForCellAt(x, y);
                Point cellVelocity = cell.getVelocity();
                Point cellPos = cell.getGridPosition();
                Point cellActPos = cell.getActualPosition();

                cellActPos.add(cellVelocity);

                Point adj = new Point(cellLength, cellLength);
                cellPos.add(adj);

                if (cellVelocity.getX() > 0){
                    if (cellPos.getX() <= cellActPos.getX()){
                        System.out.println("Move Cell"+cell+" in the X direction by +1 cell");
                        cellActPos.set(cellPos);
                    }
                }else if (cellVelocity.getX() < 0){
                    if (cellPos.getX() >= cellActPos.getX()){
                        System.out.println("Move Cell"+cell+" in the X direction by -1 cell");
                    }
                }

                if (cellVelocity.getY() > 0){
                    if (cellPos.getY() <= cellActPos.getY()){
                        System.out.println("Move Cell"+cell+" in the Y direction by +1 cell");
                    }
                }else if (cellVelocity.getY() < 0){
                    if (cellPos.getY() >= cellActPos.getY()){
                        System.out.println("Move Cell"+cell+" in the Y direction by -1 cell");
                    }
                }
                geosphere.release(cell);
            }); 
            System.out.println("Plate Updated");
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
            Point cellVelocity = cell.getVelocity();
            
            cellVelocity.set(velocity);
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
    public void collideCells(PlanetCell from, PlanetCell to, float maxDepth){
    	
    }
    
    public Point calculateEnergyTransfer(PlanetCell from, PlanetCell to, float c){
    	
    	float massA = from.getTotalMass();
    	float massB = to.getTotalMass();
    	
    	float velA_X = from.getVelocity().getX();
    	float velA_Y = from.getVelocity().getY();
    	
    	float velB_X = to.getVelocity().getX();
    	float velB_Y = to.getVelocity().getY();
    	
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
}
