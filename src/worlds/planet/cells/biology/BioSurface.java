

package worlds.planet.cells.biology;

import engine.cells.AtomicData;
import engine.surface.SurfaceMap;
import engine.util.task.Boundaries;
import engine.util.task.Task;
import engine.util.task.TaskManager;
import engine.util.Tools;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import worlds.planet.cells.PlanetCell;
import worlds.planet.surface.PlanetSurface;
import static worlds.planet.Planet.instance;

/**
 * A BioSurface is a SurfaceMap that contains BioNodes that are used to hold
 * plant and animal life. The maps are only 3x3 and will not update unless a
 * cell or more is active or alive.
 * @author Richard DeSilvey
 */
public class BioSurface extends SurfaceMap<BioNode> {

    private static final int BIO_CELL_COUNT = 3, THREAD_COUNT = 1,
            BIO_TOTAL_COUNT = BIO_CELL_COUNT * BIO_CELL_COUNT, NO_DELAY = 0;
    
    /**
     * The number of cells that are currently active. If the count is zero then
     * this section of BioNodes are not updated.
     */
    private int cellCount;
    private BioCell parentCell;
    private TaskManager manager;
    
    public BioSurface(BioCell parentCell) {
        super(BIO_CELL_COUNT, NO_DELAY, THREAD_COUNT);
        this.parentCell = parentCell;
        manager = new TaskManager(new Boundaries(0, BIO_CELL_COUNT));
        manager.addTask(new BioTask());
        
        Map<Integer, AtomicData<BioNode>> map = new Hashtable<>();
        setMap(map);
    }

    @Override
    public void reset() {
        cellCount = 0;
        buildMap();
    }
    
    public void incrementCellCount(){
        cellCount++;
        if (cellCount > BIO_TOTAL_COUNT){
            cellCount = BIO_TOTAL_COUNT;
        }
    }
    
    public void decrementCellCount(){
        cellCount--;
        if (cellCount < 0){
            cellCount = 0;
        }
    }
    
    public boolean surfaceHasLife() {
        return cellCount > 0;
    }
    
    /**
     * Skips thread averaging by overriding the super method
     */
    @Override
    public void update() {
        manager.performTasks();
    }

    @Override
    public BioNode generateCell(int x, int y) {
        return new BioNode(x, y, this);
    }
    
    /**
     * LifeForms are updated with this Task
     */
    private class BioTask extends Task {

        @Override
        public void perform(int x, int y) {
            
            BioNode node = getCellAt(x, y);
            
            if (node.hasLife()) {
                BioNode[] neighbors = countNeighbors(node);
                node.update(neighbors);
            }
            
            release(node);
        }
        
        private BioNode[] countNeighbors(BioNode node){
            int parentX = parentCell.getX(), neighborCellX = parentX;
            int parentY = parentCell.getY(), neighborCellY = parentY;
            PlanetCell neighborCell;
            
            PlanetSurface surface = (PlanetSurface) instance().getSurface();
            final int WIDTH = surface.getGridWidth();
            
            int nodeX = node.getX(), nodeY = node.getY(), neighborX, neighborY;
            
            List<BioNode> neighbors = new ArrayList<>();
            BioNode neighborNode = null;
            
            final int RE_POS_X = (nodeX - (BIO_CELL_COUNT / 2));
            final int RE_POS_Y = (nodeY - (BIO_CELL_COUNT / 2));
            
            final int X_BOUNDS = RE_POS_X + BIO_CELL_COUNT;
            final int Y_BOUNDS = RE_POS_Y + BIO_CELL_COUNT;
            
            for (int x = RE_POS_X; x < X_BOUNDS; x++) {
                for (int y = RE_POS_Y; y < Y_BOUNDS; y++) {

                    if (x < 0){
                        neighborX = BIO_CELL_COUNT - 1;
                        neighborCellX = Tools.checkBounds(parentX - 1, WIDTH);
                    }else if (x > BIO_CELL_COUNT - 1) {
                        neighborX = 0;
                        neighborCellX = Tools.checkBounds(parentX + 1, WIDTH);
                    }else{
                        neighborX = x;
                    }
                    
                    if (y < 0){
                        neighborY = BIO_CELL_COUNT - 1;
                        neighborCellY = Tools.checkBounds(parentY - 1, WIDTH);
                    }else if (y > BIO_CELL_COUNT - 1) {
                        neighborY = 0;
                        neighborCellY = Tools.checkBounds(parentY + 1, WIDTH);
                    }else{
                        neighborY = y;
                    }
                    
                    neighborCell = surface.getCellAt(neighborCellX, neighborCellY);
                    
                    BioSurface bioSurfaceRef = null;
                    if (neighborCell != parentCell){
                    	bioSurfaceRef = neighborCell.getBioSurface();
                        neighborNode = bioSurfaceRef.getCellAt(neighborX, neighborY);
                    }else{
                        neighborNode = getCellAt(neighborX, neighborY);
                    }
                    
                    if ((neighborNode != node) && neighborNode.hasLife()) {
                        neighbors.add(neighborNode);
                    }
                    
                    if (bioSurfaceRef != null){
                    	bioSurfaceRef.release(neighborNode);
                    }else{
                    	release(neighborNode);
                    }
                    surface.release(neighborCell);
                }
            }
            
            return neighbors.toArray(new BioNode[neighbors.size()]);
        }
        
        @Override
        public boolean check() {
            return surfaceHasLife();
        }

        @Override
        public void before() {
        }

        @Override
        public void after() {
        }
        
    }
    
}
