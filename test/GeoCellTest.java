
import engine.util.Tools;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import worlds.planet.geosphere.GeoCell;
import worlds.planet.enums.Layer;
import worlds.planet.TestWorld;
import static org.junit.Assert.*;
import worlds.planet.hydrosphere.HydroCell;
import worlds.planet.PlanetCell;
import worlds.planet.geosphere.Stratum;


/**
 * Does basic unit testing on the GeoCell class.
 * @author Richard DeSilvey
 */
public class GeoCellTest {
    
    private static final int WORLD_SIZE = 100;
    private TestWorld testWorld;
    
    
    @Before
    public void setUp(){
    	testWorld = new TestWorld(1, WORLD_SIZE);
    }
    
    @After
    public void tearDown(){
    	testWorld.getSurface().killAllThreads();
    	testWorld.getSurface().kill();
    }
    
    /**
     * Tests the make sure the cell has a 0 density, assumes there is no
     * strata. This is mainly a divide by 0 test.
     */
    @Test
    public void zeroGeoDensityTest(){

        GeoCell testCell = testWorld.getSurface().waitForCellAt(0, 0);
        
        Float actualDensity = testCell.getGeoDensity();
        Float expectedDensity = 0f;
        
        assertEquals("The density should be 0", expectedDensity, actualDensity);
        testWorld.getSurface().release((PlanetCell) testCell);
    }
    
    /**
     * Tests the make sure the cell has a 0 density, assumes there is no
     * strata or ocean. This is mainly a divide by 0 test.
     */
    @Test
    public void zeroDensityTest(){
        
        GeoCell testCell = testWorld.getSurface().waitForCellAt(0, 0);
        
        Float actualDensity = testCell.getDensity();
        Float expectedDensity = 0f;
        
        assertEquals("The density should be 0", expectedDensity, actualDensity);
        testWorld.getSurface().release((PlanetCell) testCell);
    }
    
    /**
     * Tests for correct density calculation without oceans.
     */
    @Test
    public void calculateDensityTest(){
        
        GeoCell testCell = testWorld.getSurface().waitForCellAt(10, 10);
        
        Layer type = Layer.BASALT;
        float totalMass = 1000;
        float totalVolume = totalMass / type.getDensity();
        
        Float expectedDensity = totalMass / totalVolume;
        
        testCell.add(type, totalMass, true);
        Float actualDensity = testCell.getGeoDensity();
        
        assertEquals(expectedDensity, actualDensity);
        testWorld.getSurface().release((PlanetCell) testCell);
    }


    /**
     * Tests removal of the strata with the special removal case of removing
     * from the strata where the removal amount exceeds the top or bottom
     * layer being removed from as well as checking to make sure that
     * zero mass layers don't exist.
     */
    @Test
    public void removeFromStrataTest() {
        
        String incorrectAmtsMsg = "The amount removed is incorrect";
        String incorrectLayerMsg = "The top layer is incorrect";
        
        GeoCell testCell = testWorld.getSurface().waitForCellAt(25, 25);

        testCell.add(Layer.RHYOLITE, 10000, true);
        testCell.add(Layer.BASALT, 500, true);
        
        Float expectedRemoval = 550f;
        Float removed = testCell.remove(expectedRemoval, false, true);
        
        assertEquals(incorrectAmtsMsg, expectedRemoval, removed);
        
        Layer topLayer = testCell.peekTopStratum().getLayer();
        Layer expectedLayer = Layer.RHYOLITE;
        
        assertEquals(incorrectLayerMsg, expectedLayer, topLayer);
        
        Stratum top = testCell.peekTopStratum();
        Float expectedMass = 9950f;
        Float actualMass = top.getMass();
        
        assertEquals("The top layer's mass is incorrect", expectedMass, actualMass);
        
        testCell.add(Layer.RHYOLITE, 1000, false);
        expectedRemoval = 9950f;
        removed = testCell.remove(expectedRemoval, false, true);
        
        assertEquals(incorrectAmtsMsg, expectedRemoval, removed);
        
        top = testCell.peekTopStratum();
        expectedLayer = Layer.RHYOLITE;
        topLayer = top.getLayer();
        
        assertEquals(incorrectLayerMsg, expectedLayer, topLayer);
        testWorld.getSurface().release((PlanetCell) testCell);
    }

