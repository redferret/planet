

package engine.util;

/**
 * A BasicTask is a task performed without updating all the cells on a map and
 * is similar to the TaskAdapter except the <code>check()</code> always returns
 * false and calls the new method <code>perform()</code>
 * @author Richard DeSilvey
 */
public abstract class BasicTask implements Task {

    @Override
    public final void perform(int x, int y) {
    }
    
    /**
     * Implement this method to perform this task.
     */
    public abstract void perform();
    
    @Override
    public final boolean check() {
        perform();
        return false;
    }

    
}
