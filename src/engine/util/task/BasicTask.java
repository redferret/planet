package engine.util.task;

/**
 * A BasicTask is a task performed without updating all the cells on a map and
 * is similar to the TaskAdapter except the <code>check()</code> always returns
 * false and calls the new method <code>perform()</code>. Also the
 * <code>construct()</code> method doesn't need to be implemented but can be
 * overridden.
 *
 * @author Richard DeSilvey
 */
public abstract class BasicTask extends Task {

    public void construct() {
        // Basic Tasks don't need to be constructed
    }

    @Override
    public final void perform(int x, int y) {
    }

    /**
     * Implement this method to perform this task. before() and after() are
     * invoked in their respective order as normal.
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
