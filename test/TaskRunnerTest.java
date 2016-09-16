
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import engine.util.concurrent.TaskRunner;

/**
 * Tests the TaskRunner class for certain expected behavior.
 * @author Richard DeSilvey
 */
public class TaskRunnerTest {

    public static CountDownLatch latch;
    private TestThread testThread;
    private Thread thread;

    @Before
    public void setup(){
        testThread = new TestThread();
        thread = new Thread(testThread);
    }
    
    @Test
    public void playAndPauseTest() throws InterruptedException {
        latch = new CountDownLatch(1);
        
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
     * @throws InterruptedException 
     */
    @Test
    public void threadStartupTest() throws InterruptedException {
        latch = new CountDownLatch(1);

        boolean signaled = latch.await(300, TimeUnit.MILLISECONDS);
        assertFalse("Latch was signaled", signaled);
    }
    
    /**
     * Tests the TaskRunner functionality for non-continuous behavior. Threads
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
            assertFalse("Thread is continuous", signaled);
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

class TestThread extends TaskRunner {

    private static final int NO_DELAY = 0;
    private static final boolean CONTINUOUS = false;
    
    public TestThread() {
        super(NO_DELAY, CONTINUOUS);
    }

    @Override
    public void update() {
        TaskRunnerTest.latch.countDown();
    }

}
