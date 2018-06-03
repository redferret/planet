
package worlds.planet.geosphere;

import engine.util.Vec2;

/**
 *
 * @author Richard
 */
public class HotSpot {
  /**
   * The position of this hot spot on the planet
   */
  private final Vec2 position;
  
  /**
   * How large the hot spot will be
   */
  private final float totalHeatMass;

  public HotSpot(Vec2 position, float totalHeatMass) {
    this.position = position;
    this.totalHeatMass = totalHeatMass;
  }

  public Vec2 getPosition() {
    return this.position;
  }
  
  
}
