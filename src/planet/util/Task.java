

package planet.util;

/**
 * A Task is something the simulation will perform given a condition.
 * @author Richard DeSilvey
 */
public interface Task {
    /**
     * This method will be called for each cell on the surface. This method
     * will only be called if <code>check()</code> returns true.
     * @param x The x coordinate of the cell.
     * @param y The y coordinate of the cell.
     */
    void perform(int x, int y);
    
    /**
     * This method is called before updating each cell on the surface. If this
     * method returns false then perform on the current frame won't be called.
     * 
     * @return true if <code>perform(x, y)</code> is to be invoked, false
     * will skip the current frame.
     */
    boolean check();
}
