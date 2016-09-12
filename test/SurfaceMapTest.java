
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.istack.internal.logging.Logger;

import engine.cells.Cell;
import engine.surface.SurfaceMap;
import engine.util.task.BasicTask;
import engine.util.task.TaskAdapter;
import static org.junit.Assert.*;

/**
 * Performs tests on the SurfaceMap class. The test creates a TestSurface which
 * holds a default map implementation that contains TestCells. A count down
 * latch is used to track each cell update made by a SurfaceThread using
 * a SurfaceTask.
 *
 * @author Richard
 */
public class SurfaceMapTest {

    private static final int MAP_SIZE = 4, SURFACE_DELAY = 1, THREAD_COUNT = 2,
            CELL_COUNT = MAP_SIZE * MAP_SIZE;
    private CountDownLatch latch;

    private TestSurface testSurface;

    @Before
    public void setUp() {
        latch = new CountDownLatch(CELL_COUNT);
        testSurface = new TestSurface(MAP_SIZE, SURFACE_DELAY, THREAD_COUNT, latch);
        testSurface.reset();
    }

    /**
     * Performs a single pass over the map that contains TestCells, each cell
     * will be flagged as being updated.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void surfaceMapRunningTest() throws InterruptedException {

    	testSurface.addTaskToThreads(testSurface.new SurfaceTask());
    	
        testSurface.startSurfaceThreads();
        testSurface.playSurfaceThreads();

        boolean signaled = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Latch was never signaled", signaled);

        String failedMsg = "A cell was never updated: ";
        for (int i = 0; i < CELL_COUNT; i++) {
            TestCell cell = testSurface.getCellAt(i);
            boolean cellUpdated = cell.isUpdated();
            testSurface.release(cell);
            assertTrue(failedMsg + cell, cellUpdated);
        }
    }

    @Test
    public void starvationTest() throws InterruptedException {
    	testSurface.addTaskToThreads(testSurface.new StarvationTask());
    	
    	testSurface.startSurfaceThreads();
        testSurface.playSurfaceThreads();
        
        boolean signaled = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Latch was never signaled", signaled);
    }
    
    @Test
    public void calculateIndexTest() {
        
        for (int testX = 0; testX < 100; testX++){
            for (int testY = 0; testY < 100; testY++){
                for (int testWidth = 0; testWidth < 100; testWidth++){
                    testCaseForIndexTesting(testX, testY, testWidth);
                }
            }
        }
    }

    @Test
    public void calculateXYTest() {
        int testIndex = 11;
        int testWidth = 3;
        Integer expectedX = 2, expectedY = 3;
        
        testXY(testIndex, testWidth, expectedX, expectedY);
        
        testIndex = 33;
        testWidth = 10;
        expectedX = 3; expectedY = 3;
        
        testXY(testIndex, testWidth, expectedX, expectedY);
    }
    
    private void testCaseForIndexTesting(int testX, int testY, int testWidth) {
        int expectedIndex = testX + (testY * testWidth);
        testIndex(testX, testY, testWidth, expectedIndex);
    }
    
    @After
    public void tearDown() {
        testSurface.killAllThreads();
        testSurface.kill();
    }
    
    /**
     * Performs an assertion with the given expectedIndex based on the three
     * parameters testX, testY, and the WIDTH. The assertion will fail if the
     * expected index isn't equal to the SurfaceMap#calcIndex return value.
     *
     * @param testX The test X coordinate
     * @param testY The test y coordinate
     * @param WIDTH The width of the map
     * @param expectedIndex The expected calculated index
     */
    private void testIndex(int testX, int testY, final int WIDTH, Integer expectedIndex) {
        Integer index = TestSurface.calcIndex(testX, testY, WIDTH);
        assertEquals(expectedIndex, index);
    }
    
    private void testXY(int testIndex, int testWidth, Integer expectedX, Integer expectedY) {
        Integer testX = TestSurface.calcX(testIndex, testWidth);
        Integer testY = TestSurface.calcY(testIndex, testWidth);
        
        assertEquals(expectedX, testX);
        assertEquals(expectedY, testY);
    }

}

class TestSurface extends SurfaceMap<TestCell> {

    private CountDownLatch latch;
    
    public TestSurface(int planetWidth, int surfaceThreadDelay, int threadCount, 
            CountDownLatch latch) {
        super(planetWidth, surfaceThreadDelay, threadCount);
        this.latch = latch;
        setupThreads(threadCount, surfaceThreadDelay);
        setupDefaultMap(planetWidth, threadCount);
    }

    @Override
    public void reset() {
        buildMap();
    }

    @Override
    public TestCell generateCell(int x, int y) {
        return new TestCell(x, y, latch);
    }

    public class StarvationTask extends BasicTask {

		@Override
		public void before() {
		}

		@Override
		public void perform() {
			TestCell cell = getCellAt(0, 0);
			cell.update();
			release(cell);
		}

		@Override
		public void after() {
		}
    	
    }
    
    public class SurfaceTask extends TaskAdapter {

        @Override
        public void before() {
        }

        @Override
        public void perform(int x, int y) {
            TestCell cell = getCellAt(x, y);
            cell.update();
            release(cell);
        }

        @Override
        public void after() {
        }
    }

}

class TestCell extends Cell {

    private CountDownLatch latch;
    private CountDownLatch wait;
    
    /**
     * Boolean flag to be used to check if this cell was called by a thread
     * within the SurfaceMap.
     */
    private boolean updated;

    public TestCell(int x, int y, CountDownLatch latch) {
        super(x, y);
        this.latch = latch;
        updated = false;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void update() {
        latch.countDown();
        updated = true;
        try {
        	wait = new CountDownLatch(1);
			wait.await(125, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Logger.getLogger(Thread.currentThread().getClass())
			.log(Level.SEVERE, "Thread was rudely interrupted " + 
					Thread.currentThread().getName());
		}
    }

    @Override
    public List<Integer[]> render(List<Integer[]> settings) {
        return settings;
    }

}
