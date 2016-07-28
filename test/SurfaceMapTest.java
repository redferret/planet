
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import engine.cells.Cell;
import engine.surface.SurfaceMap;
import engine.util.TaskAdapter;
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

    private static final int MAP_SIZE = 5, DELAY = 1, THREAD_COUNT = 1,
            CELL_COUNT = MAP_SIZE * MAP_SIZE;
    private CountDownLatch latch;

    private TestSurface testSurface;

    @Before
    public void setUp() {
        latch = new CountDownLatch(CELL_COUNT);
        testSurface = new TestSurface(MAP_SIZE, DELAY, THREAD_COUNT, latch);
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

        testSurface.startSurfaceThreads();
        testSurface.playSurfaceThreads();

        boolean signaled = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Latch was never signaled", signaled);

        String failedMsg = "A cell was never updated";
        for (int i = 0; i < CELL_COUNT; i++) {
            TestCell cell = testSurface.getCellAt(i);
            boolean cellUpdated = cell.isUpdated();
            assertTrue(failedMsg, cellUpdated);
        }
    }

    @Test
    public void getCellMethodTest() {
        TestCell cell = testSurface.getCellAt(5);
        int expectedX = 0, expectedY = 1;

        assertTrue(expectedX == cell.getX());
        assertTrue(expectedY == cell.getY());
    }

    @Test
    public void calculateIndexTest() {
        testIndex(1, 1, 3, 4);
        testIndex(1, 1, 4, 5);
        testIndex(1, 2, 3, 7);
        testIndex(9, 3, 4, 21);
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

    private void testXY(int testIndex, int testWidth, Integer expectedX, Integer expectedY) {
        Integer testX = TestSurface.calcX(testIndex, testWidth);
        Integer testY = TestSurface.calcY(testIndex, testWidth);
        
        assertEquals(expectedX, testX);
        assertEquals(expectedY, testY);
    }

    @After
    public void tearDown() {
        testSurface.killAllThreads();
        testSurface.kill();
    }

}

class TestSurface extends SurfaceMap<TestCell> {

    private CountDownLatch latch;
    
    public TestSurface(int planetWidth, int delay, int threadCount, CountDownLatch latch) {
        super(planetWidth, delay, "Test Surface", threadCount);
        this.latch = latch;
        setupThreads(threadCount, delay);
        setupDefaultMap(planetWidth, threadCount);
        addTaskToThreads(this.new SurfaceTask());
    }

    @Override
    public void reset() {
        buildMap();
    }

    @Override
    public TestCell generateCell(int x, int y) {
        return new TestCell(x, y, latch);
    }

    class SurfaceTask extends TaskAdapter {

        @Override
        public void perform(int x, int y) {
            getCellAt(x, y).update();
        }
    }

}

class TestCell extends Cell {

    private CountDownLatch latch;
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
    }

    @Override
    public List<Integer[]> render(List<Integer[]> settings) {
        return settings;
    }

}
