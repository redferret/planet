

package planet.surface;

import planet.util.Delay;
import planet.util.Task;
import planet.util.TaskAdapter;
import planet.util.TaskFactory;

/**
 * The highest level of abstraction for the surface of a planet.
 * @author Richard DeSilvey
 */
public class PlanetSurface extends Hydrosphere {

    public static boolean suppressMantelHeating;
    
    static {
        suppressMantelHeating = false;
    }
    
    public PlanetSurface(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        
        produceTasks(new GeologicalUpdateFactory());
        addTask(new HeatMantel());
        addTaskToThreads(new RockFormation());
    }

    private class GeologicalUpdateFactory implements TaskFactory {
        @Override
        public Task buildTask() {
            return new GeologicalUpdate();
        }
    }
    
    private class GeologicalUpdate implements Task {
        private Delay geologicDelay;
        
        public GeologicalUpdate() {
            geologicDelay = new Delay(5);
        }
        @Override
        public void perform(int x, int y){
            updateGeology(x, y);
        }
        @Override
        public boolean check(){
            return geologicDelay.check();
        }
    }
    
    private class HeatMantel implements Task {
        
        private Delay mantelHeatingDelay;
        
        public HeatMantel() {
            mantelHeatingDelay = new Delay(125);
        }
        
        @Override
        public void perform(int x, int y) {}

        @Override
        public boolean check() {
            if (mantelHeatingDelay.check()){
                if (!suppressMantelHeating) {
                    heatMantel();
                }
            }
            return false;
        }
        
    }
    
    private class RockFormation extends TaskAdapter {
        @Override
        public void perform(int x, int y) {
            updateRockFormation(x, y);
            updateOceans(x, y);
        }
    }
}