    /**
     * Tests the removal of multiple strata.
     */
    @Test
    public void multipleLayerRemovalTest(){
        
        Layer strata[] = {Layer.BASALT, Layer.RHYOLITE, Layer.MAFIC_SANDSTONE, Layer.SHALE,
                          Layer.BASALT, Layer.RHYOLITE, Layer.MAFIC_SANDSTONE, Layer.SHALE};
        GeoCell testCell = testWorld.getSurface().waitForCellAt(30, 30);
        
        for (Layer layer : strata){
            testCell.add(layer, 1000, true);
        }
        
        testCell.remove(3000, false, true);
        
        Layer expectedLayer = Layer.BASALT;
        Layer actualLayer = testCell.peekTopStratum().getLayer();
        
        assertEquals("Incorrect layer", expectedLayer, actualLayer);
        
        testCell.remove(1500, false, true);
        
        expectedLayer = Layer.SHALE;
        actualLayer = testCell.peekTopStratum().getLayer();
        
        assertEquals("Incorrect layer", expectedLayer, actualLayer);
        
        testWorld.getSurface().release((PlanetCell) testCell);
    }
    
    /**
     * Helper method for testing ranges.
     *
     * @param rangeDiff The amount of deviation
     * @param testNumber The outcome
     * @param expectedNumber The expected
     * @return True if the outcome is within an accepted range based on the
     * deviation.
     */
    private boolean rangeTest(Float rangeDiff, Float testNumber, Float expectedNumber) {
        Float minimum = expectedNumber - rangeDiff;
        Float maximum = expectedNumber + rangeDiff;

        return testNumber >= minimum && testNumber <= maximum;
    }

    @Test
    public void insertMassTest(){
        Layer strata[] = {Layer.BASALT, Layer.RHYOLITE, Layer.MAFIC_SANDSTONE, Layer.SHALE};
        GeoCell testCell = testWorld.getSurface().waitForCellAt(20, 20);
        float mass;
        for (Layer layer : strata){
            mass = Tools.calcMass(200, 100, layer);
            testCell.add(layer, mass, true);
        }
        mass = Tools.calcMass(50, 100, Layer.LIMESTONE);
        testCell.addAtDepth(Layer.LIMESTONE, mass, 800);
        
        Float thickness = testCell.getStrataThickness();
        Float expected = 850f;
        assertEquals("", expected, thickness);
        
        testWorld.getSurface().release((PlanetCell) testCell);
    }
    
    /**
     * Tests the functionality of adding similar layer types to the strata.
     */
    @Test
    public void addingToLayersTest(){
        
        GeoCell testCell = testWorld.getSurface().waitForCellAt(10, 23);
        testCell.add(Layer.BASALT, 5000, true);
        testCell.add(Layer.FELSIC_SANDSTONE, 1500, true);
        testCell.add(Layer.FELSIC_SANDSTONE, 250, true);
        Integer expectedLayerCount = 2;
        Integer actualLayerCount = testCell.getStrata().size();
        assertEquals("Layer Count Incorrect", expectedLayerCount, actualLayerCount);
        
        testWorld.getSurface().release((PlanetCell) testCell);
    }
   
    /**
     * Tests the hasOcean method.
     */
    @Test
    public void oceanTest(){
        GeoCell testCell = testWorld.getSurface().waitForCellAt(0, 10);
        assertFalse(testCell.hasOcean());
        
        HydroCell hydro = (HydroCell)testCell;
        hydro.addOceanMass(10000);
        
        assertTrue(testCell.hasOcean());
        
        testWorld.getSurface().release((PlanetCell) testCell);
    }
    
    /**
     * Performs a convergence test on the height of a test cell.
     */
    @Test
    public void buyoancyTest(){
        float heightRangeDiff = 0.05f;
        GeoCell testCell = testWorld.getSurface().waitForCellAt(15, 20);
        
        testCell.add(Layer.BASALT, 1000000, true);
        
        for (int i = 0; i < 10; i++){
            testCell.updateHeight();
        }
        
        float postHeight = testCell.getHeight();
        testCell.recalculateHeight();
        float recalculatedHeight = testCell.getHeight();
        
        boolean inRange = rangeTest(heightRangeDiff, postHeight, recalculatedHeight);
        assertTrue("The updated height is not in expected range", inRange);
        
        testWorld.getSurface().release((PlanetCell) testCell);
    }
    
}
