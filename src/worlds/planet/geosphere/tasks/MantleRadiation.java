
package worlds.planet.geosphere.tasks;

import engine.util.Delay;
import engine.util.task.Task;
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
public class MantleRadiation extends Task {

  private final Geosphere geosphere;
  private final Delay delay;
  
  
  public MantleRadiation(Geosphere geosphere) {
    this.geosphere = geosphere;
    delay = new Delay(200);
  }
  
  @Override
  public void before() {
  }

  @Override
  public void perform(int x, int y) {
    GeoCell cell = geosphere.getCellAt(x, y);
    float heatFromMantle = Util.calcHeatRadiation(cell.getMantleTemperature());
    float denom = (cell.getTotalMass() * cell.getSpecificHeat());
    denom = denom == 0 ? 1 : denom;
    float tempChangeToMantle = heatFromMantle / denom;
    
    cell.addToMantleHeat(-tempChangeToMantle * 0.1f);
  }

  @Override
  public void after() {
  }

  @Override
  public void construct() {
  }

  @Override
  public boolean check() {
    return delay.check();
  }
  
}
