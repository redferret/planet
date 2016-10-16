
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.util.Point;
import engine.util.task.BasicTask;
import engine.util.task.TaskAdapter;
import java.util.logging.Logger;
import static org.junit.Assert.*;

/**
 * Performs tests on the SurfaceMap class. The test creates a TestSurface which
 * holds a default map implementation that contains TestCells. A count down
 * latch is used to track each cell update made by a SurfaceThread using a
 * SurfaceTask. Additionally there is also tests on tasks that test the
 * functionality of starvation and release of resources in the test surface.
 *
 * @author Richard DeSilvey
 */
public class SurfaceMapAndTaskTest {

    private static final int MAP_SIZE = 2, SURFACE_DELAY = 1, THREAD_COUNT = 2,
            CELL_COUNT = MAP_SIZE * MAP_SIZE;
    
    /**
     * The synchronization latch for the test threads.
     */
    private CountDownLatch latch;

    /**
     * The test surface for this test file.
     */
    private TestSurface testSurface;

    @Before
    public void setUp() {
    	latch = new CountDownLatch(CELL_COUNT);
        testSurface = new TestSurface(MAP_SIZE, SURFACE_DELAY, THREAD_COUNT, latch);
        testSurface.reset();
    }

    @After
    public void tearDown() {
        testSurface.killAllThreads();
        testSurface.kill();
    }
    
    @Test
    public void totalCellCountTest(){
    	int numberOfCells = testSurface.getTotalNumberOfCells();
    	assertTrue("Number of cells don't match", CELL_COUNT == numberOfCells);
    }
    
    @Test
    public void mapSizeTest(){
    	int mapWidth = testSurface.getGridWidth();
    	assertTrue("Number of cells don't match", MAP_SIZE == mapWidth);
    }
    
    @Test
    public void generateCellTest(){
    	int x = 45;
    	int y = 12;
    	
    	Cell testCell = testSurface.generateCell(x, y);
    	
    	assertNotNull("The generated cell is null", testCell);
    	
    	assertTrue("The cell generated is not a proper instance", testCell instanceof TestCell);
    	
    	int testX = testCell.getX();
    	int testY = testCell.getY();
    	
    	assertTrue("x position is incorrect", testX == x);
    	assertTrue("y position is incorrect", testY == y);
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

        startAndRunTestSurface();

        boolean signaled = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Latch was never signaled", signaled);

        String failedMsg = "A cell was never updated: ";
        for (int i = 0; i < CELL_COUNT; i++) {
            TestCell cell = testSurface.waitForCellAt(i);
            boolean cellUpdated = cell.isUpdated();
            testSurface.release(cell);
            assertTrue(failedMsg + cell, cellUpdated);
        }
    }

    /**
     * Tests that no starvation happens to any of the threads accessing the
     * same resource to change it.
     * @throws InterruptedException 
     */
    @Test
    public void noStarvationTest() throws InterruptedException {
        testSurface.addTaskToThreads(testSurface.new NoStarvationTask());

        startAndRunTestSurface();

        boolean signaled = latch.await(6, TimeUnit.SECONDS);
        assertTrue("Resource was never released causing starvation", signaled);
        
        String failedMsg = "Cell count incorrect: ";
        TestCell cell = testSurface.waitForCellAt(0);
        Integer counter = cell.getCounter();
        testSurface.release(cell);
        assertEquals(failedMsg + cell, new Integer(4), counter);
    }

    /**
     * Tests that starvation happens when a resource is not released.
     * @throws InterruptedException 
     */
    @Test
    public void starvationTest() throws InterruptedException {
        testSurface.addTaskToThreads(testSurface.new StarvationTask());

        startAndRunTestSurface();

        boolean signaled = latch.await(1250, TimeUnit.MILLISECONDS);
        assertTrue("Starvation never occured", !signaled);
        
    }
    
