package worlds.planet.cells.geology;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.util.Random;
import static worlds.planet.Planet.instance;
import worlds.planet.enums.Layer;
/**
 *
 * @author Richard DeSilvey
 */
public class VolcanoDeposit implements PlanetObject {
    public static int instances;

    private long birthAge, maxAge;
    private boolean totop;
    private float amount,eruptionProb;
    private int size;
    private Layer type;
    public static boolean showEventTypes;
    private static Random rand;
    
    static {
        showEventTypes = true;
        rand = new Random();
        instances = 0;
    }
    
    public VolcanoDeposit(float x, float y, long maxAge, float eruptprob, 
            boolean totop, float amount, int size, Layer type) {
        this.maxAge = maxAge;
        this.eruptionProb = eruptprob;
        birthAge = instance().getSurface().getPlanetAge();
        
        this.totop = totop;
        this.size = size;
        this.amount = amount;
        this.type = type;
        instances++;
    }
    
    @Override
    public void update() {
        if ((instance().getSurface().getPlanetAge() - birthAge) >= maxAge){
            instances--;
            engine.kill(this);
        }
        
        if(rand.nextFloat() <= eruptionProb){
            new VolcanoControl((int)pos.x, (int)pos.y, size, amount, totop, 
                type, engine.getGeosphere());
        }
    }

    @Override
    public void draw(Graphics2D device) {
        if (showEventTypes){
            device.setColor(Color.MAGENTA);
            device.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            device.drawString(totop ? "V" : "U", pos.x, pos.y);
        }
    }
}
