

import java.util.concurrent.CyclicBarrier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import engine.util.task.Boundaries;
import engine.util.concurrent.SurfaceThread;
import engine.util.task.Task;
import engine.util.task.TaskFactory;

/**
 * Tests the functionality of the TaskFactory by proving each thread will
 * have their own instance of a Task.
 * @author Richard DeSilvey
 */
public class TaskFactoryTest {
    
	/**
	 * Simple boundaries for threads
	 */
    private static final Boundaries BOUNDS = new Boundaries(0, 1);
    
    /**
     * The waiting gate for the threads
     */
    private CyclicBarrier waitingGate;
    
    /**
     * Test threads
     */
    private SurfaceThread testThread, secondTestThread;
    
    /**
     * Test factory for the threads
     */
    private MyFactory factory;
    
    @Before
    public void setUp() {
        factory = new MyFactory();
        waitingGate = new CyclicBarrier(1);
        testThread = new SurfaceThread(1, BOUNDS, waitingGate);
        secondTestThread = new SurfaceThread(1, BOUNDS, waitingGate);
    }
    
    /**
     * Tests that each thread has it's own instance of a Task. The expected
     * outcome is that the counter inside each task only gets incremented
     * once.
     */
    @Test
    public void sharedResourcesTest(){
        MyFactory.MyTask testTask1 = (MyFactory.MyTask) factory.buildResource();
        MyFactory.MyTask testTask2 = (MyFactory.MyTask) factory.buildResource();
        testThread.addTask(testTask1);
        secondTestThread.addTask(testTask2);
        
        testThread.update();
        secondTestThread.update();
        
        assertTrue(testTask1.getCounter() == 1);
        assertTrue(testTask2.getCounter() == 1);
    }
    
    /**
     * Tests what happens when two threads share the same Task. The
     * expected outcome is that the counter is incremented twice because
     * each thread shares the same counter.
     */
    @Test
    public void singleTaskOnTwoThreadsTest(){
        MyFactory.MyTask taskResource = (MyFactory.MyTask) factory.buildResource();
        testThread.addTask(taskResource);
        secondTestThread.addTask(taskResource);
        
        testThread.update();
        secondTestThread.update();
        
        assertTrue(taskResource.getCounter() > 1);
    }
    
    @After
    public void tearDown() {
        testThread.kill();
    }
    
}

class MyFactory implements TaskFactory {

    @Override
    public Task buildResource() {
        return new MyTask();
    }
    
    public class MyTask extends Task {

    	/**
    	 * The resource to test
    	 */
        private Integer counter;
        
        public void construct() {
            counter = 0;
        }
        
        public Integer getCounter() {
            return counter;
        }
        
        @Override
        public void perform(int x, int y) {}

        @Override
        public boolean check() {
            counter++;
            return false;
        }
        
        @Override
        public void before() {
        }

        @Override
        public void after() {
        }
    }
}