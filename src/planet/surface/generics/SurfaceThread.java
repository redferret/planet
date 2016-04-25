package planet.surface.generics;

import planet.surface.generics.Cell;
import planet.util.Boundaries;
import planet.util.MThread;

/**
 *
 * @author Richard DeSilvey
 * @param <CellType>
 */
public abstract class SurfaceThread<CellType extends Cell> extends MThread {

    /**
     * Lower bounds are inclusive, upper bounds are exclusive
     */
    protected Boundaries bounds;
    private int curFrame;
    protected SurfaceMap<CellType> surface;
    
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
     * @param x
     * @param y 
     */
    protected abstract void update(int x, int y);
    
}
