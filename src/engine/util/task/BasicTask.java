

package engine.util.task;

/**
 * A BasicTask is a task performed without updating all the cells on a map and
 * is similar to the TaskAdapter except the <code>check()</code> always returns
 * false and calls the new method <code>perform()</code>
 * @author Richard DeSilvey
 */
public abstract class BasicTask extends Task {

    @Override
    public final void perform(int x, int y) {
    }
    
    /**
     * Implement this method to perform this task. before() and after() are called
 to their respective order for perform(x, y).
     */
    public abstract void perform();
    
    @Override
    public final boolean check() {
        before();
        perform();
        after();
        return false;
    }

    
}
