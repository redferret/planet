
package worlds.planet.geosphere.tasks;

import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.surface.SurfaceThreads;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Richard
 */
public class SurfaceMapMock extends SurfaceMap<MockCell> {

  
  public SurfaceMapMock(int totalSize, SurfaceThreads surfaceThreads) {
    super(totalSize + 1, surfaceThreads);
  }

  @Override
  public MockCell generateCell(int x, int y) {
    return null;
  }

  @Override
  public MockCell getCellAt(int x, int y) {
    return new MockCell(x, y, ThreadLocalRandom.current().nextInt(1000, 5000), 
    ThreadLocalRandom.current().nextFloat() * 5f);
  }
  
  
}
