package engine.util.task;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Richard DeSilvey
 */
public class TaskManager {

  private final List<Task> tasks;
  protected Boundaries bounds;

  public TaskManager(Boundaries bounds) {
    tasks = new ArrayList<>();
    this.bounds = bounds;
  }

  /**
   * Adding a task to a manager will invoke the Task's 'construct' method.
   *
   * @param task The task being added to this manager.
   */
  public void addTask(Task task) {
    task.construct();
    tasks.add(task);
  }

  public Boundaries getBounds() {
    return bounds;
  }

  public void performTasks() throws Exception {
    int lowerYBound = bounds.getLowerYBound();
    int upperYBound = bounds.getUpperYBound();
    int lowerXBound = bounds.getLowerXBound();
    int upperXBound = bounds.getUpperXBound();

    for (Task task : tasks) {
      if (task.check()) {
        task.before();

        int ystart = lowerYBound;
        int yinc = 1;

        for (int b = 0; b < 2; b++) {
          for (int y = ystart; (y < upperYBound); y += yinc) {

            int m = ((b > 0) && (y % 2 == 0)) ? lowerXBound + 1
                    : ((b > 0) && (y % 2 != 0)
                            ? lowerXBound - 1 : lowerXBound);

            for (int x = (y % 2) + m; x < upperXBound; x += 2) {
              task.perform(x, y);
            }
          }
        }
        task.after();
      }
    }
  }

  public void trimTasks() {
    tasks.removeIf(task -> task.isSingleTask());
  }
}
