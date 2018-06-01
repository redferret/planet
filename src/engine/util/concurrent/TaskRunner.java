package engine.util.concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Modified Runnable class.
 *
 * @author Richard DeSilvey
 */
public abstract class TaskRunner implements Runnable {

  /**
   * The delay speed in milliseconds.
   */
  private int miliSeconds;

  /**
   * Flag used to determine if the thread is running or not.
   */
  protected boolean running;

  /**
   * Flag to determine if this thread is finished executing
   */
  private boolean executing;

  /**
   * Flag that determines if this thread will loop continuously without being
   * invoked by another thread to run an iteration.
   */
  private boolean continuous;

  /**
   * The amount of time that lapsed to process a frame
   */
  private final AtomicInteger timeLapse;

  /**
   * Barrier for this thread to wait to be signaled by another thread.
   */
  private final CyclicBarrier waiter;

  /**
   * Creates a new TaskRunner with the given delay and whether this TaskRunner
   * is continuous.
   *
   * @param delay The time in milliseconds to delay for each frame or iteration.
   * @param continuous If this Runnable is continuous.
   */
  public TaskRunner(int delay, boolean continuous) {
    miliSeconds = delay;
    this.continuous = continuous;
    running = false;
    executing = true;
    timeLapse = new AtomicInteger(0);
    waiter = new CyclicBarrier(2);
  }

  /**
   * The most recent time that has lapsed.
   *
   * @return
   */
  public int timeLapse() {
    return timeLapse.get();
  }

  /**
   * Reset the amount of time to delay each frame
   *
   * @param delay The time to delay each frame in milliseconds.
   */
  public void setDelay(int delay) {
    miliSeconds = delay;
  }

  /**
   * Pauses this runner
   */
  public void pause() {
    running = false;
  }

  /**
   * Flags the runner to start running again.
   */
  public void play() {
    running = true;
    waiter.reset();
  }

  /**
   * Kills the process forever.
   */
  public void kill() {
    executing = false;
    waiter.reset();
  }

  /**
   * Whether this runner is paused or not.
   *
   * @return True if this runner is currently paused.
   */
  public boolean isPaused() {
    return !running;
  }

  /**
   * This method will be called by this class on each frame
   */
  public abstract void update() throws Exception;

  /**
   * Reset this runner as being continuous or not
   *
   * @param continuous
   */
  public void setContinuous(boolean continuous) {
    this.continuous = continuous;
  }

  @Override
  @SuppressWarnings("SleepWhileInLoop")
  public void run() {
    while (executing) {
      try {
        if (running) {
          long start = System.currentTimeMillis();
          update();
          timeLapse.getAndSet((int) (System.currentTimeMillis() - start));
        }

        Thread.sleep(miliSeconds);

        if (!running || !continuous) {
          waiter.await();
        }
      } catch (InterruptedException | BrokenBarrierException e) {
      } catch (Exception ex) {
        ex.printStackTrace();
        executing = false;
      }
    }
  }

}
