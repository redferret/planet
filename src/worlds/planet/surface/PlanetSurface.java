

package worlds.planet.surface;


/**
 * The highest level of abstraction for the surface of a planet.
 * @author Richard DeSilvey
 */
public class PlanetSurface extends Atmosphere {

    public static boolean suppressMantelHeating;
    public static boolean suppressAtmosphere;
    
    static {
        suppressMantelHeating = false;
        suppressAtmosphere = false;
    }
    
    public PlanetSurface(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        
    }
}
