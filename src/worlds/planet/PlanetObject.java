package worlds.planet;

import engine.util.Vec2;
import java.awt.Graphics2D;
import static worlds.planet.Planet.instance;

/**
 *
 * @author Richard DeSilvey
 */
public abstract class PlanetObject {

  protected Vec2 pos;
  private boolean isDead;

  public PlanetObject(int x, int y) {
    pos = new Vec2(x, y);
    isDead = false;
  }

  public void kill() {
    isDead = true;
  }

  public boolean isDead() {
    return isDead;
  }

  public abstract void draw(Graphics2D g2d);

  public abstract void update();
}
