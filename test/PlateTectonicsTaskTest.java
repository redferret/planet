
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import engine.util.Point;
import worlds.planet.geosphere.tasks.PlateTectonicsTask;

import static org.junit.Assert.*;

/**
 * @author Richard DeSilvey
 *
 */
public class PlateTectonicsTaskTest {

    private PlateTectonicsTask testTask;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        testTask = new PlateTectonicsTask() {
            public void perform() {
            }

            public void before() {
            }

            public void after() {
            }
        };
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
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

    /**
     * Builds a plate and tests to make sure a point exists in the list of
     * points returned by the method.
     */
    @Test
    public void buildPlateTest(){
    	Point center = new Point(30, 30);
    	List<Point> plate = buildPlateWithNullTest(center, 10);
    	
    	Point expectedPoint = new Point(22, 20);
    	
    	boolean containsTestPoint = false;
    	for (Point point : plate) {
    		if (point.equals(expectedPoint)){
    			containsTestPoint = true;
    			break;
    		}
    	}
    	assertTrue("Test point not contained in list", containsTestPoint);
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
