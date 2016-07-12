

package worlds.planet.cells.biology;

import engine.surface.SurfaceMap;
import engine.util.Boundaries;
import engine.util.Task;
import engine.util.TaskManager;

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
            // Update life forms
            int parentX = parentCell.getX();
            int parentY = parentCell.getY();
            BioNode node = getCellAt(x, y);
        }

        @Override
        public boolean check() {
            return cellCount > 0;
        }

    }
    
}
