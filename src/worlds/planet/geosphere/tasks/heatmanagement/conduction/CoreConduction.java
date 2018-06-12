
package worlds.planet.geosphere.tasks.heatmanagement.conduction;

import engine.surface.SurfaceMap;
import worlds.planet.geosphere.Mantle;
import worlds.planet.geosphere.UpperMantle;

/**
 *
 * @author Richard
 */
public class CoreConduction extends Conduction {

  private final UpperMantle upperMantle;
  
  public CoreConduction(SurfaceMap surface, UpperMantle lowerMantle) {
    super(surface);
    this.upperMantle = lowerMantle;
  }

  @Override
  public void perform(int x, int y) throws Exception {
    Mantle top = upperMantle.getCellAt(x, y);
    setNewTemperature(x, y, top, null);
  }
}
