
import java.util.concurrent.CyclicBarrier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import engine.util.task.Boundaries;
import engine.util.concurrent.MThread;
import engine.util.task.Task;
import static org.junit.Assert.*;

/**
 * Runs tests on using Tasks in a SurfaceThread.
 *
 * @author Richard DeSilvey
 */
public class MapThreadAndTaskTest {

  private CyclicBarrier waitingGate;
  private MThread testThread;
  private static final Boundaries BOUNDS = new Boundaries(0, 100, 0, 100);

  @Before
  public void setUp() {
    waitingGate = new CyclicBarrier(1);
    testThread = new MThread(1, BOUNDS, waitingGate);
  }

  @After
  public void tearDown() {
    testThread.kill();
  }

  /**
   * Tests a single iteration on the test SurfaceThread to run added Tasks.
   */
  @Test(expected = Exception.class)
  public void callingPerformMethodTest() throws Exception {
    TestTask task = new TestTask(true);
    testThread.addTask(task);
    testThread.update();
  }

  @Test
  public void callingCheckMethodTest() throws Exception {
    TestTask task = new TestTask(false);
    testThread.addTask(task);
    try {
      testThread.update();
    } catch (RuntimeException e) {
      fail("There should be no execptions thrown by the SurfaceThread");
    }
  }

}

class TestTask extends Task {

  private final boolean checkPass;

  public TestTask(boolean checkPass) {
    this.checkPass = checkPass;
  }

  public void construct() {
  }

  ;
    
    @Override
  public void perform(int x, int y) throws Exception {
    System.out.println("Test Task Running");
    throw new Exception();
  }

  @Override
  public boolean check() {
    return checkPass;
  }

  @Override
  public void before() {
  }

  @Override
  public void after() {
  }
}
