package worlds.planet.geosphere;

import engine.surface.SurfaceMap;
import engine.surface.SurfaceThreads;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.PlanetCell;
import worlds.planet.Util;

/**
 * The mantle is below the crust, heat and magma that build up to critical
 * points below the crust cause volcanoes to erupt and aid in the movement 
 * of plate tectonics.
 *
 * @author Richard DeSilvey
 */
public class UpperMantle extends SurfaceMap<Mantle> {

  /**
   * The average density of the mantel. The units are in kilograms per cubic
   * meter.
   */
  public final static float UPPER_MANTLE_DEPTH = 430f;
  public static final float UPPER_MANTLE_DENSITY = 3500f;
  public final static float UPPER_MANTLE_MASS = Util.calcMass(UPPER_MANTLE_DEPTH, PlanetCell.area, UPPER_MANTLE_DENSITY);
  public static final float UPPER_MANTLE_SPECIFIC_HEAT = 2.6f;

  public UpperMantle(int totalSize, SurfaceThreads surfaceThreads) {
    super("Upper Mantle", totalSize, surfaceThreads);
    setupDefaultMap(surfaceThreads.getThreadCount());
    reset();
  }

  @Override
  public Mantle generateCell(int x, int y) {
    return new Mantle(x, y, ThreadLocalRandom.current().nextInt(500, 1000)) {
      @Override
      public float getHeatConductivity() {
        return UPPER_MANTLE_SPECIFIC_HEAT;
      }
    };
  }
  

}
