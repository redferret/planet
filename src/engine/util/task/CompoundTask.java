

package engine.util.task;

/**
 * Some tasks may need to be broken into subtasks that require different timing.
 * This class offers the ability to create, using a factory, a compound task
 * that will be setup and bounded correctly to a surface map to perform multiple
 * tasks.
 * @author Richard DeSilvey
 */
public abstract class CompoundTask extends BasicTask {

    private TaskManager subTaskManager;
    
    public final void construct(){
    	if (subTaskManager == null) {
            Boundaries taskBounds = taskThread.getManager().getBounds();
            subTaskManager = new TaskManager(taskBounds);
        }
    	setup();
    }
    
    /**
     * Compound tasks don't need to use the <code>construct()</code> method
     * instead the <code>construct()</code> method will invoke the 
     * <code>setup()</code> method.
     */
    public abstract void setup();
    
    public void addSubTask(Task task) {
        subTaskManager.addTask(task);
    }
    
    @Override
    public void perform() {
        subTaskManager.performTasks();
    }

    @Override
    public void before() {
    }

    @Override
    public void after() {
    }

}
