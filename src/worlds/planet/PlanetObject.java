package worlds.planet;

import com.jme3.math.Vector2f;
import java.awt.Graphics2D;

/**
 *
 * @author Richard DeSilvey
 */
public abstract class PlanetObject {

  protected Vector2f pos;
  private boolean isDead;

  public PlanetObject(int x, int y) {
    pos = new Vector2f(x, y);
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
