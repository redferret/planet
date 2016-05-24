

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import planet.TestWorld;
import planet.util.Boundaries;
import planet.util.SurfaceThread;
import planet.util.Task;

/**
 * Runs tests on using Tasks in a SurfaceThread.
 * @author Richard DeSilvey
 */
public class TaskTest {
    
    /**
     * Used to keep the SurfaceThread from throwing an internal exception due
     * to a null reference to a Surface object.
     */
    private static TestWorld testWorld = new TestWorld();
    
    private SurfaceThread testThread;
    private static final Boundaries BOUNDS = new Boundaries(0, 100, 0, 100);

    @Before
    public void setUp() {
        testThread = new SurfaceThread(1, BOUNDS, "Test Thread");
        testThread.forceExecption(true);
    }
    
    @After
    public void tearDown() {
        testThread.kill();
    }
    
    /**
     * Tests a single iteration on the test SurfaceThread to run added Tasks.
     */
    @Test(expected = RuntimeException.class)
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

class TestTask implements Task {

    private boolean checkPass;
    
    public TestTask(boolean checkPass) {
        this.checkPass = checkPass;
    }
    
    @Override
    public void perform(int x, int y) {
        System.out.println("Test Task Running");
        throw new RuntimeException();
    }

    @Override
    public boolean check() {
        return checkPass;
    }
    
}
