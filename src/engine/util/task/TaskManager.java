package engine.util.task;

import java.util.Deque;
import java.util.LinkedList;

/**
 *
 * @author Richard DeSilvey
 */
public class TaskManager {

    private Deque<Task> tasks;
    protected Boundaries bounds;
    
    public TaskManager(Boundaries bounds) {
        tasks = new LinkedList<>();
        this.bounds = bounds;
    }
    
    public void addTask(Task task){
        tasks.add(task);
    }
    
    public Boundaries getBounds() {
        return bounds;
    }
    
    public void performTasks() {
        int lowerYBound = bounds.getLowerYBound();
        int upperYBound = bounds.getUpperYBound();
        int lowerXBound = bounds.getLowerXBound();
        int upperXBound = bounds.getUpperXBound();
        
        tasks.forEach(task -> {
            if (task.check()) {
                task.before();
                for (int y = lowerYBound; y < upperYBound; y++) {
                    for (int x = lowerXBound; x < upperXBound; x++) {
                        task.perform(x, y);
                    }
                }
                task.after();
            }
        });
    }
    
}