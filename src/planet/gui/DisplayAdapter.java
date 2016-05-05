
package planet.gui;


/**
 * Connects the simulation and any display object. The method encapsulated in
 * this interface will be called by the Surface object each time it updates.
 * This doesn't mean that the surface is ready or not being worked on but still
 * signals your display to refresh.
 * 
 * @author Richard DeSilvey
 */
public interface DisplayAdapter {
    
    /**
     * On each update to the surface the simulation will call this method.
     * Rendering doesn't have to always happen when this method is called,
     * it is more of a way to flag your display when the data has finished
     * updating on the current frame.
     */
    public void repaint();
    
}
