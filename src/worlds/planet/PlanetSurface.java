package worlds.planet;

import worlds.planet.geosphere.Geosphere;


/**
 * The highest level of abstraction for the surface of a planet.
 *
 * @author Richard DeSilvey
 */
public class PlanetSurface extends Geosphere {

  public static boolean suppressMantelHeating;
  public static boolean suppressAtmosphere;

  static {
    suppressMantelHeating = false;
    suppressAtmosphere = false;
  }

  public PlanetSurface(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
    super(worldSize, surfaceDelay, threadsDelay, threadCount);

  }

  @Override
  public boolean load() {
    return false;
  }
}
