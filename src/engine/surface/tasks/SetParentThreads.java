
package engine.surface.tasks;

import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.task.TaskAdapter;

/**
 * Sets up the surface further by setting each cell the parent thread.
 */
public class SetParentThreads extends TaskAdapter {

  private final SurfaceMap surface;
  
  public SetParentThreads(SurfaceMap surface) {
    this.surface = surface;
  }

  @Override
  public void before() {
    this.singleTask = true;
  }

  @Override
  public void perform(int x, int y) throws Exception {
    Cell c = surface.getCellAt(x, y);
    c.setParentThread(SetParentThreads.this.getThread());
  }

  @Override
  public void after() {
  }
}
