

package planet.surface;

import planet.Planet;
import planet.cells.PlanetCell;
import planet.util.Delay;
import planet.util.Task;
import planet.util.TaskFactory;

/**
 *
 * @author Richard DeSilvey
 */
public class Atmosphere extends Hydrosphere {

    public Atmosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        produceTasks(new EvaporateFactory());
    }

    private class EvaporateFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new EvaporateTask();
        }
        
        private class EvaporateTask implements Task {

            private Delay delay;

            public EvaporateTask() {
                delay = new Delay(20);
            }
            
            @Override
            public void perform(int x, int y) {
                if (y != 0){
                    PlanetCell cell = getCellAt(x, y);
                    float w = Planet.self().getGridWidth();
                    float h = w / 2;
                    float rate = 0;

                    if (0 <= y && y < h){
                        rate = y / h;
                    }else if (h <= y && y < w){
                        rate = (w - y) / h;
                    }
                    final int a = 50;
                    cell.addOceanMass(- (rate * a));
                }
            }

            @Override
            public boolean check() {
                return delay.check();
            }
            
        }
        
    }
    
}
