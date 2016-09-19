

package engine.util.task;

/**
 * TaskAdapter will have the check() method always return true. The check()
 * method can't be overridden.
 * @author Richard DeSilvey
 */
public abstract class TaskAdapter extends Task {
	public final void construct(){};
    @Override
    public final boolean check() {
        return true;
    }
}
