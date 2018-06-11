
package worlds.planet.geosphere.tasks;

import engine.task.TaskAdapter;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Lithosphere;
import worlds.planet.geosphere.Mantle;
import worlds.planet.geosphere.UpperMantle;


/**
 *
 * @author Richard
 */
public class RadioactiveDecay extends TaskAdapter {

  private final Lithosphere surface;
  private final UpperMantle upperMantle;
  private float joules, prob;

  public RadioactiveDecay(Lithosphere surface, UpperMantle upperMantle) {
    this.surface = surface;
    this.upperMantle = upperMantle;
    prob = 1f / surface.getTotalNumberOfCells();
    joules = 2e13f;
  }
  @Override
  public void perform(int x, int y) throws Exception {
//    if (ThreadLocalRandom.current().nextFloat() < prob) {
//      Crust crust = surface.getCellAt(x, y);
//      float change = (joules * 0.01f) / (crust.getHeatCapacity() * crust.getTotalMass());
//      crust.addToTemperature(change);
//      
//    }
//    if (ThreadLocalRandom.current().nextFloat() < prob) {
//      Mantle mantle = upperMantle.getCellAt(x, y);
//      float change = (joules * 2.0f) / (mantle.getHeatCapacity() * UpperMantle.UPPER_MANTLE_MASS);
//      mantle.addToTemperature(change);
//      
//    }
  }
}
