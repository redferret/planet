

package engine.util;

/**
 * A Task is something the simulation will perform given a condition.
 * @author Richard DeSilvey
 */
public interface Task {
    /**
     * This method will be called for each cell on the surface. This method
     * will only be called if <code>check()</code> returns true. Both x and y
     * are bounded by [0, w) where w is the width of the map.
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
    
    /**
     * Called after perform(x, y) finishes on the entire map.
     */
    void post();
    
    /**
     * Called before perform(x, y) starts on the entire map.
     */
    void pre();
}
