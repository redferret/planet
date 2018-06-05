
package worlds.planet.geosphere.tasks;

import engine.surface.SurfaceMap;
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
  public void before() throws Exception {}

  @Override
  public void perform(int x, int y) throws Exception {
    Mantle bottom = upperMantle.getCellAt(x, y);
    Crust crust = surface.getCellAt(x, y);
    setNewTemperature(x, y, crust.getStrataThickness(), null, bottom);
  }

  @Override
  public void after() throws Exception {}
  
}
