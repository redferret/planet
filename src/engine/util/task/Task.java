package engine.util.task;

import engine.surface.Cell;
import engine.util.concurrent.MThread;

/**
 * A Task is something the simulation will perform given a condition.
 *
 * @author Richard DeSilvey
 */
public abstract class Task {

  /**
   * A public, mutable, reference to the thread working on this task. If this
   * object reference is null then this task is not part of a TaskFactory.
   */
  private MThread parentThread = null;
  
  /**
   * Marks this Task as being a single task to run only once. By default
   * this is set to false.
   */
  protected boolean singleTask = false;

  public void setThread(MThread thread) {
    parentThread = thread;
  }

  public MThread getThread() {
    return parentThread;
  }

  public boolean isNotChildCell(Cell cell) {
    return cell.getParentThread() != parentThread;
  }

  public boolean isSingleTask() {
    return singleTask;
  }
  
  /**
   * This method is called when a task is added to a TaskManager. This method is
   * used because in some instances the rest of the framework hasn't been
   * initialized yet. CompoundTasks that add subtasks would use this method
   * instead of a constructor, otherwise the constructor is called then
   * this method is called.
   */
  public abstract void construct();

  /**
   * This method is called before updating each cell on the surface. If this
   * method returns false then perform on the current frame won't be called.
   *
   * @return true if <code>perform(x, y)</code> is to be invoked, false will
   * skip the current frame.
   * @throws java.lang.Exception
   */
  public abstract boolean check() throws Exception;

  /**
   * An optional before-processing method called before all calls to perform(x,
   * y) or the single call to perform() finishes.
   */
  public abstract void before() throws Exception;

  /**
   * This method will be called for each cell on the surface. This method will
   * only be called if <code>check()</code> returns true. Both x and y are
   * bounded by [0, w) where w is the width of the map.
   *
   * @param x The x coordinate of the cell.
   * @param y The y coordinate of the cell.
   */
  public abstract void perform(int x, int y) throws Exception;

  /**
   * An optional after-processing method called after all calls to perform(x, y)
   * or the single call to perform() finishes.
   */
  public abstract void after() throws Exception;

}
