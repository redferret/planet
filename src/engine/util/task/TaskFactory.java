package engine.util.task;

/**
 * A TaskFactory is an object factory that produces Task objects. This allows
 * for each SurfaceThread of have it's own instance of a Task because that Task
 * may have it's own instance variables that need to be unique to each thread.
 *
 * @author Richard DeSilvey
 */
public interface TaskFactory {

  /**
   * A Task is a Resource for a SurfaceThread to use to make updates to the data
   * in a SurfaceMap. These resources are of type Task.
   *
   * @return A new instance of a task.
   */
  Task buildTask();
}
