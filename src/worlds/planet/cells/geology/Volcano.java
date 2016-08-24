package worlds.planet.cells.geology;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.ArrayList;
import worlds.planet.enums.Layer;
import worlds.planet.enums.RockType;
/**
 * The Volcano sprite deposits large amounts of Igneous rock and can be found
 * at plate boundaries and hot spots.
 * 
 * @author Richard DeSilvey
 */
public class Volcano implements PlanetObject {

    private float amount;
    private int radius;
    private static final Random rand;
    public static boolean showEvents;
    private boolean totop;
    private ArrayList<GeoCell> deposited;
    private Layer depositType;
    
    static {
        rand = new Random();
        showEvents = true;
    }
    
    public Volcano(int x, int y, int size, float amount, Layer type, 
            boolean totop){
        
        this.amount = amount;
        
        this.totop = totop;
        radius = size;
        depositType = type;
        deposited = new ArrayList<>();
    }

    public int getRadius() {
        return radius;
    }
    
    @Override
    public void update() {
        
        if (rand.nextBoolean()){
            int vx = (int)(pos.x * Tools.scaleWidth());
            int vy = (int)(pos.y * Tools.scaleHeight());

            int n = (int)(radius/2f);
            int range = rand.nextInt(5);

            double rad = (float) (rand.nextInt(360) * Math.PI) / 180;

            int nx = (int) (range * Math.cos(rad)) + vx;
            int ny = (int) (range * Math.sin(rad)) + vy;

            nx = checkXBounds(nx, parent.getMap());
            ny = checkYBounds(ny, parent.getMap());
            
            setupVolcano(nx, ny, amount, totop);

            radius--;

            if (radius < 5) {
                engine.kill(this);
                
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
        
        nx = checkXBounds(nx, parent.getMap());
        ny = checkYBounds(ny, parent.getMap());
        
        for (int i = 0; i < n; i++){
            erupt(nx, ny, rand.nextInt(radius), amount, totop);
        }
    }
    
    private void erupt(int x, int y, int size, float amount, boolean totop){
        
        double randomRads = (float) (rand.nextInt(360) * Math.PI) / 180;
        float offset;
        
        int nx = (int) (size * Math.cos(randomRads)) + x;
        int ny = (int) (size * Math.sin(randomRads)) + y;

        nx = checkXBounds(nx, parent.getMap());
        ny = checkYBounds(ny, parent.getMap());
        
        GeoCell cell = parent.getCellAt(nx, ny);
        Stratum temp = null;
        if (cell == null) return;
        else if (cell.peekTopStratum() == null) return;
        
        if (deposited.contains(cell)) return;
        
        offset = totop ? compressTopSedimentLayer(cell) : 0;
  
        if (!totop){
            if (cell.peekBottomStratum().getLayer() == METAMORPHIC){
                temp = cell.removeBottomStratum();
            }
        }
        
        cell.add(depositType, amount + offset, totop);

        if (temp != null){
            cell.appendStratum(temp);
        }
        
        cell.recalculateHeight();
        
        deposited.add(cell);
        
    }
    
    private float compressTopSedimentLayer(GeoCell cell){
        float offset = 0;
        
        Stratum top = cell.peekTopStratum();
        
        // If there is a layer of sediment, then turn it into sedimentary rock
        if (top != null){
         
            if (top.getLayer().getRockType() == RockType.SEDIMENT){
                if (top.getMass() > 1000){
                    top.setStratumType(Layer.BASALT);
                    cell.remove((offset = (long) (top.getMass()*0.95f)), false, true);
                }else{
                    offset = top.getMass();
                    cell.removeTopStratum();
                }
            }
        }
        
        return offset;
    }
    
    @Override
    public void draw(Graphics2D device) {
        
        if (showEvents){
            Graphics2D g2d = device;

            g2d.setColor(Color.RED);
            g2d.drawArc((int)sp.getX()-(radius/2), 
                    (int)sp.getY()-(radius/2), radius, radius, 0, 360);
        }
    }
}
