
package worlds.planet.geosphere.tasks;

import engine.util.Delay;
import engine.util.task.Task;
import worlds.planet.Util;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Lithosphere;
import worlds.planet.geosphere.UpperMantle;

/**
 * Mantel Cooling can take place when heat is transfered to the crust above.
 * Depending on the specific heat of the crust on average, the amount
 * of heat the mantel has, and how thick the crust is will all determine how
 * much heat is diffused into the rock above. 
 * @author Richard
 */
public class UpperMantleRadiation extends Radiation {

  public UpperMantleRadiation(Lithosphere geosphere) {
    super(geosphere);
  }
  
  @Override
  public void before() {
  }

  @Override
  public void perform(int x, int y) {
    UpperMantle upperMantle = geosphere.getCellAt(x, y);
    this.updateRadiation(upperMantle, UpperMantle.UPPER_MANTLE_MASS, 2.2f, (cell) -> {
      return ((UpperMantle)cell).getUpperMantleTemperature();
    },(flux) -> {
        upperMantle.addToUpperMantleHeat(flux);
    });
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
