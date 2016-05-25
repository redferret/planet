
package planet.util;

/**
 * A TaskFactory is an object factory that produces Task objects. This
 * allows for each SurfaceThread of have it's own instance of a Task because
 * that Task may have it's own instance variables the need to be unique to
 * each thread.
 * @author Richard DeSilvey
 */
public interface TaskFactory {
    Task buildTask();
}
