
package worlds.planet.geosphere.tasks;

import engine.surface.SurfaceMap;
import worlds.planet.Util;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Mantle;
import worlds.planet.geosphere.UpperMantle;

/**
 *
 * @author Richard
 */
public class CrustConduction extends Conduction<Crust> {

  private final UpperMantle upperMantle;
  
  public CrustConduction(SurfaceMap surface, UpperMantle upperMantle) {
    super(surface);
    this.upperMantle = upperMantle;
  }

  @Override
  public void perform(int x, int y) throws Exception {
    Mantle bottom = upperMantle.getCellAt(x, y);
    setNewTemperature(x, y, null, bottom);
  }

}
