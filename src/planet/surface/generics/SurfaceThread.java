package planet.surface.generics;


import planet.util.Boundaries;
import planet.util.MThread;

/**
 * A surface can be broken up into sections where a SurfaceThread can modify
 * and control that section. 
 * @author Richard DeSilvey
 * @param <CellType> The cell being worked on.
 */
public abstract class SurfaceThread<CellType extends Cell> extends MThread {

    /**
     * Lower bounds are inclusive, upper bounds are exclusive
     */
    protected Boundaries bounds;
    private int curFrame;
    protected SurfaceMap<CellType> surface;
    
    /**
     * Constructs a new SurfaceThread.
     * @param delay The amount of time to delay each frame in milliseconds
     * @param bounds The surface boundaries
     * @param name The name of this thread
     * @param ref The reference to the surface being worked on
     */
    public SurfaceThread(int delay, Boundaries bounds, String name, SurfaceMap<CellType> ref) {
        
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

        for (int b = 0; b < 2; b++){
            for (int y = ystart; (sw ? (y < upperYBound) : (y >= 0)); y += yinc){

                m = ((b > 0) && (y % 2 == 0)) ? 1 :
                    ((b > 0) && (y % 2 != 0) ? -1 : 0);
                

                for (int x = ((y % 2) + m) + lowerXBound; x < upperXBound; x += 2){

                    update(x, y);
                }
            }
        }
        curFrame++;
        
        if (curFrame == Integer.MAX_VALUE){
            curFrame = 0;
        }
        
        postUpdate();
        
    }
    
    /**
     * This method is called after the update(x, y) method is called.
     * This allows a surface map to apply buffers or do some other
     * special type of update to the surface.
     */
    protected abstract void postUpdate();
    
    /**
     * This method is called with the coordinates for a cell location
     * that is ready to be updated.
     * @param x The x coordinate
     * @param y The y coordinate
     */
    protected abstract void update(int x, int y);
    
}
