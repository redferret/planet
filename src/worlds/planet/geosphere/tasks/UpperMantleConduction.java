
package worlds.planet.geosphere.tasks;

import engine.surface.Cell;
import worlds.planet.geosphere.Lithosphere;
import worlds.planet.geosphere.Core;
import worlds.planet.geosphere.Mantle;
import worlds.planet.geosphere.UpperMantle;

/**
 *
 * @author Richard
 */
public class UpperMantleConduction extends Conduction<Mantle> {

  private final Core core;
  private final Lithosphere lithosphere;
  
  public UpperMantleConduction(UpperMantle surface, Core core,
          Lithosphere lithosphere) {
    super(surface);
    this.lithosphere = lithosphere;
    this.core = core;
  }
  
  @Override
  public void before() throws Exception {
  }

  @Override
  public void perform(int x, int y) throws Exception {
    Cell bottom = core.getCellAt(x, y);
    Cell top = lithosphere.getCellAt(x, y);
    setNewTemperature(x, y, top, bottom);
  }

  @Override
  public void after() throws Exception {
  }
  
}
