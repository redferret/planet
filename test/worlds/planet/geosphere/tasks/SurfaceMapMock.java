
package worlds.planet.geosphere.tasks;

import engine.surface.SurfaceMap;
import engine.surface.SurfaceThreads;

/**
 *
 * @author Richard
 */
public class SurfaceMapMock extends SurfaceMap<MockCell> {

  
  public SurfaceMapMock(int totalSize, SurfaceThreads surfaceThreads) {
    super("Mock", totalSize + 1, surfaceThreads);
    setupDefaultMap(1);
    buildMap();
  }

  @Override
  public MockCell generateCell(int x, int y) {
    return new MockCell(x, y, 500, 2.5f);
  }
  
  
}
