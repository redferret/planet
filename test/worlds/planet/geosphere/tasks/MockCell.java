
package worlds.planet.geosphere.tasks;

import engine.surface.Cell;

/**
 *
 * @author Richard
 */
public class MockCell extends Cell {

  private final float testConductivity;
  
  public MockCell(int x, int y, float testTemperature, float testConductivity) {
    super(x, y, 0);
    this.testConductivity = testConductivity;
  }


  @Override
  public float getHeatCapacity() {
    return testConductivity;
  }

  @Override
  public float getBCR() {return 0;}

  @Override
  public float getHCR() {
    throw new UnsupportedOperationException("Not supported."); 
  }

  @Override
  public float getTCR() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public float getZLength() {
    throw new UnsupportedOperationException("Not supported."); 
  }

}
