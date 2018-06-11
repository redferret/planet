
package worlds.planet.geosphere;

import engine.surface.Cell;
import java.util.concurrent.ThreadLocalRandom;
import static worlds.planet.geosphere.UpperMantle.UPPER_MANTLE_DEPTH;
import static worlds.planet.geosphere.UpperMantle.UPPER_MANTLE_SPECIFIC_HEAT;

/**
 *
 * @author Richard
 */
public class Mantle extends Cell {
  
  public Mantle(int x, int y) {
    super(x, y, ThreadLocalRandom.current().nextInt(400, 1000));

  }

  @Override
  public float getHeatCapacity() {
    return UPPER_MANTLE_SPECIFIC_HEAT;
  }

  @Override
  public float getZLength() {
    return UPPER_MANTLE_DEPTH;
  }

  @Override
  public float getBottomResistence() {
    return 0;
  }

  @Override
  public float getHorizontalResistence() {
    return 0;
  }

  @Override
  public float getTopResistence() {
    return 0;
  }

}
