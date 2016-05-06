

package planet.surface;

import planet.util.Boundaries;
import planet.util.SurfaceThread;

/**
 * The highest level of abstraction for the surface of a planet.
 * @author Richard DeSilvey
 */
public class PlanetSurface extends Hydrosphere {

    public PlanetSurface(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
    }

    @Override
    public SurfaceThread generateSurfaceThread(int delay, Boundaries bounds, String name) {
        return new SurfaceThread(delay, bounds, name, this);
    }
}
