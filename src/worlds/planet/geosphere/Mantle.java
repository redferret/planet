
package worlds.planet.geosphere;

import com.jme3.math.Vector2f;
import engine.surface.Cell;
import engine.util.concurrent.AtomicFloat;
import java.util.concurrent.ThreadLocalRandom;
import static worlds.planet.geosphere.UpperMantle.UPPER_MANTLE_DEPTH;
import static worlds.planet.geosphere.UpperMantle.UPPER_MANTLE_SPECIFIC_HEAT;

/**
 *
 * @author Richard
 */
public class Mantle extends Cell {
  
  private final AtomicFloat magma;
  private final Vector2f magmaVelocity;
  private final Vector2f magmaAcceleration;
  
  public Mantle(int x, int y) {
    super(x, y, ThreadLocalRandom.current().nextInt(400, 1000));
    magma = new AtomicFloat(0);
    magmaVelocity = new Vector2f();
    magmaAcceleration = new Vector2f();
  }

  public float getMagma() {
    return magma.get();
  }
  
  public Vector2f getMagmaVelocity() {
    return magmaVelocity;
  }
  
  public void applyForceToMagma(Vector2f acc) {
    magmaAcceleration.add(acc);
  }
  
  public void updateMagmaVelocity() {
    magmaVelocity.add(magmaAcceleration);
    magmaAcceleration.zero();
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
