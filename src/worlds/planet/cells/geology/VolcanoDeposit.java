package worlds.planet.cells.geology;

import engine.surface.SurfaceThread;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.util.Random;
import worlds.planet.enums.Layer;
import static worlds.planet.Planet.instance;

/**
 *
 * @author Richard DeSilvey
 */
public class VolcanoDeposit extends PlanetObject {
    public static int intrusiveCount, extrusiveCount;

    private long birthAge, maxAge;
    private boolean totop;
    private float amount,eruptionProb;
    private int size;
    private Layer type;
    public static boolean showEventTypes;
    private static Random rand;
    private SurfaceThread parent;
    
    static {
        showEventTypes = true;
        rand = new Random();
        intrusiveCount = 0;
        extrusiveCount = 0;
    }
    
    public VolcanoDeposit(int x, int y, long maxAge, float eruptprob, 
            boolean totop, float amount, int size, Layer type, SurfaceThread taskThread) {
        super(x, y);
        this.parent = taskThread;
        parent.addObject(this);
        this.maxAge = maxAge;
        this.eruptionProb = eruptprob;
        birthAge = instance().getSurface().getPlanetAge();
        
        this.totop = totop;
        this.size = size;
        this.amount = amount;
        this.type = type;
        if (totop)
                extrusiveCount++;
            else
                intrusiveCount++;
    }
    
    @Override
    public void update() {
        if ((instance().getSurface().getPlanetAge() - birthAge) >= maxAge){
            if (totop)
                extrusiveCount--;
            else
                intrusiveCount--;
            kill();
        }
        
        if(rand.nextFloat() <= eruptionProb){
            new VolcanoControl((int)pos.getX(), (int)pos.getY(), size, amount, totop, 
                type, parent);
        }
    }

    @Override
    public void draw(Graphics2D device) {
        if (showEventTypes){
            device.setColor(Color.MAGENTA);
            device.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            device.drawString(totop ? "V" : "U", pos.getX(), pos.getY());
        }
    }
}
