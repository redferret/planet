

package planet.surface;

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
    }

    @Override
    public void partialUpdate(int x, int y) {
        updateGeology(x, y);
        updateOceans(x, y);
    }

    @Override
    public void fullUpdate() {
        if (!suppressMantelHeating || checkForGeologicalUpdate()){
            heatMantel();
        }
    }

    
}
