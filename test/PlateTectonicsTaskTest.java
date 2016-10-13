
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import engine.util.Point;
import engine.util.concurrent.SurfaceThread;
import engine.util.task.Boundaries;
import worlds.planet.PlanetCell;
import worlds.planet.geosphere.tasks.PlateTectonicsTask;
import static org.junit.Assert.*;

/**
 * @author Richard DeSilvey
 *
 */
public class PlateTectonicsTaskTest {

    private PlateTectonicsTask testTask;

    @Before
    public void setUp() {
        testTask = new PlateTectonicsTask() {
            public void perform() {}
            public void before() {}
            public void after() {}
        };
    }

    @After
    public void tearDown() {
    }

    @Test
    public void thrustFoldCrustTest(){
    	fail("Not implemented yet.");
    }
    
    /**
     * Tests to see if a cell is given a reference to
     * a SurfaceThread when a new plate is created. Each task
     * when added to a thread will contain a reference to that thread.
     * This will be used as a way to set the cell's thread reference.
     */
    @Test
    public void setParentTest(){
    	
    	SurfaceThread testThread = new SurfaceThread(1, new Boundaries(0, 1), new CyclicBarrier(1));
        testThread.addTask(testTask);
    	
    	PlanetCell testCell = new PlanetCell(0, 0);
        testTask.setParent(testCell);
        assertEquals("Thread not set", testThread, testCell.getPlateControlThread());
    }
    
    /**
     * Adds a plate and tests to see if it was added.
     */
    @Test
    public void addPlateTest() {
    	Point center = new Point(0, 0);
        List<Point> plate = buildPlateWithNullTest(center, 5);
        testTask.addPlate(plate);
        
        assertEquals("No plate was added", 1, testTask.getNumberOfPlates());
    }

    @Test
    public void removePlateTest(){
    	Point center = new Point(0, 0);
    	for (int i = 1; i <= 10; i++){
	        List<Point> plate = buildPlateWithNullTest(center, 5);
	        testTask.addPlate(plate);
    	}
    	testTask.removePlate(5);
    	testTask.removePlate(1);
    	
    	assertEquals("Plate Removal not evident", 8, testTask.getNumberOfPlates());
    }
    
    /**
     * Builds a plate and tests to make sure a point exists in the list of
     * points returned by the method.
     */
    @Test
    public void buildPlateTest(){
    	Point center = new Point(30, 30);
    	List<Point> plate = buildPlateWithNullTest(center, 10);
    	
    	Point expectedPoint = new Point(36, 33);
    	
    	boolean containsTestPoint = containsPoint(plate, expectedPoint);
    	assertTrue("Test point not contained in list", containsTestPoint);
    }
    
    private boolean containsPoint(List<Point> testPlate, Point testPoint){
        for (Point point : testPlate) {
            if (point.equals(testPoint)){
                return true;
            }
    	}
        return false;
    }
    
    /**
     * Tests to make sure the list returned by the method is not null.
     */
    private List<Point> buildPlateWithNullTest(Point center, int radius) {
        List<Point> plate = testTask.buildPlate(center, radius);
        assertNotNull("The returned list is null", plate);
        return plate;
    }

}
