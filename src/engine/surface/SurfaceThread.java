package engine.surface;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;
import engine.util.Boundaries;
import engine.util.MThread;
import engine.util.Task;

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
    private boolean throwExecption;
    
    private CyclicBarrier waitingGate;
    /**
     * Constructs a new SurfaceThread.
     *
     * @param delay The amount of time to delay each frame in milliseconds
     * @param bounds The surface boundaries
     * @param name The name of this thread
     * @param waitingGate The CyclicBarrier to synchronize with other threads
     */
    public SurfaceThread(int delay, Boundaries bounds, String name, CyclicBarrier waitingGate) {
        super(delay, name, CONTINUOUS);
        this.waitingGate = waitingGate;
        this.bounds = bounds;
        curFrame = 0;
        tasks = new LinkedList<>();
        throwExecption = false;
    }
    
    public void throwExecption(boolean b){
        throwExecption = b;
    }
    
    public final void update() {

        try {
            waitingGate.await();
            performTasks();
        } catch (SurfaceThreadException | InterruptedException | BrokenBarrierException e) {
            logException(e);
        }
        curFrame++;
    }

    private void logException(final java.lang.Exception e) throws SurfaceThreadException {
        String msg = "An exception occured when updating the surface:" + getName();
        if (throwExecption){
            throw new SurfaceThreadException(e);
        }else{
            Logger.getLogger(SurfaceThread.class.getName()).log(Level.SEVERE, msg);
            e.printStackTrace();
        }
    }

    private void performTasks() {
        
        int lowerYBound = bounds.getLowerYBound();
        int upperYBound = bounds.getUpperYBound();
        int lowerXBound = bounds.getLowerXBound();
        int upperXBound = bounds.getUpperXBound();
        
        tasks.forEach(task -> {
            if (task.check()) {
                for (int y = lowerYBound; y < upperYBound; y++) {
                    for (int x = lowerXBound; x < upperXBound; x++) {
                        task.perform(x, y);
                    }
                }
            }
        });
    }

    public void addTask(Task task){
        tasks.add(task);
    }
    
}
