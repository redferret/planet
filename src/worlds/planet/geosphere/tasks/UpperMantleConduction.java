
package worlds.planet.geosphere.tasks;

import worlds.planet.geosphere.Mantle;
import worlds.planet.geosphere.UpperMantle;

/**
 *
 * @author Richard
 */
public class UpperMantleConduction extends Conduction<Mantle> {

  public UpperMantleConduction(UpperMantle surface) {
    super(surface);
  }
  
  @Override
  public void before() throws Exception {
  }

  @Override
  public void perform(int x, int y) throws Exception {
    Mantle upperMantle = surface.getCellAt(x, y);
    
  }

  @Override
  public void after() throws Exception {
  }
  
}
