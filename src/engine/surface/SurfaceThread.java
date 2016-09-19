package engine.surface;


import engine.util.exception.SurfaceThreadException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;
import engine.util.task.Boundaries;
import engine.util.concurrent.TaskRunner;
import engine.util.task.Task;
import engine.util.task.TaskManager;

/**
 * A surface can be broken up into sections where a SurfaceThread can modify and
 * control that section.
 *
 * @author Richard DeSilvey
 */
public class SurfaceThread extends TaskRunner {

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
     * @param waitingGate The CyclicBarrier to synchronize with other threads
     */
    public SurfaceThread(int delay, Boundaries bounds, CyclicBarrier waitingGate) {
        super(delay, CONTINUOUS);
        this.waitingGate = waitingGate;
        manager = new TaskManager(bounds);
        curFrame = 0;
        throwExecption = false;
    }
    
    public void throwExecption(boolean b) {
        throwExecption = b;
    }
    
    public final void update() {

        try {
            waitingGate.await();
            manager.performTasks();
        } catch (RuntimeException e) {
            logException(e);
        } catch (InterruptedException | BrokenBarrierException ex) {
        }
        curFrame++;
    }

    private void logException(final java.lang.Exception e) throws SurfaceThreadException {
        String msg = "An exception occured when updating the surface";
        if (throwExecption){
            throw new SurfaceThreadException(e);
        }else{
            Logger.getLogger(SurfaceThread.class.getName()).log(Level.SEVERE, msg);
            e.printStackTrace();
        }
    }

    public final void addTask(Task task){
        manager.addTask(task);
    }

    public TaskManager getManager() {
        return manager;
    }
}
