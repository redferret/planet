
import org.junit.Test;
import planet.surface.GeoCell;
import planet.surface.Layer;
import planet.surface.test.World;

import static org.junit.Assert.*;
import planet.surface.HydroCell;

/**
 * Does basic unit testing on the GeoCell class.
 * @author Richard
 */
public class GeoCellTest {
    
    private static final int WORLD_SIZE = 100;
    private static World testWorld;
    
    static {
        testWorld = new World(WORLD_SIZE);
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
     * Performs simple add and remove of molten lava to a test cell.
     */
    @Test
    public void moltenLavaTest(){
        GeoCell testCell = testWorld.getSurface().getCellAt(20, 10);
        
        testCell.putMoltenRockToSurface(1000);
        testCell.removeAllMoltenRock();
        
        assertTrue(testCell.getMoltenRockFromSurface() == 0);
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
    
}
