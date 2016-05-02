
package planet;

import planet.gui.DisplayAdapter;
import planet.surface.Surface;

/**
 * The class that encapsulates a surface and keeps track of the timescale.
 * @author Richard DeSilvey
 */
public abstract class Planet {
    
    private int gridSize;
    private int sqrtBase;
    private int base;
    protected TimeScale timescale;
    private static Planet current;
    private Surface planetSurface;

    public static final int DEFAULT_PLANET_DELAY = 1000;
    public static enum TimeScale {Geological, Evolutionary, Civilization}
    
    /**
     * Constructs a new Planet.
     *
     * @param gridSize The number of cells
     * @param sqrtBase The length of one side of a cell in meters.
     * @param surfaceThreadsDelay How fast does the planet thread update
     * @param threadCount The number of threads updating the map
     */
    public Planet(int gridSize, int sqrtBase, int surfaceThreadsDelay, int threadCount){
        
        current = this;
        this.gridSize = gridSize;
        base = sqrtBase * sqrtBase;
        this.sqrtBase = sqrtBase;
        timescale = TimeScale.Geological;
        planetSurface = new Surface(gridSize, DEFAULT_PLANET_DELAY, surfaceThreadsDelay, threadCount);
        initPlanet();
    }
    
    protected final void startThreads(){
        planetSurface.startAll();
        planetSurface.start();
    }
    
    protected final void initPlanet(){
        planetSurface.setupMap();
    }
    
    public final void play(){
        planetSurface.playAll();
        planetSurface.play();
    }
    
    public final void pause(){
        planetSurface.pauseAll();
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
    
    public final int getGridSize(){
        return gridSize;
    }
    
    public final int getTotalNumberOfCells(){
        return gridSize * gridSize;
    }
    
}
