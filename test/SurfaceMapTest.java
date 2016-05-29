
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import planet.cells.Cell;
import planet.surface.SurfaceMap;
import planet.util.TaskAdapter;
import static org.junit.Assert.*;

/**
 * Performs tests on the SurfaceMap class.
 * @author Richard
 */
public class SurfaceMapTest {
    
    private static final int MAP_SIZE = 5, DELAY = 1, THREAD_COUNT = 1;
    public static CountDownLatch latch;
    
    private CountDownLatch waitingLatch;
    private TestSurface testSurface;
    
    @Before
    public void setUp() {
        latch = new CountDownLatch(MAP_SIZE * MAP_SIZE);
        testSurface = new TestSurface(MAP_SIZE, DELAY, THREAD_COUNT);
        testSurface.reset();
    }
    
    /**
     * Performs a single pass over the map that contains TestCells, each
     * cell will be flagged as being updated.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void surfaceMapRunningTest() throws InterruptedException{
        waitingLatch = new CountDownLatch(1);
        
        testSurface.startAll();
        
        // Wait for the threads to start up
        waitingLatch.await(1, TimeUnit.SECONDS);
        testSurface.playAll();
        
        boolean signaled = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Latch was never signaled", signaled);
        
        String failedMsg = "A cell was never updated";
        int cellCount = MAP_SIZE * MAP_SIZE;
        for (int i = 0; i < cellCount; i++){
            TestCell cell = testSurface.getCellAt(i);
            boolean cellUpdated = cell.isUpdated();
            assertTrue(failedMsg, cellUpdated);
        }
    }
    
    @Test
    public void getCellMethodTest(){
        TestCell cell = testSurface.getCellAt(3);
        int expectedX = 3, expectedY = 0;
        
        assertTrue(expectedX == cell.getX());
        assertTrue(expectedY == cell.getY());
    }
    
    @After
    public void tearDown() {
        testSurface.killAllThreads();
        testSurface.kill();
    }
    
}

class TestSurface extends SurfaceMap<TestCell> {

    public TestSurface(int planetWidth, int delay, int threadCount) {
        super(planetWidth, delay, "Test Surface", threadCount);
        setupThreads(threadCount, delay);
        addTaskToThreads(new SurfaceTask());
    }

    @Override
    public void reset() {
        setupMap();
    }

    @Override
    public TestCell generateCell(int x, int y) {
        return new TestCell(x, y);
    }
    
    class SurfaceTask extends TaskAdapter {
        @Override
        public void perform(int x, int y) {
            SurfaceMapTest.latch.countDown();
            getCellAt(x, y).update();
        }
    }
    
}

class TestCell extends Cell {

    private boolean updated;
    
    public TestCell(int x, int y) {
        super(x, y);
        updated = false;
    }

    public boolean isUpdated() {
        return updated;
    }
    
    public void update(){
        updated = true;
    }
    
    @Override
    public List<Integer[]> render(List<Integer[]> settings) {
        return settings;
    }
    
}