    @Test
    public void calculateIndexTest() {
        for (int testX = 0; testX < 100; testX++) {
            for (int testY = 0; testY < 100; testY++) {
                for (int testWidth = 0; testWidth < 100; testWidth++) {
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
        expectedX = 3;
        expectedY = 3;

        testXY(testIndex, testWidth, expectedX, expectedY);
    }

    /**
     * Tests the method that gets a list of cells that will be only available
     * to the calling thread. 
     */
    @Test
    public void getCellsWithPointsTest(){
        Point[] cellPoints = {new Point(0,0), new Point(0,1),
                              new Point(1,0), new Point(1,1)};
        List<TestCell> cells = testSurface.waitForCells(cellPoints);
        
        assertNotNull("Nothing was returned", cells);
        
        TestCell[] cellArray = cells.toArray(new TestCell[cells.size()]);
        for (int i = 0; i < cellArray.length; i++){
            Point testPoint = cellArray[i].getPosition();
            Point expected = cellPoints[i];
            assertEquals("Cell Position doesn't match", expected, testPoint);
        }
        testSurface.release(cells.toArray(new TestCell[cellArray.length]));
    }
    
    /**
     * Tests the method that gets a list of cells that will be only available
     * to the calling thread. 
     */
    @Test
    public void getCellsWithIndexesTest(){
        int[] cellPoints = {0, 1, 2, 3};
        List<TestCell> cells = testSurface.waitForCells(cellPoints);
        
        assertNotNull("Nothing was returned", cells);
        
        TestCell[] cellArray = cells.toArray(new TestCell[cells.size()]);
        for (int i = 0; i < cellArray.length; i++){
            Point testPoint = cellArray[i].getPosition();
            Integer testIndex = SurfaceMap.calcIndex((int)testPoint.getX(), (int)testPoint.getY(), MAP_SIZE);
            Integer expectedIndex = cellPoints[i]; 
            assertEquals("Cell Indexes don't match", expectedIndex, testIndex);
        }
        testSurface.release(cells.toArray(new TestCell[cellArray.length]));
    }
    
    private void startAndRunTestSurface(){
    	testSurface.startSurfaceThreads();
        testSurface.playSurfaceThreads();
    }
    
    private void testCaseForIndexTesting(int testX, int testY, int testWidth) {
        int expectedIndex = testX + (testY * testWidth);
        testIndex(testX, testY, testWidth, expectedIndex);
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
        super(planetWidth, surfaceThreadDelay);
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

    /**
     * Deliberately starve threads by not releasing the resource. 
     */
    public class StarvationTask extends BasicTask {

        @Override
        public void perform() {
            TestCell cell = waitForCellAt(0);
            cell.count();
        }

        @Override
        public void before() {
        }

        @Override
        public void after() {
        }
        
    }
    
    /**
     * Demonstrates proper use of accessing and releasing a resource.
     */
    public class NoStarvationTask extends BasicTask {

        @Override
        public void before() {
        }

        @Override
        public void perform() {
            TestCell cell = waitForCellAt(0);
            cell.count();
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
            TestCell cell = waitForCellAt(x, y);
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
    private int counter;

    /**
     * Boolean flag to be used to check if this cell was called by a thread
     * within the SurfaceMap.
     */
    private boolean updated;

    public TestCell(int x, int y, CountDownLatch latch) {
        super(x, y);
        this.latch = latch;
        updated = false;
        counter = 0;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void count(){
        counter++;
        latch.countDown();
        try {
            Logger.getLogger("Test").log(Level.INFO, "Counted by {0} to value {1}"
                    , new Object[]{Thread.currentThread().getName(), counter});
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Logger.getLogger("Test").log(Level.INFO, "Shutting down "
                    , Thread.currentThread().getName());
        }
    }
    
    public int getCounter() {
        return counter;
    }
    
    public void update() {
        latch.countDown();
        updated = true;
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            System.err.println("Thread was rudely interrupted "
                    + Thread.currentThread().getName());
        }
    }

    @Override
    public List<Integer[]> render(List<Integer[]> settings) {
        return settings;
    }

}
