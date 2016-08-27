

package worlds.planet.cells.geology;

import engine.util.Point;
import java.awt.Graphics2D;
import static worlds.planet.Planet.instance;

/**
 *
 * @author Richard DeSilvey
 */
public abstract class PlanetObject {
    
    protected Point pos;
    private boolean isDead;
    
    public PlanetObject(int x, int y){
        pos = new Point(x, y);
        isDead = false;
    }
    
    public void kill(){
        isDead = true;
    }
    
    public boolean isDead(){
        return isDead;
    }
    
    public abstract void draw(Graphics2D g2d);
    public abstract void update();
}
