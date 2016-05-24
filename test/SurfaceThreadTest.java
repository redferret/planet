
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import planet.TestWorld;
import planet.util.Boundaries;
import planet.util.SurfaceThread;

/**
 *
 * @author Richard
 */
public class SurfaceThreadTest {
    
    private SurfaceThread testThread;
    private static final Boundaries BOUNDS = new Boundaries(0, 1, 0, 1);
    
    @Before
    public void setUp() {
        testThread = new SurfaceThread(1, BOUNDS, "Test Thread");
    }
    
    /**
     * Since there is no Planet object being created, there should be
     * a RuntimeException thrown by the SurfaceThread during it's update
     * iteration.
     */
    @Test(expected = RuntimeException.class)
    public void ForceExceptionTest(){
        testThread.forceExecption(true);
        testThread.update();
    }
    
    /**
     * The SurfaceThead by default should not force out an exception. 
     */
    @Test
    public void NoForcedExceptionTest(){
        try {
            testThread.update();
        } catch (Exception e) {
            fail("No Exception should have been thrown");
        }
    }
    
    @After
    public void tearDown() {
        testThread.kill();
    }
    
}
