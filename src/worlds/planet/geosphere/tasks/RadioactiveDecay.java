
package worlds.planet.geosphere.tasks;

import engine.util.task.TaskAdapter;
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
  private float joules;

  public RadioactiveDecay(Lithosphere surface, UpperMantle upperMantle) {
    this.surface = surface;
    this.upperMantle = upperMantle;
    joules = 2e13f;
  }
  
  @Override
  public void before() throws Exception {}

  @Override
  public void perform(int x, int y) throws Exception {
    if (ThreadLocalRandom.current().nextFloat() < 0.0002f) {
      Crust crust = surface.getCellAt(x, y);
      float change = (joules * 0.01f) / (crust.getHeatCapacity() * crust.getTotalMass());
      crust.addToTemperature(change);
      
    }
    if (ThreadLocalRandom.current().nextFloat() < 0.0002f) {
      Mantle mantle = upperMantle.getCellAt(x, y);
      float change = (joules) / (mantle.getHeatCapacity() * UpperMantle.UPPER_MANTLE_MASS);
      mantle.addToTemperature(change);
      
    }
  }

  @Override
  public void after() throws Exception {}

}
