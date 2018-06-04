
package worlds.planet.geosphere;

import com.jme3.math.Vector2f;

/**
 *
 * @author Richard
 */
public class HotSpot {
  /**
   * The position of this hot spot on the planet
   */
  private final Vector2f position;
  
  /**
   * How large the hot spot will be
   */
  private final float totalHeatMass;

  public HotSpot(Vector2f position, float totalHeatMass) {
    this.position = position;
    this.totalHeatMass = totalHeatMass;
  }

  public Vector2f getPosition() {
    return this.position;
  }
  
  
}
