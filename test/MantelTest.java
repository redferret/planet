import org.junit.*;

import worlds.planet.PlanetCell;
import worlds.planet.PlanetSurface;
import worlds.planet.TestWorld;
import worlds.planet.geosphere.Layer;
import worlds.planet.geosphere.LayerMaterial;
import static org.junit.Assert.*;
import worlds.planet.Planet;
import static worlds.planet.Planet.instance;
import worlds.planet.geosphere.tasks.MantleHeatLoss;

/**
 *
 * @author Richard
 */
public class MantelTest {
  private static final int WORLD_SIZE = 10;
  private static final TestWorld testWorld;

  static {
    TestWorld.CELL_LENGTH = 100000;
    TestWorld.SURFACE_THREAD_DELAY = 1;
    testWorld = new TestWorld(WORLD_SIZE, 1);
  }
  
  @Test
  public void testMantelCooling() throws InterruptedException {
    PlanetSurface surface = testWorld.getSurface();
    instance().setTimescale(Planet.TimeScale.Geological);
    PlanetCell cell = surface.getCellAt(0, 0);
    
    LayerMaterial m1 = new LayerMaterial("Basalt", 6E16f, 0.84f, 1900f, 1f, null);
    Layer layer = new Layer();
    layer.addMaterial(m1);
    cell.addToStrata(layer, true);
    
    MantleHeatLoss testTask = new MantleHeatLoss(surface);
    testTask.perform(0, 0);
    System.out.println("Cell Thickness " + cell.getStrataThickness());
    System.out.println("Cell Height " + cell.getHeight());
    System.out.println("Mantel After Cooling " + cell.getMantleTemperature());
  }
}
