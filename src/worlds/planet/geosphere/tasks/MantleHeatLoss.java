
package worlds.planet.geosphere.tasks;

import engine.util.task.Task;
import worlds.planet.PlanetCell;
import static worlds.planet.Surface.timeStep;
import worlds.planet.Util;
import worlds.planet.geosphere.GeoCell;
import worlds.planet.geosphere.Geosphere;

/**
 * Mantel Cooling can take place when heat is transfered to the crust above.
 * Depending on the specific heat of the crust on average, the amount
 * of heat the mantel has, and how thick the crust is will all determine how
 * much heat is diffused into the rock above. 
 * @author Richard
 */
public class MantleHeatLoss extends Task {

  private final Geosphere geosphere;

  /**
   * A simple task that allows heat to radiate out vertically. This cooling
   * is based off of inferred flux. The hotter the cell is the more it will
   * release that heat. The rate that this heat is released can trigger
   * volcanoes. 
   */
  public static float mantleSpecificHeat;
  
  static {
    mantleSpecificHeat = 2.6f;
  }
  
  public MantleHeatLoss(Geosphere geosphere) {
    this.geosphere = geosphere;
  }
  
  @Override
  public void before() {
  }

  @Override
  public void perform(int x, int y) {
    GeoCell cell = geosphere.getCellAt(x, y);
    float heatFromMantle = Util.calcHeatRadiation(cell.getMantleTemperature());
    float denom = (cell.getTotalMass() * cell.getSpecificHeat());
    float tempChangeToMantle = heatFromMantle / denom;
    cell.addToMantleHeat(-tempChangeToMantle);
    
  }

  @Override
  public void after() {
  }

  @Override
  public void construct() {
  }

  @Override
  public boolean check() {
    return true;
  }
  
}
