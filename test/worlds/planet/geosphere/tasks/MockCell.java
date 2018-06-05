
package worlds.planet.geosphere.tasks;

import engine.surface.Cell;

/**
 *
 * @author Richard
 */
public class MockCell extends Cell {

  private final float testTemperature, testConductivity;
  
  public MockCell(int x, int y, float testTemperature, float testConductivity) {
    super(x, y);
    this.testTemperature = testTemperature;
    this.testConductivity = testConductivity;
  }


  @Override
  public float getHeatConductivity() {
    return testConductivity;
  }

  @Override
  public void addToTemperatureFlux(float flux) {}

  @Override
  public float getTemperature() {
    return testTemperature;
  }
  
}
