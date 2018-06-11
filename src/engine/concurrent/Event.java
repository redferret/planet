
package engine.concurrent;

/**
 * An Event is similar to a Task but it is consumable. An event must be
 * created for another thread that owns a cell not belonging to the thread
 * that creates the Event. These Events are simple, such as a cell receiving
 * sediments or heat from a neighboring cell. 
 * @author Richard
 */
public interface Event {
  /**
   * This Event is processed by the parent thread and created by a neighboring
   * thread.
   */
  public void execute();
}
