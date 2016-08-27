package worlds.planet.cells.geology;

import engine.util.Tools;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.Planet;
import worlds.planet.enums.Layer;
import worlds.planet.surface.Surface;
/**
 * The Volcano sprite deposits large amounts of Igneous rock and can be found
 * at plate boundaries and hot spots.
 * 
 * @author Richard DeSilvey
 */
public class Volcano extends PlanetObject {

    private float amount;
    private int radius;
    private static final Random rand;
    public static boolean showEvents;
    private boolean totop;
    private ArrayList<GeoCell> deposited;
    private Layer depositType;
    private static Surface parent;
    private int worldSize;
    private float surfaceDepthMul;
    
    static {
        rand = new Random();
        showEvents = true;
    }
    
    public Volcano(int x, int y, int size, float amount, Layer type, 
            boolean totop, float surfaceDepthMul){
        super(x, y);
        this.surfaceDepthMul = surfaceDepthMul;
        this.amount = amount;
        worldSize = Planet.instance().getSurface().getGridWidth();
        this.totop = totop;
        radius = size;
        depositType = type;
        deposited = new ArrayList<>();
        parent = Planet.instance().getSurface();
    }

    public int getRadius() {
        return radius;
    }
    
    @Override
    public void update() {
        
        if (rand.nextBoolean()){
            int vx = (int)(pos.getX() /** Tools.scaleWidth()*/);
            int vy = (int)(pos.getY() /** Tools.scaleHeight()*/);

            int n = (int)(radius/2f);
            int range = rand.nextInt(5);

            double rad = (float) (rand.nextInt(360) * Math.PI) / 180;

            int nx = (int) (range * Math.cos(rad)) + vx;
            int ny = (int) (range * Math.sin(rad)) + vy;

            nx = Tools.checkBounds(nx, worldSize);
            ny = Tools.checkBounds(ny, worldSize);
            
            setupVolcano(nx, ny, amount, totop);

            radius--;

            if (radius < 5) {
                kill();
                deposited.clear();
            }
        }
    }
    
    private void setupVolcano(int x, int y, float amount, boolean totop){
        
        int n = (int)(radius/2f);
        int range = rand.nextInt(5);
        
        double rad = (float) (rand.nextInt(360) * Math.PI) / 180;

        int nx = (int) (range * Math.cos(rad)) + x;
        int ny = (int) (range * Math.sin(rad)) + y;
        
        nx = Tools.checkBounds(nx, worldSize);
        ny = Tools.checkBounds(ny, worldSize);
        
        for (int i = 0; i < n; i++){
            erupt(nx, ny, rand.nextInt(radius), amount, totop);
        }
    }
    
    private void erupt(int x, int y, int size, float amount, boolean totop){
        
        double randomRads = (float) (rand.nextInt(360) * Math.PI) / 180;
        
        int nx = (int) (size * Math.cos(randomRads)) + x;
        int ny = (int) (size * Math.sin(randomRads)) + y;

        nx = Tools.checkBounds(nx, worldSize);
        ny = Tools.checkBounds(ny, worldSize);
        
        GeoCell cell = parent.getCellAt(nx, ny);
        Stratum temp = null;
        if (cell == null) return;
        else if (cell.peekTopStratum() == null) return;
        
        if (deposited.contains(cell)) {
            return;
        }

        if (!totop) {
            float thickness = cell.getStrataThickness();
            thickness *= surfaceDepthMul;
            cell.addAtDepth(depositType, amount, thickness);
        } else {
            float offset = cell.getSedimentBuffer().removeAllSediments();
            offset = Tools.changeMass(offset, Layer.MIX, depositType);
            cell.add(depositType, amount + offset, totop);

            if (temp != null) {
                cell.appendStratum(temp);
            }
        }
        cell.recalculateHeight();
        
        deposited.add(cell);
        
    }
    
    
    
    @Override
    public void draw(Graphics2D device) {
        
        if (showEvents){
            Graphics2D g2d = device;

            g2d.setColor(Color.RED);
            g2d.drawArc((int)pos.getX()-(radius/2), 
                    (int)pos.getY()-(radius/2), radius, radius, 0, 360);
        }
    }
}
