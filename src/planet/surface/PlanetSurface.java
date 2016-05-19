

package planet.surface;

import planet.util.Delay;

/**
 * The highest level of abstraction for the surface of a planet.
 * @author Richard DeSilvey
 */
public class PlanetSurface extends Hydrosphere {

    public static boolean suppressMantelHeating;
    private Delay mantelHeatingDelay, geologicDelay;
    
    static {
        suppressMantelHeating = false;
    }
    
    public PlanetSurface(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        mantelHeatingDelay = new Delay(100);
        geologicDelay = new Delay(2);
    }

    @Override
    public void partialUpdate(int x, int y) {
        if (geologicDelay.check()){
            updateGeology(x, y);
        }
        updateRockFormation(x, y);
        updateOceans(x, y);
    }

    @Override
    public void fullUpdate() {
        if (mantelHeatingDelay.check()){
            if (!suppressMantelHeating || checkForGeologicalUpdate()){
                heatMantel();
            }
        }
    }

    
}
