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

    private float absLowestHeight;
    private AtomicInteger lowestHeightIntPart;
    private AtomicInteger lowestHeightDecPart;
    private Deque<Task> tasks;
    /**
     * Lower bounds are inclusive, upper bounds are exclusive
     */
    protected Boundaries bounds;
    private int curFrame;
    
    private static final boolean CONTINUOUS = true;
    
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
        lowestHeightIntPart = new AtomicInteger(Integer.MAX_VALUE);
        lowestHeightDecPart = new AtomicInteger(Integer.MAX_VALUE);
        absLowestHeight = Integer.MAX_VALUE;
        tasks = new LinkedList<>();
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
            
            for (int y = lowerYBound; y < upperYBound; y++) {
                for (int x = lowerXBound; x < upperXBound; x++) {
                    updateMinimumHeight(x, y);
                }
            }
            
        } catch (Exception e) {
            Logger.getLogger(SurfaceThread.class.getName()).log(Level.SEVERE,
                    "An exception occured when updating the surface: {0}", getName());
            e.printStackTrace();
        }
        curFrame++;

        absLowestHeight = absLowestHeight < 0 ? 0 : absLowestHeight;
        int intPart = (int) absLowestHeight;
        int decPart = (int) ((absLowestHeight - intPart) * 10);

        lowestHeightIntPart.set(intPart);
        lowestHeightDecPart.set(decPart);

        absLowestHeight = Integer.MAX_VALUE;
    }

    public float getPreviousLowestHeight() {

        float decPart = lowestHeightDecPart.get() / 10f;
        decPart = lowestHeightIntPart.get() + decPart;

        return decPart;
    }

    public void addTask(Task task){
        tasks.add(task);
    }
    
    private void updateMinimumHeight(int x, int y) {
        PlanetSurface surface = (PlanetSurface) Planet.self().getSurface();
        float cellHeight = surface.getCellAt(x, y).getHeightWithoutOceans();

        if (cellHeight < absLowestHeight) {
            absLowestHeight = cellHeight;
        }
    }

}