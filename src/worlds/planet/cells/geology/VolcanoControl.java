package worlds.planet.cells.geology;

import engine.surface.SurfaceThread;
import engine.util.Delay;
import engine.util.Point;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.enums.Layer;

/**
 *
 * @author Richard DeSilvey
 */
public class VolcanoControl extends PlanetObject {

    private static Random rand = ThreadLocalRandom.current();
    private boolean totop;
    private float amount;
    private int size, life;
    private Delay lifeDelay;
    private Layer type;
    private SurfaceThread parent;
    private float surfaceDepthMul;
    
    public VolcanoControl(int x, int y, int size, float amount, 
            boolean totop, Layer type, SurfaceThread taskThread) {
        super(x, y);
        parent = taskThread;
        parent.addObject(this);
        this.totop = totop;
        this.size = size;
        this.amount = amount;
        this.type = type;
        this.surfaceDepthMul = 0.75f;
        life = Math.max(10, rand.nextInt(size));
        lifeDelay = new Delay(life * 3, false);
    }

    public void setAgeLength(int life) {
        this.life = life;
    }
    
    @Override
    public void update() {
        
        if (!lifeDelay.check()){
            if (rand.nextFloat() < 0.05f){
                addVolcano((int)pos.getX(), (int)pos.getY(), size);
            }
            
            int n = rand.nextBoolean() ? -1 : 1;
            int m = rand.nextBoolean() ? -1 : 1;
            int rx = rand.nextInt(5) * n;
            int ry = rand.nextInt(5) * m;
            
            pos = new Point(pos.getX() + rx, pos.getY() + ry);
            
        }else{
            kill();
        }
    }
    
    private void addVolcano(int x, int y, int radius){
        
        int n = radius;
        int range = rand.nextInt(radius);
        
        double rad = (float) (rand.nextInt(360) * Math.PI) / 180;

        int nx = (int) (range * Math.cos(rad)) + x;
        int ny = (int) (range * Math.sin(rad)) + y;
        
        for (int i = 0; i < n; i++){
            PlanetObject o = new Volcano(nx, ny, size, amount, type, totop, surfaceDepthMul);
            parent.addObject(o);
        }
        
    }

    @Override
    public void draw(Graphics2D device) {
        
    }
}
