
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import engine.util.Point;
import engine.util.concurrent.SurfaceThread;
import engine.util.task.Boundaries;
import worlds.planet.PlanetCell;
import worlds.planet.enums.Layer;
import worlds.planet.geosphere.GeoCell;
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
    	fail("Not implemented yet");
    }
    
    @Test
    public void energyTransferTotalInelasticTest(){
    	
    	Point velA = new Point(10, 0);
    	Point velB = new Point(0, 0);
    	PlanetCell[] testCells = setupCellsForEnergyTransferTesting(1000, 1000, velA, velB);
    	
    	PlanetCell cellA = testCells[0];
    	PlanetCell cellB = testCells[1];
    	
    	Point finalVelA = calcEnergyTransferWithNullTest(cellA, cellB, 0);
    	
    	Point expectedVelocityA = new Point(5f, 0);
    	
    	assertEquals("Velocities don't match", expectedVelocityA, finalVelA);
    }
    
    @Test
    public void energyTransferTotalElasticTest(){
    	
    	Point velA = new Point(10, -9);
    	Point velB = new Point(-12, 12);
    	PlanetCell[] testCells = setupCellsForEnergyTransferTesting(2000, 1000, velA, velB);
    	
    	PlanetCell cellA = testCells[0];
    	PlanetCell cellB = testCells[1];
    	
    	Point finalVelA = calcEnergyTransferWithNullTest(cellA, cellB, 1);
    	Point finalVelB = calcEnergyTransferWithNullTest(cellB, cellA, 1);
    	
    	Point expectedVelocityA = new Point(-14000f/3000f, 5);
    	assertEquals("Velocities of A don't match", expectedVelocityA, finalVelA);
    	
    	Point expectedVelocityB = new Point(52000f/3000f, -16);
    	assertEquals("Velocities of B don't match", expectedVelocityB, finalVelB);
    	
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
    	
    	Point expectedPoint = new Point(33, 38);
    	
    	boolean containsTestPoint = containsPoint(plate, expectedPoint);
    	assertTrue("Test point not contained in list", containsTestPoint);
    }
    
    private boolean containsPoint(List<Point> testPlate, Point testPoint){
        return testPlate.stream().anyMatch((point) -> (point.equals(testPoint)));
    }
    
    /**
     * Tests to make sure the list returned by the method is not null.
     */
    private List<Point> buildPlateWithNullTest(Point center, int radius) {
        List<Point> plate = testTask.buildPlate(center, radius);
        assertNotNull("The returned list is null", plate);
        return plate;
    }
    
    private PlanetCell[] setupCellsForEnergyTransferTesting(float massA, float massB, Point velA, Point velB){
    	PlanetCell cellA = new PlanetCell(0, 0);
    	PlanetCell cellB = new PlanetCell(0, 0);
    	
    	GeoCell.cellArea = 1;
    	
    	cellA.add(Layer.BASALT, massA, true);
    	cellB.add(Layer.BASALT, massB, true);
    	
    	cellA.setVelocity(velA);
    	cellB.setVelocity(velB);
    	
    	return new PlanetCell[]{cellA, cellB};
    }
    
    private Point calcEnergyTransferWithNullTest(PlanetCell cellA, PlanetCell cellB, float c){
    	Point finalVel = testTask.calculateEnergyTransfer(cellA, cellB, c);
    	assertNotNull("Returned velocity is Null", finalVel);
    	return finalVel;
    }

}
