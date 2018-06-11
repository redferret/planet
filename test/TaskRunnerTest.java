
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import engine.concurrent.TaskRunner;

/**
 * Tests the TaskRunner class for certain expected behavior. The TaskRunner is a
 * Runnable object that will execute updates and will manage the behavior of
 * threads. Threads can be paused, played, or killed.
 *
 * @author Richard DeSilvey
 */
public class TaskRunnerTest {

  public static CountDownLatch latch;
  private TestThread testThread;
  private Thread thread;

  @Test
  public void playAndPauseTest() throws InterruptedException {
    latch = new CountDownLatch(1);

    testThread = new TestThread(latch);
    thread = new Thread(testThread);

    thread.start();
    testThread.play();
    boolean signaled = latch.await(600, TimeUnit.MILLISECONDS);
    assertTrue(signaled);

    testThread.pause();
    latch = new CountDownLatch(1);
    signaled = latch.await(100, TimeUnit.MILLISECONDS);
    assertFalse("Thread isn't paused", signaled);
  }

  /**
   * Tests the TaskRunner for startup execution. By default when threads are
   * started they shouldn't execute any code until signaled to be played.
   *
   * @throws InterruptedException
   */
  @Test
  public void threadStartupTest() throws InterruptedException {
    latch = new CountDownLatch(1);

    testThread = new TestThread(latch);
    thread = new Thread(testThread);

    boolean signaled = latch.await(300, TimeUnit.MILLISECONDS);
    assertFalse("Latch was signaled", signaled);
  }

  /**
   * Tests the TaskRunner functionality for non-continuous behavior. Threads can
   * be signaled to execute one frame at a time.
   *
   * @throws InterruptedException
   */
  @Test
  public void nonContinuousExecutionTest() throws InterruptedException {

    final int TEST_COUNT = 5;

    for (int i = 0; i < TEST_COUNT; i++) {
      latch = new CountDownLatch(2);
      testThread = new TestThread(latch);
      thread = new Thread(testThread);

      testThread.play();
      boolean signaled = latch.await(100, TimeUnit.MILLISECONDS);
      assertFalse("Thread is continuous", signaled);
    }
  }

  /**
   * Continuous threads only need to be signaled once when executing.
   *
   * @throws InterruptedException
   */
  @Test
  public void continuousExecutionTest() throws InterruptedException {

    latch = new CountDownLatch(5);

    testThread = new TestThread(latch);
    thread = new Thread(testThread);
    testThread.setContinuous(true);

    thread.start();
    testThread.play();
    boolean signaled = latch.await(100, TimeUnit.MILLISECONDS);

    assertTrue(signaled);
  }

  @After
  public void tearDown() {
    testThread.kill();
  }

}

/**
 * A non-continuous test thread to test the TaskRunner class.
 *
 * @author Richard DeSilvey
 *
 */
class TestThread extends TaskRunner {

  private static final int NO_DELAY = 0;
  private static final boolean CONTINUOUS = false;
  private CountDownLatch latch;

  public TestThread(CountDownLatch latch) {
    super(NO_DELAY, CONTINUOUS);
    this.latch = latch;
  }

  @Override
  public void update() {
    latch.countDown();
  }

}
