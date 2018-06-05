
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
  public float getVerticalResistence() {return 0;}

}
