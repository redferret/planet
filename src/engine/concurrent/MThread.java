package engine.concurrent;

import java.util.concurrent.CyclicBarrier;
import engine.task.Boundaries;
import engine.task.Task;
import engine.task.TaskManager;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A surface can be broken up into sections where a MThread can modify and
 control that section.
 *
 * @author Richard DeSilvey
 */
public class MThread extends TaskRunner {

  private final TaskManager manager;
  /**
   * Other threads may want to update or apply an event to other cells
   * not belonging to that thread. The other thread will post an update to
   * this thread to process the event using the queue.
   */
  private final ConcurrentLinkedDeque<Event> eventQueue;
  
  private static final boolean CONTINUOUS = true;

  private final CyclicBarrier waitingGate;

  public MThread(int delay) {
    this(delay, new Boundaries(0, 0), new CyclicBarrier(1));
  }
  
  public MThread(int delay, Boundaries bounds) {
    this(delay, bounds, new CyclicBarrier(1));
  }
  
  /**
   * Constructs a new MapThread.
   *
   * @param delay The amount of time to delay each frame in milliseconds
   * @param bounds The surface boundaries
   * @param waitingGate The CyclicBarrier to synchronize with other threads
   */
  public MThread(int delay, Boundaries bounds, CyclicBarrier waitingGate) {
    super(delay, CONTINUOUS);
    this.waitingGate = waitingGate;
    manager = new TaskManager(bounds);
    eventQueue = new ConcurrentLinkedDeque<>();
  }

  @Override
  public final void update() throws Exception {
    waitingGate.await();
    processEventQueue();
    manager.performTasks();
    manager.trimTasks();
  }

  public void pushEvent(Event event) {
    eventQueue.push(event);
  }
  
  public void processEventQueue() {
    eventQueue.forEach(event -> {
      event.execute();
    });
  }

  public final void addTask(Task task) {
    task.setThread(this);
    manager.addTask(task);
  }

  public TaskManager getManager() {
    return manager;
  }
}
