
package worlds.planet.geosphere;

import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.surface.SurfaceThreads;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.PlanetCell;
import worlds.planet.Util;
import worlds.planet.geosphere.tasks.CoreConduction;

/**
 *
 * @author Richard
 */
public class Core extends SurfaceMap {

  public static final float CORE_DEPTH = 1.22e6f;
  public static final float CORE_MASS = Util.calcMass(CORE_DEPTH, PlanetCell.area, 7860f);
  
  public Core(int totalSize, SurfaceThreads surfaceThreads) {
    super("Core", totalSize, surfaceThreads);
    setupDefaultMap(surfaceThreads.getThreadCount());
    reset();
  }

  public void setupConduction(UpperMantle upperMantle) {
    getSurfaceThreads().produceTasks(() -> {
      return new CoreConduction(this, upperMantle);
    });
  }
  
  @Override
  public Cell generateCell(int x, int y) {
    return new Cell(x, y, ThreadLocalRandom.current().nextInt(2000, 3200)) {
      @Override
      public float getHeatCapacity() {
        return 45.0f;
      }

      public float getZLength() {
        return CORE_DEPTH;
      }
      
      @Override
      public float getBottomResistence() {
        return 0;
      }
      
      @Override
      public float getHorizontalResistence() {
        return 0;
      }

      @Override
      public float getTopResistence() {
        return 0;
      }
      
    };
  }
  
}
