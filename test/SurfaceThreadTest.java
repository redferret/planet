
import java.util.concurrent.CyclicBarrier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import planet.util.Boundaries;
import planet.surface.SurfaceThread;
import planet.surface.SurfaceThreadException;
import planet.util.TaskAdapter;
import static org.junit.Assert.*;
/**
 *
 * @author Richard
 */
public class SurfaceThreadTest {
    
    private CyclicBarrier waitingGate;
    private SurfaceThread testThread;
    private static final Boundaries BOUNDS = new Boundaries(0, 1, 0, 1);
    
    @Before
    public void setUp() {
        waitingGate = new CyclicBarrier(1);
        testThread = new SurfaceThread(1, BOUNDS, "Test Thread", waitingGate);
        testThread.addTask(new ExecptionTask());
    }
    
    /**
     * Tests the functionality of the SurfaceThread exception handling.
     */
    @Test(expected = SurfaceThreadException.class)
    public void ForceExceptionTest(){
        testThread.throwExecption(true);
        testThread.update();
    }
    
    /**
     * The SurfaceThead by default should not force out an exception. 
     */
    @Test
    public void NoForcedExceptionTest(){
        try {
            testThread.update();
        } catch (SurfaceThreadException e) {
            fail("No Exception should have been thrown");
        }
    }
    
    @After
    public void tearDown() {
        testThread.kill();
    }
    
}

class ExecptionTask extends TaskAdapter {
    @Override
    public void perform(int x, int y) {
        throw new SurfaceThreadException("The task threw an error");
    }
}