
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import planet.util.MThread;

/**
 * Tests the MThread class for certain expected behavior.
 * @author Richard DeSilvey
 */
public class MThreadTest {

    public static CountDownLatch latch;
    private TestThread testThread;

    @Before
    public void setup(){
        testThread = new TestThread();
        testThread.start();
    }
    
    @Test
    public void playAndPauseTest() throws InterruptedException {
        latch = new CountDownLatch(1);
        
        testThread.play();
        boolean signaled = latch.await(500, TimeUnit.MILLISECONDS);
        assertTrue(signaled);
        
        testThread.pause();
        latch = new CountDownLatch(1);
        signaled = latch.await(100, TimeUnit.MILLISECONDS);
        assertFalse("Thread isn't paused", signaled);
    }
    
    /**
     * Tests the MThread for startup execution. By default when threads are
     * started they shouldn't execute any code until signaled to be played.
     * @throws InterruptedException 
     */
    @Test
    public void threadStartupTest() throws InterruptedException {
        latch = new CountDownLatch(1);

        boolean signaled = latch.await(300, TimeUnit.MILLISECONDS);
        assertFalse("Latch was signaled", signaled);
    }
    
    /**
     * Tests the MThread functionality for non-continuous behavior. Threads
     * can be signaled to execute one frame at a time.
     * @throws InterruptedException 
     */
    @Test
    public void nonContinuousExecutionTest() throws InterruptedException {
        
        final int TEST_COUNT = 5;
        
        for (int i = 0; i < TEST_COUNT; i++){
            latch = new CountDownLatch(2);
            testThread.play();
            boolean signaled = latch.await(100, TimeUnit.MILLISECONDS);
            assertFalse("Thread is not continuous", signaled);
        }
    }

    /**
     * Continuous threads only need to be signaled once when executing.
     * 
     * @throws InterruptedException 
     */
    @Test
    public void continuousExecutionTest() throws InterruptedException{
        testThread.setContinuous(true);
        latch = new CountDownLatch(5);
        
        testThread.play();
        boolean signaled = latch.await(100, TimeUnit.MILLISECONDS);
        
        assertTrue(signaled);
    }
    
    @After
    public void tearDown() {
        testThread.kill();
    }

}

class TestThread extends MThread {

    public TestThread() {
        super(0, "Test Thread", false);
    }

    @Override
    public void update() {
        MThreadTest.latch.countDown();
    }

}
