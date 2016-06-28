

import java.util.concurrent.CyclicBarrier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import engine.util.Boundaries;
import engine.surface.SurfaceThread;
import engine.util.Task;
import engine.util.TaskFactory;

/**
 * Tests the functionality of the TaskFactory by proving each thread will
 * have their own instance of a Task.
 * @author Richard DeSilvey
 */
public class TaskFactoryTest {
    
    private static final Boundaries BOUNDS = new Boundaries(0, 1, 0, 1);
    private CyclicBarrier waitingGate;
    private SurfaceThread testThread, secondTestThread;
    private MyFactory factory;
    
    @Before
    public void setUp() {
        factory = new MyFactory();
        waitingGate = new CyclicBarrier(1);
        testThread = new SurfaceThread(1, BOUNDS, "Test Thread 1", waitingGate);
        secondTestThread = new SurfaceThread(1, BOUNDS, "Test Thread 2", waitingGate);
    }
    
    /**
     * Tests that each thread has it's own instance of a Task. The expected
     * outcome is that the counter inside each task only gets incremented
     * once.
     */
    @Test
    public void sharedResourcesTest(){
        MyFactory.MyTask testTask1 = (MyFactory.MyTask) factory.buildTask();
        MyFactory.MyTask testTask2 = (MyFactory.MyTask) factory.buildTask();
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
        MyFactory.MyTask testTask = (MyFactory.MyTask) factory.buildTask();
        testThread.addTask(testTask);
        secondTestThread.addTask(testTask);
        
        testThread.update();
        secondTestThread.update();
        
        assertTrue(testTask.getCounter() > 1);
    }
    
    @After
    public void tearDown() {
        testThread.kill();
    }
    
}

class MyFactory implements TaskFactory {

    @Override
    public Task buildTask() {
        return new MyTask();
    }
    
    public class MyTask implements Task {

        private Integer counter;
        
        public MyTask() {
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
        
    }
}