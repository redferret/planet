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
import engine.util.TaskManager;

/**
 * A surface can be broken up into sections where a SurfaceThread can modify and
 * control that section.
 *
 * @author Richard DeSilvey
 */
public class SurfaceThread extends MThread {

    /**
     * Lower bounds are inclusive, upper bounds are exclusive
     */
    private int curFrame;
    private TaskManager manager;
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
        manager = new TaskManager(bounds);
        curFrame = 0;
        throwExecption = false;
    }
    
    public void throwExecption(boolean b){
        throwExecption = b;
    }
    
    public final void update() {

        try {
            waitingGate.await();
            manager.performTasks();
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

    public void addTask(Task task){
        manager.addTask(task);
    }
    
}
