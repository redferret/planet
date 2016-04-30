
package planet.gui;


/**
 * The DisplayAdapter provides an additional draw method after the Frame is
 * finished rendering. If the Display is not drawing SurfaceMaps then this
 * adapter can be used to access drawing. This method is called after
 * SurfaceMaps are drawn (if any) and SpriteObjects currently held in the
 * engine.
 * 
 * @author Richard DeSilvey
 */
public interface DisplayAdapter {
    
    public void repaint();
    
}
