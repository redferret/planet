package planet.generics;

import java.util.logging.Level;
import java.util.logging.Logger;
import planet.surface.Surface;
import planet.util.Boundaries;
import planet.util.MThread;

/**
 * A surface can be broken up into sections where a SurfaceThread can modify
 * and control that section. 
 * @author Richard DeSilvey
 */
public class SurfaceThread extends MThread {

    /**
     * Lower bounds are inclusive, upper bounds are exclusive
     */
    protected Boundaries bounds;
    private int curFrame;
    protected Surface surface;
    
    /**
     * Constructs a new SurfaceThread.
     * @param delay The amount of time to delay each frame in milliseconds
     * @param bounds The surface boundaries
     * @param name The name of this thread
     * @param ref The reference to the surface being worked on
     */
    public SurfaceThread(int delay, Boundaries bounds, String name, Surface ref) {
        super(delay, name, false);

        this.bounds = bounds;
        curFrame = 0;
        this.surface =  ref;
    }

    
    /**
     * Each time the thread posts an update this method is called following
     * a post-update call to postUpdate()
     */
    public final void update(){

        boolean sw  = (curFrame % 2) == 0;
        int m;
        int lowerYBound = bounds.getLowerYBound();
        int upperYBound = bounds.getUpperYBound();
        int lowerXBound = bounds.getLowerXBound();
        int upperXBound = bounds.getUpperXBound();
        
        
        int ystart  = sw ? lowerYBound : (upperYBound - 1);
        int yinc    = sw ? 1 : -1; 

        try {
            for (int b = 0; b < 2; b++) {
                for (int y = ystart; (sw ? (y < upperYBound) : (y >= 0)); y += yinc) {
                    
                    m = ((b > 0) && (y % 2 == 0)) ? 1
                            : ((b > 0) && (y % 2 != 0) ? -1 : 0);
                    
                    for (int x = ((y % 2) + m) + lowerXBound; x < upperXBound; x += 2) {
                        update(x, y);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(SurfaceThread.class.getName()).log(Level.SEVERE, 
                    "An exception occured when updating the surface");
            e.printStackTrace();
        }
        curFrame++;
        
    }
   
    private void update(int x, int y) {
        surface.updateGeology(x, y);
        surface.updateOceans(x, y);
        surface.updateMinimumHeight(x, y);
    }

}
