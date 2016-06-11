
import org.junit.Test;
import planet.cells.GeoCell;
import planet.enums.Layer;
import planet.worlds.TestWorld;

import static org.junit.Assert.*;
import planet.cells.HydroCell;
import planet.surface.Stratum;


/**
 * Does basic unit testing on the GeoCell class.
 * @author Richard DeSilvey
 */
public class GeoCellTest {
    
    private static final int WORLD_SIZE = 100;
    private static TestWorld testWorld;
    
    static {
        testWorld = new TestWorld(1, WORLD_SIZE);
    }
    
    /**
     * Tests the make sure the cell has a 0 density, assumes there is no
     * strata. This is mainly a divide by 0 test.
     */
    @Test
    public void zeroGeoDensityTest(){

        GeoCell testCell = testWorld.getSurface().getCellAt(0, 0);
        
        Float actualDensity = testCell.getGeoDensity();
        Float expectedDensity = 0f;
        
        assertEquals("The density should be 0", expectedDensity, actualDensity);
    }
    
    /**
     * Tests the make sure the cell has a 0 density, assumes there is no
     * strata or ocean. This is mainly a divide by 0 test.
     */
    @Test
    public void zeroDensityTest(){
        
        GeoCell testCell = testWorld.getSurface().getCellAt(0, 0);
        
        Float actualDensity = testCell.getDensity();
        Float expectedDensity = 0f;
        
        assertEquals("The density should be 0", expectedDensity, actualDensity);
        
    }
    
    /**
     * Tests for correct density calculation without oceans.
     */
    @Test
    public void calculateDensityTest(){
        
        GeoCell testCell = testWorld.getSurface().getCellAt(10, 10);
        
        Layer type = Layer.BASALT;
        float totalMass = 1000;
        float totalVolume = totalMass / type.getDensity();
        
        Float expectedDensity = totalMass / totalVolume;
        
        testCell.add(type, totalMass, true);
        Float actualDensity = testCell.getGeoDensity();
        
        assertEquals(expectedDensity, actualDensity);
    }
    
    /**
     * Performs a test on a cell by adding two types of stratum and checks to
     * make sure that the density, volume, and mass are correct.
     * Volume, density, and height are tested in ranges with specified
     * deviations.
     */
    @Test
    public void strataTest() {

        boolean inRange;

        float volumeRangeDiff = 0.0000005f;
        float densityRangeDiff = 0.0005f;
        float heightRangeDiff = 0.000005f;
        long cellBase = testWorld.getCellArea();

        GeoCell testCell = testWorld.getSurface().getCellAt(50, 50);

        testCell.add(Layer.GRANITE, 10000, true);
        testCell.add(Layer.BASALT, 500, true);

        Float expectedTotalMass = 10500f;
        Float actualTotalMass = testCell.getTotalMass();

        assertEquals("Total masses didn't match", expectedTotalMass, actualTotalMass);

        Float actualTotalVolume = testCell.getTotalVolume();
        Float expectedTotalVolume = 3.7514535f;

        Float actualDensity = testCell.getGeoDensity();
        Float expectedDensity = expectedTotalMass / expectedTotalVolume;

        inRange = rangeTest(densityRangeDiff, actualDensity, expectedDensity);
        assertTrue("Density is out of accepted range", inRange);

        inRange = rangeTest(volumeRangeDiff, actualTotalVolume, expectedTotalVolume);
        assertTrue("Volume is out of accepted range", inRange);

        Float expectedHeight = expectedTotalMass / (cellBase * expectedDensity);

        Float actualHeight = testCell.getHeight();

        inRange = rangeTest(heightRangeDiff, actualHeight, expectedHeight);
        assertTrue("Height is out of accepted range", inRange);
        
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
        
        GeoCell testCell = testWorld.getSurface().getCellAt(25, 25);

        testCell.add(Layer.GRANITE, 10000, true);
        testCell.add(Layer.BASALT, 500, true);
        
        Float expectedRemoval = 550f;
        Float removed = testCell.remove(expectedRemoval, false, true);
        
        assertEquals(incorrectAmtsMsg, expectedRemoval, removed);
        
        Layer topLayer = testCell.peekTopStratum().getLayer();
        Layer expectedLayer = Layer.GRANITE;
        
        assertEquals(incorrectLayerMsg, expectedLayer, topLayer);
        
        Stratum top = testCell.peekTopStratum();
        Float expectedMass = 9950f;
        Float actualMass = top.getMass();
        
        assertEquals("The top layer's mass is incorrect", expectedMass, actualMass);
        
        testCell.add(Layer.GRAVEL, 1000, false);
        expectedRemoval = 9950f;
        removed = testCell.remove(expectedRemoval, false, true);
        
        assertEquals(incorrectAmtsMsg, expectedRemoval, removed);
        
        top = testCell.peekTopStratum();
        expectedLayer = Layer.GRAVEL;
        topLayer = top.getLayer();
        
        assertEquals(incorrectLayerMsg, expectedLayer, topLayer);
    }

    /**
     * Tests the removal of multiple strata.
     */
    @Test
    public void multipleLayerRemovalTest(){
        
        Layer strata[] = {Layer.BASALT, Layer.GRANITE, Layer.SANDSTONE, Layer.SHALE,
                          Layer.BASALT, Layer.GRANITE, Layer.SANDSTONE, Layer.SHALE};
        GeoCell testCell = testWorld.getSurface().getCellAt(30, 30);
        
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

    /**
     * Performs simple add and remove of molten lava to a test cell.
     */
    @Test
    public void moltenLavaTest(){
        GeoCell testCell = testWorld.getSurface().getCellAt(20, 10);
        
        testCell.putMoltenRockToSurface(1000);
        Float moltenMass = testCell.getMoltenRockFromSurface();
        Float expected = 1000f;
        assertEquals("Mass incorrect", expected, moltenMass);
        
        testCell.removeAllMoltenRock();
        
        assertTrue("No lava should exist", testCell.getMoltenRockFromSurface() == 0);
    }
   
    /**
     * Tests the hasOcean method.
     */
    @Test
    public void oceanTest(){
        GeoCell testCell = testWorld.getSurface().getCellAt(0, 10);
        assertFalse(testCell.hasOcean());
        
        HydroCell hydro = (HydroCell)testCell;
        hydro.addOceanMass(10000);
        
        assertTrue(testCell.hasOcean());
    }
    
    /**
     * Performs a convergence test on the height of a test cell.
     */
    @Test
    public void buyoancyTest(){
        float heightRangeDiff = 0.05f;
        GeoCell testCell = testWorld.getSurface().getCellAt(15, 20);
        
        testCell.add(Layer.BASALT, 1000000, true);
        
        for (int i = 0; i < 10; i++){
            testCell.updateHeight();
        }
        
        float postHeight = testCell.getHeight();
        testCell.recalculateHeight();
        float recalculatedHeight = testCell.getHeight();
        
        boolean inRange = rangeTest(heightRangeDiff, postHeight, recalculatedHeight);
        assertTrue("The updated height is not in expected range", inRange);
    }
    
}
