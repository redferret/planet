

package worlds.planet.cells;

import engine.surface.SurfaceMap;
import engine.util.Boundaries;
import engine.util.Task;
import engine.util.TaskManager;
import java.util.List;

/**
 * The Biosphere is represented by this cell. The biosphere includes all life
 * forms and their impact on the environment.
 * @author Richard DeSilvey
 */
public class BioCell extends HydroCell {

    private BioSurface bioSurface;
    
    
    public BioCell(int x, int y) {
        super(x, y);
        bioSurface = new BioSurface();
    }
    
    public void updateBiology(){
        bioSurface.update();
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        
        return super.render(settings);
    }
    
}

class BioSurface extends SurfaceMap<BioNode> {

    private static final int BIO_CELL_COUNT = 3, THREAD_COUNT = 1,
            BIO_TOTAL_COUNT = BIO_CELL_COUNT * BIO_CELL_COUNT;
    private static final String THREAD_NAME = "Bio Surface";
    
    /**
     * The number of cells that are currently active. If the count is zero then
     * this section of BioNodes are not updated.
     */
    private int cellCount;
    private TaskManager manager;
    
    public BioSurface() {
        super(BIO_CELL_COUNT, 0, THREAD_NAME, THREAD_COUNT);
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
        }

        @Override
        public boolean check() {
            return cellCount > 0;
        }
        
    }
    
}