package engine.util.task;

import engine.util.concurrent.SurfaceThread;

/**
 * A Task is something the simulation will perform given a condition.
 *
 * @author Richard DeSilvey
 */
public abstract class Task {

    /**
     * A public, mutable, reference to the thread working on this task. If
     * this object reference is null then this task is not part of a 
     * TaskFactory.
     */
    protected SurfaceThread taskThread = null;
    
    public void setThread(SurfaceThread thread){
    	taskThread = thread;
    }
    
    /**
     * This method is called before updating each cell on the surface. If this
     * method returns false then perform on the current frame won't be called.
     *
     * @return true if <code>perform(x, y)</code> is to be invoked, false will
     * skip the current frame.
     */
    public abstract boolean check();

    /**
     * A before-processing method called before perform(x, y) or perform()
     * starts.
     */
    public abstract void before();

    /**
     * This method will be called for each cell on the surface. This method will
     * only be called if <code>check()</code> returns true. Both x and y are
     * bounded by [0, w) where w is the width of the map.
     *
     * @param x The x coordinate of the cell.
     * @param y The y coordinate of the cell.
     */
    public abstract void perform(int x, int y);

    /**
     * A after-processing method called after perform(x, y) or perform()
     * finishes.
     */
    public abstract void after();

    /**
     * This method is called when a task is added to a TaskManager. This
     * method is used because in some instances the rest of the framework
     * hasn't been initialized yet. CompoundTasks that add subtasks would
     * use this method instead of a constructor.
     */
    public abstract void construct();
}
