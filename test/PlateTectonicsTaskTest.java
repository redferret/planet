

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
			public void perform() {}
			public void before() {}
			public void after() {}
		};
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void buildPlateTest() {
		
		List<Point> plate = buildPlateWithNullTest();
		
		
	}

	/**
	 * Tests to make sure the list returned by the method is not null.
	 */
	private List<Point> buildPlateWithNullTest(){
		Point centralCell = new Point(0,0);
		int radius = 1;
		List<Point> plate = testTask.buildPlate(centralCell, radius);
		
		assertNotNull("The returned list is null", plate);
		
		return plate;
	}
	
}
