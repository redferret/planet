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
    private long curFrame;
    
    public TaskManager(Boundaries bounds) {
        tasks = new LinkedList<>();
        this.bounds = bounds;
        curFrame = 0;
    }
    
    /**
     * Adding a task to a manager will invoke the Task's 'construct' method.
     * @param task The task being added to this manager.
     */
    public void addTask(Task task){
    	task.construct();
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
            	try{
            		task.before();
            	} finally {
	                boolean sw = (curFrame % 2) == 0;
	
	                int ystart = sw? lowerYBound : (upperYBound - 1);
	                int yinc = sw? 1 : -1;
	
	                for (int b = 0; b < 2; b++) {
	                    for (int y = ystart; (sw? (y < upperYBound) : (y >= 0)); y += yinc) {
	
	                        int m = ((b > 0) && (y % 2 == 0))? lowerXBound + 1
	                                : ((b > 0) && (y % 2 != 0) ? lowerXBound - 1 : lowerXBound);
	
	                        for (int x = (y % 2) + m; x < upperXBound; x += 2) {
	                            task.perform(x, y);
	                        }
	                    }
	                }
            	}
            	try{
            		task.after();
            	}finally {}
            }
        });
    }
    
}
