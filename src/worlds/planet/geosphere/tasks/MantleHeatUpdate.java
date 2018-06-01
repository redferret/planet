
package worlds.planet.geosphere.tasks;

import engine.util.Delay;
import engine.util.task.Task;
import worlds.planet.geosphere.GeoCell;
import worlds.planet.geosphere.Geosphere;

/**
 *
 * @author Richard
 */
public class MantleHeatUpdate extends Task {

  private final Geosphere surface;
  private final Delay delay;
  
  public static int delayHeatUpdate = 1;
  
  public MantleHeatUpdate(Geosphere surface) {
    this.surface = surface;
    delay = new Delay(delayHeatUpdate);
  }
  
  @Override
  public void construct() {}

  @Override
  public boolean check() throws Exception {
    return delay.check();
  }

  @Override
  public void before() throws Exception {
  }

  @Override
  public void perform(int x, int y) throws Exception {
    GeoCell cell = surface.getCellAt(x, y);
    float acc = cell.getTemperatureAcc();
    cell.applyTemperatureFlux(acc);
    float flux = cell.getTemperatureFlux();
    cell.addToMantleHeat(-flux);
    cell.zeroTemperatureAcc();
  }

  @Override
  public void after() throws Exception {
  }
  
}
