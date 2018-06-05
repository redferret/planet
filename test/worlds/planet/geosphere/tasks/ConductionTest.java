
package worlds.planet.geosphere.tasks;

import org.junit.Test;
import static org.junit.Assert.*;
import worlds.planet.PlanetCell;

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
    int x = 0;
    int y = 0;
    MockCell top = surfaceMock.getCellAt(x, y);
    MockCell bottom = surfaceMock.getCellAt(x, y);
    float[] K_vals = conductionTest.calculateHeatConductance(x, y, 2000, top, bottom);
    
  }

  @Test
  public void testCalculateHeatFlow() {
  }

  @Test
  public void testCalculateNewTemperature() {
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
