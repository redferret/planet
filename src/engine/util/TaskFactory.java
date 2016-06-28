
package engine.util;

/**
 * A TaskFactory is an object factory that produces Task objects. This
 * allows for each SurfaceThread of have it's own instance of a Task because
 * that Task may have it's own instance variables that need to be unique to
 * each thread.
 * @author Richard DeSilvey
 */
public interface TaskFactory {
    
    /**
     * This method should return a new instance of a task.
     * @return A new instance of a task.
     */
    Task buildTask();
}
