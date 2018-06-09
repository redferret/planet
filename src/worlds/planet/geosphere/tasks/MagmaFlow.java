
package worlds.planet.geosphere.tasks;

import engine.util.task.CompoundTask;
import engine.util.task.TaskAdapter;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Lithosphere;

/**
 *
 * @author Richard
 */
public class MagmaFlow extends CompoundTask {

  private final Lithosphere crust;
  
  public MagmaFlow(Lithosphere crust) {
    this.crust = crust;
  }

  @Override
  public void setup() {
    addSubTask(new UpdateVelocities());
    addSubTask(new ApplyVelocities());
  }

  /**
   * Updates the acceleration for the magma under the crust
   */
  private class UpdateVelocities extends TaskAdapter {
    @Override
    public void perform(int x, int y) throws Exception {
      Crust crustCell = crust.getCellAt(x, y);
      // Update the magma flow under the crust
    }
  }

  /**
   * Applies the acceleration to the velocity of the magma
   */
  private class ApplyVelocities extends TaskAdapter {
    @Override
    public void perform(int x, int y) throws Exception {
      Crust crustCell = crust.getCellAt(x, y);
      crustCell.updateMagmaVelocity();
    }
  }

}
