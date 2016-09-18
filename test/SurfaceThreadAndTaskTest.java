

import java.util.concurrent.CyclicBarrier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import engine.util.task.Boundaries;
import engine.surface.SurfaceThread;
import engine.util.exception.SurfaceThreadException;
import engine.util.task.Task;
import static org.junit.Assert.*;

/**
 * Runs tests on using Tasks in a SurfaceThread.
 * @author Richard DeSilvey
 */
public class SurfaceThreadAndTaskTest {
    
    private CyclicBarrier waitingGate;
    private SurfaceThread testThread;
    private static final Boundaries BOUNDS = new Boundaries(0, 100, 0, 100);

    @Before
    public void setUp() {
        waitingGate = new CyclicBarrier(1);
        testThread = new SurfaceThread(1, BOUNDS, waitingGate);
        testThread.throwExecption(true);
    }
    
    @After
    public void tearDown() {
        testThread.kill();
    }
    
    /**
     * Tests a single iteration on the test SurfaceThread to run added Tasks.
     */
    @Test(expected = SurfaceThreadException.class)
    public void callingPerformMethodTest(){
        TestTask task = new TestTask(true);
        testThread.addTask(task);
        testThread.update();
    }
    
    @Test
    public void callingCheckMethodTest(){
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

    private boolean checkPass;
    
    public TestTask(boolean checkPass) {
        this.checkPass = checkPass;
    }
    
    @Override
    public void perform(int x, int y) {
        System.out.println("Test Task Running");
        throw new SurfaceThreadException();
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
