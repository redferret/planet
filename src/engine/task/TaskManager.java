package engine.task;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Richard DeSilvey
 */
public class TaskManager {

  private final ConcurrentLinkedQueue<Task> tasks;
  protected Boundaries bounds;

  public TaskManager(Boundaries bounds) {
    tasks = new ConcurrentLinkedQueue<>();
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

        for (int x = lowerXBound; x < upperXBound; x++) {
          for (int y = lowerYBound; y < upperYBound; y++){
            task.perform(x, y);
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
