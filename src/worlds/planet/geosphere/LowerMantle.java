
package worlds.planet.geosphere;

import engine.surface.SurfaceMap;
import engine.surface.SurfaceThreads;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.PlanetCell;
import worlds.planet.Util;
import worlds.planet.geosphere.tasks.LowerMantleConduction;

/**
 *
 * @author Richard
 */
public class LowerMantle extends SurfaceMap<Mantle> {
  
  public final static float LOWER_MANTLE_DEPTH = 2665f;
  public final static float LOWER_MANTLE_MASS = Util.calcMass(LOWER_MANTLE_DEPTH, PlanetCell.area, 4400f);
  public static final float LOWER_MANTLE_DENSITY = 3500f;
  public static final float LOWER_MANTLE_SPECIFIC_HEAT = 4.6f;

  public LowerMantle(int totalSize, SurfaceThreads surfaceThreads) {
    super("Lower Mantle", totalSize, surfaceThreads);
    setupDefaultMap(surfaceThreads.getThreadCount());
    reset();
  }

  public void setDependentSurfaces(UpperMantle upperMantle, Core core) {
    getSurfaceThreads().produceTasks(() -> {
      return new LowerMantleConduction(this, upperMantle, core);
    });
  }
  
  @Override
  public Mantle generateCell(int x, int y) {
    return new Mantle(x, y, ThreadLocalRandom.current().nextInt(3000, 4000)) {
      @Override
      public float getHeatConductivity() {
        return LOWER_MANTLE_SPECIFIC_HEAT;
      }
    };
  }
  
}
