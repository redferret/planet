
package worlds.planet.geosphere.tasks;

import engine.surface.Cell;
import engine.surface.SurfaceMap;
import worlds.planet.geosphere.Core;
import static worlds.planet.geosphere.LowerMantle.LOWER_MANTLE_DEPTH;
import worlds.planet.geosphere.Mantle;
import worlds.planet.geosphere.UpperMantle;

/**
 *
 * @author Richard
 */
public class LowerMantleConduction extends Conduction<Mantle> {

  private final UpperMantle upperMantle;
  private final Core core;
  
  public LowerMantleConduction(SurfaceMap surface, UpperMantle upperMantle,
          Core core) {
    super(surface);
    this.upperMantle = upperMantle;
    this.core = core;
  }

  @Override
  public void before() throws Exception {}

  @Override
  public void perform(int x, int y) throws Exception {
    Mantle top = upperMantle.getCellAt(x, y);
    Cell bottom = core.getCellAt(x, y);
    setNewTemperature(x, y, LOWER_MANTLE_DEPTH, top, bottom);
  }

  @Override
  public void after() throws Exception {}
  
}
