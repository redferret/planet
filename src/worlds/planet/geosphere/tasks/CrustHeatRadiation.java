
package worlds.planet.geosphere.tasks;

import engine.util.Delay;
import engine.util.task.TaskAdapter;
import worlds.planet.Util;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Lithosphere;

/**
 *
 * @author Richard
 */
public class CrustHeatRadiation extends TaskAdapter {

  private final Lithosphere surface;
  private final Delay delay;

  public CrustHeatRadiation(Lithosphere surface) {
    this.surface = surface;
    delay = new Delay(1000);
  }

  @Override
  public boolean check() {
    return delay.check();
  }
  
  @Override
  public void before() throws Exception {}

  @Override
  public void perform(int x, int y) throws Exception {
    Crust crust = surface.getCellAt(x, y);
    float flux = Util.calcHeatRadiation(crust.getTemperature());
    float change = Util.calcChangeInTemperature(flux, crust.getHeatCapacity(), crust.getTotalMass());
    crust.addToTemperature(-change);
  }

  @Override
  public void after() throws Exception {}
  
}
