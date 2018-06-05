
package worlds.planet.geosphere.tasks;

import engine.surface.SurfaceMap;
import worlds.planet.geosphere.LowerMantle;
import worlds.planet.geosphere.Mantle;

/**
 *
 * @author Richard
 */
public class CoreConduction extends Conduction {

  private final LowerMantle lowerMantle;
  
  public CoreConduction(SurfaceMap surface, LowerMantle lowerMantle) {
    super(surface);
    this.lowerMantle = lowerMantle;
  }

  @Override
  public void before() throws Exception {}

  @Override
  public void perform(int x, int y) throws Exception {
    Mantle top = lowerMantle.getCellAt(x, y);
    setNewTemperature(x, y, 1.22e6f, top, null);
  }

  @Override
  public void after() throws Exception {}
  
}
