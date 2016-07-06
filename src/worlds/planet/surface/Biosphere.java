

package worlds.planet.surface;

import engine.util.Task;

/**
 *
 * @author Richard DeSilvey
 */
public class Biosphere extends Hydrosphere {

    public Biosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        addTask(new BioCellTask());
    }

    private class BioCellTask implements Task {

        @Override
        public void perform(int x, int y) {
            getCellAt(x, y).updateBiology();
        }

        @Override
        public boolean check() {
            return true; // For now
        }
        
    }
    
}
