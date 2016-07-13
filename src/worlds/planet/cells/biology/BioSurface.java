

package worlds.planet.cells.biology;

import engine.surface.SurfaceMap;
import engine.util.Boundaries;
import engine.util.Task;
import engine.util.TaskManager;
import engine.util.Tools;
import java.util.ArrayList;
import java.util.List;
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
    private static final String THREAD_NAME = "Bio Surface";
    
    /**
     * The number of cells that are currently active. If the count is zero then
     * this section of BioNodes are not updated.
     */
    private int cellCount;
    private BioCell parentCell;
    private TaskManager manager;
    
    public BioSurface(BioCell parentCell) {
        super(BIO_CELL_COUNT, NO_DELAY, THREAD_NAME, THREAD_COUNT);
        this.parentCell = parentCell;
        Boundaries bounds = new Boundaries(0, BIO_CELL_COUNT);
        manager = new TaskManager(bounds);
        manager.addTask(new BioTask());
    }

    @Override
    public void reset() {
        cellCount = 0;
        setupMap();
    }
    
    private void incrementCellCount(){
        cellCount++;
        if (cellCount > BIO_TOTAL_COUNT){
            cellCount = BIO_TOTAL_COUNT;
        }
    }
    
    private void decrementCellCount(){
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
        return new BioNode(x, y);
    }
    
    /**
     * LifeForms are updated with this Task
     */
    private class BioTask implements Task {

        @Override
        public void perform(int x, int y) {
            
            BioNode node = getCellAt(x, y);

            if (node.hasLife()) {
                BioNode[] neighbors = countNeighbors(node);
                node.update(neighbors);
            }
        }
        
        private BioNode[] countNeighbors(BioNode node){
            int parentX = parentCell.getX(), neighborCellX = parentX;
            int parentY = parentCell.getY(), neighborCellY = parentY;
            BioCell neighborCell;
            
            PlanetSurface surface = (PlanetSurface) instance().getSurface();
            final int WIDTH = surface.getGridWidth();
            final int BIO_NODE_COUNT = WIDTH * BIO_CELL_COUNT;
            
            int nodeX = node.getX(), nodeY = node.getY(), neighborX, neighborY;
            
            List<BioNode> neighbors = new ArrayList<>();
            BioNode neighbor = null;
            
            final int X_BOUNDS = (nodeX - (BIO_CELL_COUNT / 2)) + BIO_CELL_COUNT;
            final int Y_BOUNDS = (nodeY - (BIO_CELL_COUNT / 2)) + BIO_CELL_COUNT;
            
            for (int x = (nodeX - 1); x < X_BOUNDS; x++) {
                for (int y = (nodeY - 1); y < Y_BOUNDS; y++) {

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
                    
                    if (neighborCell != parentCell){
                        neighbor = neighborCell.getBioSurface().getCellAt(neighborX, neighborY);
                    }else{
                        neighbor = getCellAt(neighborX, neighborY);
                    }
                    
                    if ((neighbor != node) && neighbor.hasLife()) {
                        neighbors.add(neighbor);
                    }
                }
            }
            
            return neighbors.toArray(new BioNode[neighbors.size()]);
        }
        
        @Override
        public boolean check() {
            return surfaceHasLife();
        }

    }
    
}
