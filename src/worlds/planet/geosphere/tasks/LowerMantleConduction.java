
package worlds.planet.geosphere.tasks;

import worlds.planet.geosphere.Lithosphere;
import worlds.planet.geosphere.Mantle;

/**
 *
 * @author Richard
 */
public class LowerMantleConduction extends Conduction<Mantle> {

  public LowerMantleConduction(Lithosphere surface) {
    super(surface);
  }

  @Override
  public void before() throws Exception {}

  @Override
  public void perform(int x, int y) throws Exception {
    Mantle lowerMantle = surface.getCellAt(x, y);

  }

  @Override
  public void after() throws Exception {}
  
}
