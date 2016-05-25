package planet.util;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import planet.Planet;
import planet.surface.PlanetSurface;

/**
 * A surface can be broken up into sections where a SurfaceThread can modify and
 * control that section.
 *
 * @author Richard DeSilvey
 */
public class SurfaceThread extends MThread {

    private Deque<Task> tasks;
    /**
     * Lower bounds are inclusive, upper bounds are exclusive
     */
    protected Boundaries bounds;
    private int curFrame;
    
    private static final boolean CONTINUOUS = true;
    private boolean forceExecption;
    /**
     * Constructs a new SurfaceThread.
     *
     * @param delay The amount of time to delay each frame in milliseconds
     * @param bounds The surface boundaries
     * @param name The name of this thread
     */
    public SurfaceThread(int delay, Boundaries bounds, String name) {
        super(delay, name, CONTINUOUS);

        this.bounds = bounds;
        curFrame = 0;
        tasks = new LinkedList<>();
        forceExecption = false;
    }
    
    public void catchExecption(boolean b){
        forceExecption = b;
    }
    
    /**
     * Each time the thread posts an perform this method is called following a
     * post-perform call to postUpdate()
     */
    public final void update() {

        int lowerYBound = bounds.getLowerYBound();
        int upperYBound = bounds.getUpperYBound();
        int lowerXBound = bounds.getLowerXBound();
        int upperXBound = bounds.getUpperXBound();
        
        try {
            tasks.forEach(task -> {
                if (task.check()) {
                    for (int y = lowerYBound; y < upperYBound; y++) {
                        for (int x = lowerXBound; x < upperXBound; x++) {
                            task.perform(x, y);
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            String msg = "An exception occured when updating the surface:" + getName();
            if (forceExecption){
                throw new RuntimeException(e);
            }else{
                Logger.getLogger(SurfaceThread.class.getName()).log(Level.SEVERE, msg);
                e.printStackTrace();
            }
        }
        curFrame++;
    }

    public void addTask(Task task){
        tasks.add(task);
    }
    
}