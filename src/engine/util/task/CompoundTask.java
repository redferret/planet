

package engine.util.task;

/**
 * Some tasks may need to be broken into subtasks that require different timing.
 * This class offers the ability to create, using a factory, a compound task
 * that will be setup and bounded correctly to a surface map to perform multiple
 * tasks. Compound tasks need to be created via TaskFactory to work properly.
 * @author Richard DeSilvey
 */
public class CompoundTask extends BasicTask {

    private TaskManager subTaskManager;
    
    public void addSubTask(Task task){
        subTaskManager.addTask(task);
    }
    
    @Override
    public void perform() {
        subTaskManager.performTasks();
    }

    @Override
    public void before() {
        if (subTaskManager == null) {
            Boundaries taskBounds = taskThread.getManager().getBounds();
            subTaskManager = new TaskManager(taskBounds);
        }
    }

    @Override
    public void after() {
    }

}
