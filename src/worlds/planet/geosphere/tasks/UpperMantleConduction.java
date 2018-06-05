
package worlds.planet.geosphere.tasks;

import engine.surface.Cell;
import worlds.planet.geosphere.Lithosphere;
import worlds.planet.geosphere.LowerMantle;
import worlds.planet.geosphere.Mantle;
import worlds.planet.geosphere.UpperMantle;
import static worlds.planet.geosphere.UpperMantle.UPPER_MANTLE_DEPTH;

/**
 *
 * @author Richard
 */
public class UpperMantleConduction extends Conduction<Mantle> {

  private final LowerMantle lowerMantle;
  private final Lithosphere lithosphere;
  
  public UpperMantleConduction(UpperMantle surface, LowerMantle lowerMantle,
          Lithosphere lithosphere) {
    super(surface);
    this.lithosphere = lithosphere;
    this.lowerMantle = lowerMantle;
  }
  
  @Override
  public void before() throws Exception {
  }

  @Override
  public void perform(int x, int y) throws Exception {
    Mantle top = lowerMantle.getCellAt(x, y);
    Cell bottom = lithosphere.getCellAt(x, y);
    setNewTemperature(x, y, UPPER_MANTLE_DEPTH, top, bottom);
  }

  @Override
  public void after() throws Exception {
  }
  
}
