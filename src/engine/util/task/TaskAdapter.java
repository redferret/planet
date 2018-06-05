package engine.util.task;

/**
 * TaskAdapter will have the check() method always return true. The check()
 * method can be overridden.
 *
 * @author Richard DeSilvey
 */
public abstract class TaskAdapter extends Task {

  @Override
  public void construct() {}
    
  @Override
  public boolean check() {
    return true;
  }
}
