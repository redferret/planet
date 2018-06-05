
package worlds.planet.geosphere;

import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.surface.SurfaceThreads;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.geosphere.tasks.CoreConduction;

/**
 *
 * @author Richard
 */
public class Core extends SurfaceMap {

  public Core(int totalSize, SurfaceThreads surfaceThreads) {
    super("Core", totalSize, surfaceThreads);
    setupDefaultMap(surfaceThreads.getThreadCount());
    reset();
  }

  public void setDependentSurface(LowerMantle lowerMantle) {
    getSurfaceThreads().produceTasks(() -> {
      return new CoreConduction(this, lowerMantle);
    });
  }
  
  @Override
  public Cell generateCell(int x, int y) {
    return new Cell(x, y, ThreadLocalRandom.current().nextInt(4000, 6000)) {
      @Override
      public float getHeatCapacity() {
        return 45.0f;
      }

      @Override
      public float getVerticalResistence() {
        return 1e2f;
      }
      
      @Override
      public float getHorizontalResistence() {
        return 2e8f;
      }
      
      @Override
      public float topNullConducance() {
        return 0;
      }
      
      @Override
      public float bottomNullConductance() {
        return 0;
      }
    };
  }
  
}
