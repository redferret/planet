
package engine.surface;

import engine.util.concurrent.MThread;
import engine.util.task.Boundaries;
import engine.util.task.Task;
import engine.util.task.TaskFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Richard
 */
public class SurfaceThreads {
  
  protected List<MThread> threadReferences;
  private int prevSubThreadAvg;
  private ExecutorService threadPool;
  private CyclicBarrier waitingGate;

  public SurfaceThreads() {
    threadReferences = new ArrayList<>();
    prevSubThreadAvg = 0;
  }
  
  /**
   * Starts all the threads and "Initiates an orderly shutdown in which
   * previously submitted tasks are executed, but no new tasks will be accepted.
   * Invocation has no additional effect if already shut down."
   * the main thread is also started.
   */
  public final void startThreads() {
    threadPool.shutdown();
  }

  /**
   * Pauses all the threads
   */
  public final void pauseThreads() {
    threadReferences.forEach(thread -> {
      thread.pause();
    });
  }

  /**
   * Plays all the threads
   */
  public final void playThreads() {
    threadReferences.forEach(thread -> {
      thread.play();
    });
  }
  
  /**
   * Sets all the threads to this delay except the main thread
   *
   * @param delay The amount of time to set all threads to delay each frame in
   * milliseconds.
   */
  public final void setThreadsDelay(int delay) {
    threadReferences.forEach(thread -> {
      thread.setDelay(delay);
    });
  }
  
  /**
   * Adds the Task instance to each thread. The thread will be assigned to the
   * task allowing access to the parent thread via the task.
   *
   * @param task The task being added to each thread.
   */
  public void addTaskToThreads(Task task) {
    threadReferences.forEach(thread -> {
      thread.addTask(task);
    });
  }

  /**
   * Produces individual instances of a Task for each thread using the given
   * instance of a TaskFactory.
   *
   * @param factory The factory that will produce a Task for each thread.
   */
  public void produceTasks(TaskFactory factory) {
    threadReferences.forEach(thread -> {
      Task producedTask = factory.buildTask();
      thread.addTask(producedTask);
    });
  }

  public int getThreadCount() {
    return this.threadReferences.size();
  }
  
  public void checkSubThreads() {
    int avg = 0;
    for (MThread thread : threadReferences) {
      avg = thread.timeLapse();
    }
    prevSubThreadAvg = avg / threadReferences.size();
  }
  
  /**
   * This method will check if all
   * threads have finished their iteration. If all threads have finished their
   * iteration then this method will signal all threads to run and return true,
   * otherwise this method will return false.<br>
   * One could wait for the threads until they are ready to run again, this
   * will ensure that all threads are ready to run in synch with each other.
   * If they are not continuously running this method can be used to run each
   * thread.
   * <br>
   * <code>while(!synchronizeThreads()){<br>// Block until threads are done and ready to run again
   * <br>}</code>
   *
   * @return True if all the threadReferences were signaled to run.
   */
  public boolean synchronizeThreads() {
    int sleeping = 0;
    int expected = threadReferences.size();

    if (expected > 0) {
      for (int i = 0; i < expected; i++) {
        boolean paused = threadReferences.get(i).isPaused();
        if (paused) {
          sleeping++;
        }
      }
      if (sleeping == expected) {
        playThreads();
        return true;
      }
    }
    return false;
  }

  public void setThreadsAsContinuous(boolean c) {
    threadReferences.forEach(thread -> {
      thread.setContinuous(c);
    });
  }

  /**
   * Gets the average runtime between all threads loaded in the simulation.
   *
   * @return The average runtime between all threads.
   */
  public int getAverageThreadTime() {
    return prevSubThreadAvg;
  }
  
  /**
   * Sets up each individual thread for this surface. If you are using this
   * surface with multiple threads working on the same Map it is recommended to
   * setup the Map by calling the <code>setupDefaultMap()</code> method. This
   * will setup the Map as a ConcurrentHashMap. Otherwise the Map data structure
   * needs to be able to handle multiple threads accessing it's contents at the
   * same time similar to how the ConcurrentHashMap functions.
   *
   * @param terrainSize
   * @param threadDivision The value given is the dimensions of the threads. A
   * value n would yield an N x N grid of threads. Each controlling a section of
   * the surface map. Each thread is a SurfaceThread.
   * @param delay The thread delay for each frame in milliseconds.
   */
  public final void setupThreads(int terrainSize, int threadDivision, int delay) {

    int threadCount = threadDivision * threadDivision;
    waitingGate = new CyclicBarrier(threadCount);
    int w = terrainSize / threadDivision;
    Boundaries bounds;
    threadPool = Executors.newFixedThreadPool(threadCount + 1);
    for (int y = 0; y < threadDivision; y++) {
      for (int x = 0; x < threadDivision; x++) {
        int lowerX = w * x;
        int upperX = w * (x + 1);
        int lowerY = w * y;
        int upperY = w * (y + 1);
        bounds = new Boundaries(lowerX, upperX, lowerY, upperY);
        MThread thread = new MThread(delay, bounds, waitingGate);
        threadPool.submit(thread);
        threadReferences.add(thread);
      }
    }
  }

  /**
   * Shuts down all threads in the pool.
   */
  public void killAllThreads() {
    threadReferences.forEach(thread -> {
      thread.kill();
    });
    threadPool.shutdownNow();
  }

}
