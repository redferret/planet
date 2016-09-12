

package worlds.planet.surface;

import engine.util.Delay;
import engine.util.task.Task;
import engine.util.task.TaskFactory;
import worlds.planet.cells.PlanetCell;

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
    
        private class BioCellTask extends Task {

            private Delay delay;
            
            public BioCellTask() {
                delay = new Delay(5);
            }
            
            @Override
            public void perform(int x, int y) {
                PlanetCell cell = waitForCellAt(x, y);
                cell.updateBiology();
                release(cell);
            }

            @Override
            public boolean check() {
                return delay.check();
            }

            @Override
            public void before() {
            }

            @Override
            public void after() {
            }

        }
    }
}
