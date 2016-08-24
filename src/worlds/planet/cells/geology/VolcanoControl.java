package worlds.planet.cells.geology;

import engine.util.Delay;
import java.awt.Graphics2D;
import java.util.Random;
import worlds.planet.enums.Layer;

/**
 *
 * @author Richard DeSilvey
 */
public class VolcanoControl implements PlanetObject {

    private static Random rand = new Random();
    private boolean totop;
    private float amount;
    private int size, life;
    private Delay lifeDelay;
    private Layer type;
    
    public VolcanoControl(int x, int y, int size, float amount, 
            boolean totop, Layer type) {
        
        this.totop = totop;
        this.size = size;
        this.amount = amount;
        this.type = type;
        life = Math.max(10, rand.nextInt(size));
        lifeDelay = new Delay(life * 3, false);
    }

    @Override
    public void update() {
        
        if (!lifeDelay.check()){
            if (rand.nextFloat() < 0.05f){
                addVolcano((int)pos.x, (int)pos.y, 30);
            }
            
            int n = rand.nextBoolean() ? -1 : 1;
            int m = rand.nextBoolean() ? -1 : 1;
            int rx = rand.nextInt(5) * n;
            int ry = rand.nextInt(5) * m;
            
            pos.x += rx;
            pos.y += ry;
            
        }else{
            engine.kill(this);
        }
    }
    
    private void addVolcano(int x, int y, int radius){
        
        int n = radius;
        int range = rand.nextInt(radius);
        
        double rad = (float) (rand.nextInt(360) * Math.PI) / 180;

        int nx = (int) (range * Math.cos(rad)) + x;
        int ny = (int) (range * Math.sin(rad)) + y;
        
        for (int i = 0; i < n; i++){
            new Volcano(nx, ny, size, amount, type, totop);
        }
        
    }

    @Override
    public void draw(Graphics2D device) {
        
    }
}
