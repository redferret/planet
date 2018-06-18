
package worlds.planet.geosphere;

import engine.surface.Cell;
import java.util.concurrent.ThreadLocalRandom;
import static worlds.planet.geosphere.Core.CORE_DEPTH;

/**
 *
 * @author Richard
 */
public class CoreCell extends Cell {

  public CoreCell(int x, int y) {
    super(x, y, ThreadLocalRandom.current().nextInt(2500, 3200));
    addToMagma(ThreadLocalRandom.current().nextInt(400, 600));
  }

  @Override
  public float getHeatCapacity() {
    return 45.0f;
  }

  @Override
  public float getZLength() {
    return CORE_DEPTH;
  }

  @Override
  public float getBCR() {
    return 0;
  }

  @Override
  public float getHCR() {
    return 1e6f;
  }

  @Override
  public float getTCR() {
    return 0;
  }
}
