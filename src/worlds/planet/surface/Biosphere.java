

package worlds.planet.surface;

import engine.util.Delay;
import engine.util.Task;
import engine.util.TaskFactory;

/**
 *
 * @author Richard DeSilvey
 */
public abstract class Biosphere extends Hydrosphere {

    public Biosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        produceTasks(new BioCellTaskFactory());
    }

    private class BioCellTaskFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new BioCellTask();
        }
    
        private class BioCellTask implements Task {

            private Delay delay;
            
            public BioCellTask() {
                delay = new Delay();
            }
            
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
}
