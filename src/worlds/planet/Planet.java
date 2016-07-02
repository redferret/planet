package worlds.planet;

import java.util.logging.Level;
import java.util.logging.Logger;
import engine.surface.SurfaceMap;
import engine.surface.SurfaceThread;
import worlds.planet.surface.PlanetSurface;
import worlds.planet.surface.Surface;

/**
 * The class that encapsulates a surface and keeps track of the timescale.
 *
 * @author Richard DeSilvey
 */
public abstract class Planet {

    private int cellLength;
    private int area;
    protected TimeScale timescale;
    private static Planet current;
    private Surface planetSurface;

    private static final String EXISTING_INSTANCE_ERR_MSG = "An instance of Planet is already running: 5000";
    private static final int EXISTING_INSTANCE_ERR_MSG_CODE = 5000;
    
    public static enum TimeScale {
        Geological, Evolutionary, Civilization, None
    }

    static {
        current = null;
    }
    
    /**
     * Constructs a new Planet.
     *
     * @param gridWidth The number of cells in the X and Y axis. That is
     * gridWidth * gridWidth = total number of cells.
     * @param cellLength The length of one side of a cell in meters.
     * @param ageStepDelay The amount of time to delay between each update to
     * the planet's age.
     * @param surfaceThreadsDelay How fast does the planet thread(s) update
     * @param threadCount The number of threads that work on the map
     */
    public Planet(int gridWidth, int cellLength, int ageStepDelay, int surfaceThreadsDelay, int threadCount) {
        Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, "New Planet");
        if (current != null){
            Logger.getLogger(SurfaceThread.class.getName()).log(Level.SEVERE, EXISTING_INSTANCE_ERR_MSG);
            System.exit(EXISTING_INSTANCE_ERR_MSG_CODE);
        }
        current = this;
        area = cellLength * cellLength;
        this.cellLength = cellLength;
        timescale = TimeScale.None;
        planetSurface = new PlanetSurface(gridWidth, ageStepDelay, surfaceThreadsDelay, threadCount);
    }

    protected final void startThreads() {
        planetSurface.startSurfaceThreads();
        planetSurface.start();
    }

    public final void play() {
        planetSurface.playSurfaceThreads();
        planetSurface.play();
    }

    public final void pause() {
        planetSurface.pauseSurfaceThreads();
        planetSurface.pause();
    }
    
    public Surface getSurface() {
        return planetSurface;
    }

    /**
     * References the most recent instantiated object of this class.
     *
     * @return A reference to the current Planet
     */
    public final static Planet instance() {
        return current;
    }

    public boolean isTimeScale(TimeScale scale) {
        return scale == timescale;
    }

    public final TimeScale getTimeScale() {
        return timescale;
    }

    public void setTimescale(TimeScale timescale) {
        this.timescale = timescale;
    }

    /**
     * The length of a cell in meters.
     *
     * @return length of a cell in meters.
     */
    public int getCellLength() {
        return cellLength;
    }

    /**
     * The area of the cell is the cell length^2
     *
     * @return The area of a cell in square meters.
     */
    public int getCellArea() {
        return area;
    }

}
