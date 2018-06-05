
package worlds.planet.geosphere.tasks;

import engine.util.task.TaskAdapter;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Lithosphere;


/**
 *
 * @author Richard
 */
public class RemoveHeatTest extends TaskAdapter {

  private final Lithosphere surface;

  public RemoveHeatTest(Lithosphere surface) {
    this.surface = surface;
  }
  
  @Override
  public void before() throws Exception {}

  @Override
  public void perform(int x, int y) throws Exception {
    if (ThreadLocalRandom.current().nextFloat() < 0.0002f) {
      Crust cell = surface.getCellAt(x, y);
      cell.addToTemperature(-0);
    }
  }

  @Override
  public void after() throws Exception {}

}
