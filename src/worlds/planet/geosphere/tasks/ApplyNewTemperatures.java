
package worlds.planet.geosphere.tasks;

import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.task.TaskAdapter;

/**
 *
 * @author Richard
 */
public class ApplyNewTemperatures extends TaskAdapter {
  private final SurfaceMap surface;

  public ApplyNewTemperatures(SurfaceMap surface) {
    this.surface = surface;
  }
  
  @Override
  public void perform(int x, int y) throws Exception {
    Cell cell = surface.getCellAt(x, y);
    cell.updateTemperature();
  }

}
