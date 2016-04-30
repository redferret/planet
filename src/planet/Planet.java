
package planet;

import planet.gui.DisplayAdapter;
import planet.surface.GeoCell;
import planet.surface.Surface;
import planet.util.MThread;

/**
 * The class that encapsulates a surface and keeps track of the timescale.
 * @author Richard DeSilvey
 */
public abstract class Planet extends MThread {
    
    private int gridSize;
    private int sqrtBase;
    private int base;
    private long curFrame;
    private DisplayAdapter display;
    protected TimeScale timescale;
    private static Planet current;
    private Surface planetSurface;

    public static enum TimeScale {Geological, Evolutionary, Civilization}
    
    /**
     * Constructs a new Planet.
     *
     * @param gridSize The number of cells
     * @param sqrtBase The length of one side of a cell in meters.
     * @param mainThreadDelay The time to delay between each frame in
     * milliseconds.
     * @param planetThreadDelay How fast does the planet thread update
     * @param threadCount The number of threads updating the map
     */
    public Planet(int gridSize, int sqrtBase, int mainThreadDelay, 
            int planetThreadDelay, int threadCount){
        
        super(mainThreadDelay, "Main~", true);
        
        current = this;
        curFrame = 0;
        this.gridSize = gridSize;
        base = sqrtBase * sqrtBase;
        this.sqrtBase = sqrtBase;
        planetSurface = new Surface(gridSize, planetThreadDelay, threadCount);
        
    }
    
    public void setDisplay(DisplayAdapter display) {
        this.display = display;
    }
    
    protected final void startThreads(){
        planetSurface.startAll();
        planetSurface.start();
    }
    
    protected final void initPlanet(){
        planetSurface.setupMap();
    }
    
    public final void play(){
        planetSurface.play();
    }
    
    public final void pause(){
        planetSurface.pause();
    }
    
    public Surface getSurface() {
        return planetSurface;
    }
    
    public final static Planet self(){
        return current;
    }
    
    public final TimeScale getTimeScale(){
        return timescale;
    }
    
    public int getSqrtBase() {
        return sqrtBase;
    }

    public int getBase() {
        return base;
    }
    
    public void updateSurface(){
        boolean sw  = (curFrame % 2) == 0;
        int l       = gridSize, m;

        int ystart  = sw ? 0 : (l - 1);
        int yinc    = sw ? 1 : -1;

        for (int b = 0; b < 2; b++){
            for (int y = ystart; (sw ? (y < l) : (y >= 0)); y += yinc){

                m = ((b > 0) && (y % 2 == 0)) ? 1 :
                    ((b > 0) && (y % 2 != 0) ? -1 : 0);

                for (int x = (y % 2) + m; x < gridSize; x += 2){
                    update(x, y);
                }
            }
        }
        curFrame++;
        display.repaint();
    }
    
    private void update(int x, int y) {
        planetSurface.updateGeology(x, y);
        planetSurface.updateOceans(x, y);
        planetSurface.checkForMinimumHeight(x, y);
    }
    
    public final int getGridSize(){
        return gridSize;
    }
    
    public final int getTotalNumberOfCells(){
        return gridSize * gridSize;
    }
    
}
