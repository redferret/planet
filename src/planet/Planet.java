
package planet;

import java.util.logging.Level;
import java.util.logging.Logger;
import planet.surface.SurfaceMap;
import planet.surface.PlanetSurface;
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

    public static enum TimeScale {Geological, Evolutionary, Civilization, None}
    
    /**
     * Constructs a new Planet.
     *
     * @param gridSize The number of cells
     * @param sqrtBase The length of one side of a cell in meters.
     * @param ageStepDelay The amount of time to delay between each update to the
     * planet's age.
     * @param surfaceThreadsDelay How fast does the planet thread update
     * @param threadCount The number of threads updating the map
     */
    public Planet(int gridSize, int sqrtBase, int ageStepDelay, int surfaceThreadsDelay, int threadCount){
        Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, "New Planet");
        current = this;
        this.gridSize = gridSize;
        base = sqrtBase * sqrtBase;
        this.sqrtBase = sqrtBase;
        timescale = TimeScale.None;
        planetSurface = new PlanetSurface(gridSize, ageStepDelay, surfaceThreadsDelay, threadCount);
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
    
    /**
     * A static reference to the most recent instantiated Planet.
     * @return A reference to the Planet 
     */
    public final static Planet self(){
        return current;
    }
    
    public boolean isTimeScale(TimeScale scale) {
        return scale == timescale;
    }

    public final TimeScale getTimeScale(){
        return timescale;
    }
    
    public void setTimescale(TimeScale timescale) {
        this.timescale = timescale;
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
