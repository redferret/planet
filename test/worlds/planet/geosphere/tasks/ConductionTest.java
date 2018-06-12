
package worlds.planet.geosphere.tasks;

import worlds.planet.geosphere.tasks.heatmanagement.conduction.Conduction;
import org.junit.Test;
import worlds.planet.PlanetCell;
import static org.junit.Assert.*;

/**
 *
 * @author Richard
 */
public class ConductionTest {
  
  private final SurfaceMapMock surfaceMock;
  private final ConductionImpl conductionTest;
  
  public ConductionTest() {
    SurfaceThreadsMock threadsMock = new SurfaceThreadsMock();
    surfaceMock = new SurfaceMapMock(8, threadsMock);
    conductionTest = new ConductionImpl(surfaceMock);
    PlanetCell.length = 100000;
    PlanetCell.area = PlanetCell.length * PlanetCell.length;
    
    
  }


  @Test
  public void testCalculateHeatConductance() {
    float[] K_vals = getConductances();
    
    float[] expectedValues = new float[]{1250.0f, 1250.0f, 1250.0f, 1250.0f, 8812.909f, 8812.909f};
    for (int k = 0; k < 6; k++){
      assertTrue(K_vals[k] == expectedValues[k]);
    }
  }
  
  private float[] getConductances() {
    int x = 0;
    int y = 0;
    MockCell top = new MockCell(x, y, 500, 2.5f);
    MockCell bottom = new MockCell(x, y, 3000, 2.5f);
    return conductionTest.calculateHeatConductance(x, y, 2000, top, bottom);
  }

  @Test
  public void testCalculateHeatFlow() {
    float[] K_vals = getConductances();
    float[] T_vals = new float[]{350, 1000, 768, 400, 500, 3000};
    float heatFlow = conductionTest.calculateHeatFlow(K_vals, T_vals, 4000);
    assertTrue(heatFlow < 0);
    heatFlow = conductionTest.calculateHeatFlow(K_vals, T_vals, 0);
    assertTrue(heatFlow > 0);
    
  }

  @Test
  public void testCalculateNewTemperature() {
    float[] K_vals = getConductances();
    float[] T_vals = new float[]{350, 1000, 768, 400, 500, 3000};
    float heatFlow = conductionTest.calculateHeatFlow(K_vals, T_vals, 4000);
    float newTemp = conductionTest.calculateNewTemperature(heatFlow, 4000, K_vals, 2000);
    assertTrue(newTemp == 1502.3848f);
  }

  public class ConductionImpl extends Conduction {

    public ConductionImpl(SurfaceMapMock mock) {
      super(mock);
    }

    @Override
    public void before() throws Exception {}

    @Override
    public void perform(int x, int y) throws Exception {
    
    }

    @Override
    public void after() throws Exception {}
  }
  
}
